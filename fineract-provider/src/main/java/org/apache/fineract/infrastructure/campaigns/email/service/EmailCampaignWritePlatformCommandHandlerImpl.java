/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.campaigns.email.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignValidator;
import org.apache.fineract.infrastructure.campaigns.email.data.PreviewCampaignMessage;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaign;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessage;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageRepository;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignMustBeClosedToBeDeletedException;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignMustBeClosedToEditException;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignNotFound;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportParameterUsage;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportRepository;
import org.apache.fineract.infrastructure.dataqueries.exception.ReportNotFoundException;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailCampaignWritePlatformCommandHandlerImpl implements EmailCampaignWritePlatformService {

    private final PlatformSecurityContext context;
    private final EmailCampaignRepository emailCampaignRepository;
    private final EmailCampaignValidator emailCampaignValidator;
    private final ReportRepository reportRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ReadReportingService readReportingService;
    private final GenericDataService genericDataService;
    private final FromJsonHelper fromJsonHelper;

    @Transactional
    @Override
    public CommandProcessingResult create(JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();

        this.emailCampaignValidator.validateCreate(command.json());

        final Long businessRuleId = command.longValueOfParameterNamed(EmailCampaignValidator.businessRuleId);

        final Report businessRule = this.reportRepository.findById(businessRuleId)
                .orElseThrow(() -> new ReportNotFoundException(businessRuleId));

        final Long reportId = command.longValueOfParameterNamed(EmailCampaignValidator.stretchyReportId);

        Report report = null;
        Map<String, String> stretchyReportParams = null;
        if (reportId != null) {
            report = this.reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId));
            final Set<ReportParameterUsage> reportParameterUsages = report.getReportParameterUsages();
            stretchyReportParams = new HashMap<>();

            if (reportParameterUsages != null && !reportParameterUsages.isEmpty()) {
                for (final ReportParameterUsage reportParameterUsage : reportParameterUsages) {
                    stretchyReportParams.put(reportParameterUsage.getReportParameterName(), "");
                }
            }
        }

        EmailCampaign emailCampaign = EmailCampaign.instance(currentUser, businessRule, report, command);
        if (stretchyReportParams != null) {
            emailCampaign.setStretchyReportParamMap(new Gson().toJson(stretchyReportParams));
        }

        this.emailCampaignRepository.saveAndFlush(emailCampaign);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(emailCampaign.getId()).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long resourceId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.emailCampaignValidator.validateForUpdate(command.json());
            final EmailCampaign emailCampaign = this.emailCampaignRepository.findById(resourceId)
                    .orElseThrow(() -> new EmailCampaignNotFound(resourceId));

            if (emailCampaign.isActive()) {
                throw new EmailCampaignMustBeClosedToEditException(emailCampaign.getId());
            }
            final Map<String, Object> changes = emailCampaign.update(command);

            if (changes.containsKey(EmailCampaignValidator.businessRuleId)) {
                final Long newValue = command.longValueOfParameterNamed(EmailCampaignValidator.businessRuleId);
                final Report reportId = this.reportRepository.findById(newValue).orElseThrow(() -> new ReportNotFoundException(newValue));
                emailCampaign.setBusinessRuleId(reportId);

            }

            if (!changes.isEmpty()) {
                this.emailCampaignRepository.saveAndFlush(emailCampaign);
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(resourceId).with(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }

    }

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long resourceId) {
        this.context.authenticatedUser();
        final EmailCampaign emailCampaign = this.emailCampaignRepository.findById(resourceId)
                .orElseThrow(() -> new EmailCampaignNotFound(resourceId));

        if (emailCampaign.isActive()) {
            throw new EmailCampaignMustBeClosedToBeDeletedException(emailCampaign.getId());
        }

        /*
         * Do not delete but set a boolean is_visible to zero
         */
        emailCampaign.delete();
        this.emailCampaignRepository.saveAndFlush(emailCampaign);

        return new CommandProcessingResultBuilder() //
                .withEntityId(emailCampaign.getId()) //
                .build();

    }

    @Override
    public void insertDirectCampaignIntoEmailOutboundTable(final Loan loan, final EmailCampaign emailCampaign,
            HashMap<String, String> campaignParams) {
        try {
            List<HashMap<String, Object>> runReportObject = this.getRunReportByServiceImpl(campaignParams.get("reportName"),
                    campaignParams);

            if (runReportObject != null) {
                for (HashMap<String, Object> entry : runReportObject) {
                    String message = this.compileEmailTemplate(emailCampaign.getEmailMessage(), emailCampaign.getCampaignName(), entry);
                    Client client = loan.getClient();
                    String emailAddress = client.emailAddress();

                    if (emailAddress != null && isValidEmail(emailAddress)) {
                        EmailMessage emailMessage = EmailMessage.pendingEmail(null, client, null, emailCampaign,
                                emailCampaign.getEmailSubject(), message, emailAddress, emailCampaign.getCampaignName());
                        this.emailMessageRepository.save(emailMessage);
                    }
                }
            }
        } catch (final IOException e) {
            // TODO throw something here
        }

    }

    private void insertDirectCampaignIntoEmailOutboundTable(final String emailParams, final String emailSubject,
            final String messageTemplate, final String campaignName, final Long campaignId) {
        try {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(emailParams,
                    new TypeReference<HashMap<String, String>>() {});

            HashMap<String, String> queryParamForRunReport = new ObjectMapper().readValue(emailParams,
                    new TypeReference<HashMap<String, String>>() {});

            List<HashMap<String, Object>> runReportObject = this.getRunReportByServiceImpl(campaignParams.get("reportName"),
                    queryParamForRunReport);

            if (runReportObject != null) {
                for (HashMap<String, Object> entry : runReportObject) {
                    String message = this.compileEmailTemplate(messageTemplate, campaignName, entry);
                    Integer clientId = (Integer) entry.get("id");
                    EmailCampaign emailCampaign = this.emailCampaignRepository.findById(campaignId).orElse(null);
                    Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId.longValue());
                    String emailAddress = client.emailAddress();

                    if (emailAddress != null && isValidEmail(emailAddress)) {
                        EmailMessage emailMessage = EmailMessage.pendingEmail(null, client, null, emailCampaign, emailSubject, message,
                                emailAddress, campaignName);
                        this.emailMessageRepository.save(emailMessage);
                    }
                }
            }
        } catch (final IOException e) {
            // TODO throw something here
        }

    }

    public static boolean isValidEmail(String email) {

        boolean isValid = true;

        try {

            InternetAddress emailO = new InternetAddress(email);
            emailO.validate();

        } catch (AddressException ex) {

            isValid = false;
        }
        return isValid;
    }

    @Transactional
    @Override
    public CommandProcessingResult activateEmailCampaign(Long campaignId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();

        this.emailCampaignValidator.validateActivation(command.json());

        final EmailCampaign emailCampaign = this.emailCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new EmailCampaignNotFound(campaignId));

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

        emailCampaign.activate(currentUser, fmt, activationDate);

        this.emailCampaignRepository.saveAndFlush(emailCampaign);

        if (emailCampaign.isDirect()) {
            insertDirectCampaignIntoEmailOutboundTable(emailCampaign.getParamValue(), emailCampaign.getEmailSubject(),
                    emailCampaign.getEmailMessage(), emailCampaign.getCampaignName(), emailCampaign.getId());
        } else if (emailCampaign.isSchedule()) {
            // if recurrence start date is in the past, calculate next trigger date, otherwise use recurrence start
            // date as next trigger date when activating
            LocalDateTime nextTriggerDateWithTime;
            LocalDateTime recurrenceStartDate = emailCampaign.getRecurrenceStartDate();
            LocalDateTime tenantDateTime = DateUtils.getLocalDateTimeOfTenant();
            if (DateUtils.isBefore(recurrenceStartDate, tenantDateTime)) {
                nextTriggerDateWithTime = CalendarUtils.getNextRecurringDate(emailCampaign.getRecurrence(), recurrenceStartDate,
                        tenantDateTime);
            } else {
                nextTriggerDateWithTime = recurrenceStartDate;
            }

            emailCampaign.setNextTriggerDate(nextTriggerDateWithTime);
            this.emailCampaignRepository.saveAndFlush(emailCampaign);
        }

        /*
         * if campaign is direct insert campaign message into email outbound table else if its a schedule create a job
         * process for it
         */
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(emailCampaign.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult closeEmailCampaign(Long campaignId, JsonCommand command) {

        final AppUser currentUser = this.context.authenticatedUser();
        this.emailCampaignValidator.validateClosedDate(command.json());

        final EmailCampaign emailCampaign = this.emailCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new EmailCampaignNotFound(campaignId));

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closureDate = command.localDateValueOfParameterNamed("closureDate");

        emailCampaign.close(currentUser, fmt, closureDate);

        this.emailCampaignRepository.saveAndFlush(emailCampaign);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(emailCampaign.getId()) //
                .build();
    }

    private String compileEmailTemplate(final String textMessageTemplate, final String campaignName,
            final Map<String, Object> emailParams) {
        final MustacheFactory mf = new DefaultMustacheFactory();
        final Mustache mustache = mf.compile(new StringReader(textMessageTemplate), campaignName);

        final StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, emailParams);

        return stringWriter.toString();
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    @Override
    public List<HashMap<String, Object>> getRunReportByServiceImpl(final String reportName, final Map<String, String> queryParams)
            throws IOException {
        final String reportType = "report";

        List<HashMap<String, Object>> resultList;
        final GenericResultsetData results = this.readReportingService.retrieveGenericResultSetForSmsEmailCampaign(reportName, reportType,
                queryParams);
        final String response = this.genericDataService.generateJsonFromGenericResultsetData(results);
        resultList = new ObjectMapper().readValue(response, new TypeReference<>() {});
        // loop changes array date to string date
        for (Iterator<HashMap<String, Object>> it = resultList.iterator(); it.hasNext();) {
            HashMap<String, Object> entry = it.next();
            for (Iterator<Map.Entry<String, Object>> iter = entry.entrySet().iterator(); iter.hasNext();) {
                Map.Entry<String, Object> map = iter.next();
                String key = map.getKey();
                Object ob = map.getValue();
                if (ob instanceof ArrayList && ((ArrayList) ob).size() == 3) {
                    String changeArrayDateToStringDate = ((ArrayList) ob).get(2).toString() + "-" + ((ArrayList) ob).get(1).toString() + "-"
                            + ((ArrayList) ob).get(0).toString();
                    entry.put(key, changeArrayDateToStringDate);
                }
            }
        }
        return resultList;
    }

    @Override
    public PreviewCampaignMessage previewMessage(final JsonQuery query) {
        PreviewCampaignMessage campaignMessage = null;
        this.context.authenticatedUser();
        this.emailCampaignValidator.validatePreviewMessage(query.json());
        final String emailParams = this.fromJsonHelper.extractStringNamed("paramValue", query.parsedJson());
        final String textMessageTemplate = this.fromJsonHelper.extractStringNamed("emailMessage", query.parsedJson());

        try {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(emailParams,
                    new TypeReference<HashMap<String, String>>() {});

            HashMap<String, String> queryParamForRunReport = new ObjectMapper().readValue(emailParams,
                    new TypeReference<HashMap<String, String>>() {});

            List<HashMap<String, Object>> runReportObject = this.getRunReportByServiceImpl(campaignParams.get("reportName"),
                    queryParamForRunReport);

            if (runReportObject != null) {
                for (HashMap<String, Object> entry : runReportObject) {
                    // add string object to campaignParam object
                    String textMessage = this.compileEmailTemplate(textMessageTemplate, "EmailCampaign", entry);
                    if (!textMessage.isEmpty()) {
                        final Integer totalMessage = runReportObject.size();
                        campaignMessage = new PreviewCampaignMessage().setCampaignMessage(textMessage)
                                .setTotalNumberOfMessages(totalMessage);
                        break;
                    }
                }
            }
        } catch (final IOException e) {
            // TODO throw something here
        }

        return campaignMessage;

    }

    @Transactional
    @Override
    public CommandProcessingResult reactivateEmailCampaign(final Long campaignId, JsonCommand command) {
        this.emailCampaignValidator.validateActivation(command.json());

        final AppUser currentUser = this.context.authenticatedUser();

        final EmailCampaign emailCampaign = this.emailCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new EmailCampaignNotFound(campaignId));

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        final LocalDate reactivationDate = command.localDateValueOfParameterNamed("activationDate");
        emailCampaign.reactivate(currentUser, fmt, reactivationDate);
        if (emailCampaign.isSchedule()) {
            // if recurrence start date is in the past, calculate next trigger date, otherwise use recurrence start date
            // as next trigger date when activating
            LocalDateTime nextTriggerDate = null;
            LocalDateTime tenantDateTime = DateUtils.getLocalDateTimeOfTenant();
            LocalDateTime recurrenceStartDate = emailCampaign.getRecurrenceStartDate();
            if (DateUtils.isBefore(recurrenceStartDate, tenantDateTime)) {
                nextTriggerDate = CalendarUtils.getNextRecurringDate(emailCampaign.getRecurrence(), recurrenceStartDate, tenantDateTime);
            } else {
                nextTriggerDate = recurrenceStartDate;
            }
            final String dateString = nextTriggerDate.toString() + " " + recurrenceStartDate.getHour() + ":"
                    + recurrenceStartDate.getMinute() + ":" + recurrenceStartDate.getSecond();
            final DateTimeFormatter simpleDateFormat = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                    .appendPattern("yyyy-MM-dd HH:mm:ss").toFormatter();
            final LocalDateTime nextTriggerDateWithTime = LocalDateTime.parse(dateString, simpleDateFormat);

            emailCampaign.setNextTriggerDate(nextTriggerDateWithTime);
            this.emailCampaignRepository.saveAndFlush(emailCampaign);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(emailCampaign.getId()) //
                .build();

    }

    private void handleDataIntegrityIssues(@SuppressWarnings("unused") final JsonCommand command, final Throwable realCause,
            final NonTransientDataAccessException dve) {
        throw ErrorHandler.getMappable(dve, "error.msg.email.campaign.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
