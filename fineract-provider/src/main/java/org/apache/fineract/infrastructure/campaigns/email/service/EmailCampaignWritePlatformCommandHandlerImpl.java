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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.domain.*;
import org.apache.fineract.infrastructure.dataqueries.exception.ReportNotFoundException;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.FileSystemContentRepository;
import org.apache.fineract.infrastructure.reportmailingjob.helper.IPv4Helper;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailMessageWithAttachmentData;
import org.apache.fineract.infrastructure.campaigns.email.domain.*;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignMustBeClosedToBeDeletedException;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignMustBeClosedToEditException;
import org.apache.fineract.infrastructure.campaigns.email.exception.EmailCampaignNotFound;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.campaigns.email.data.PreviewCampaignMessage;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignData;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailCampaignValidator;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.*;
import java.util.*;

@Service
public class EmailCampaignWritePlatformCommandHandlerImpl implements EmailCampaignWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(EmailCampaignWritePlatformCommandHandlerImpl.class);

    private final PlatformSecurityContext context;

    private final EmailCampaignRepository emailCampaignRepository;
    private final EmailCampaignValidator emailCampaignValidator;
    private final EmailCampaignReadPlatformService emailCampaignReadPlatformService;
    private final ReportRepository reportRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ReadReportingService readReportingService;
    private final GenericDataService genericDataService;
    private final FromJsonHelper fromJsonHelper;
    private final LoanRepository loanRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final EmailMessageJobEmailService emailMessageJobEmailService;

    @Autowired
    public EmailCampaignWritePlatformCommandHandlerImpl(final PlatformSecurityContext context,
            final EmailCampaignRepository emailCampaignRepository, final EmailCampaignValidator emailCampaignValidator,
            final EmailCampaignReadPlatformService emailCampaignReadPlatformService, final ReportRepository reportRepository,
            final EmailMessageRepository emailMessageRepository, final ClientRepositoryWrapper clientRepositoryWrapper,
            final ReadReportingService readReportingService, final GenericDataService genericDataService,
            final FromJsonHelper fromJsonHelper, final LoanRepository loanRepository,
            final SavingsAccountRepository savingsAccountRepository, final EmailMessageJobEmailService emailMessageJobEmailService) {
        this.context = context;
        this.emailCampaignRepository = emailCampaignRepository;
        this.emailCampaignValidator = emailCampaignValidator;
        this.emailCampaignReadPlatformService = emailCampaignReadPlatformService;
        this.reportRepository = reportRepository;
        this.emailMessageRepository = emailMessageRepository;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.readReportingService = readReportingService;
        this.genericDataService = genericDataService;
        this.fromJsonHelper = fromJsonHelper;
        this.loanRepository = loanRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.emailMessageJobEmailService = emailMessageJobEmailService;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(JsonCommand command) {

        final AppUser currentUser = this.context.authenticatedUser();

        this.emailCampaignValidator.validateCreate(command.json());

        final Long businessRuleId = command.longValueOfParameterNamed(EmailCampaignValidator.businessRuleId);

        final Report businessRule = this.reportRepository.findOne(businessRuleId);
        if (businessRule == null) { throw new ReportNotFoundException(businessRuleId); }

        final Long reportId = command.longValueOfParameterNamed(EmailCampaignValidator.stretchyReportId);

        final Report report = this.reportRepository.findOne(reportId);
        if (report == null) { throw new ReportNotFoundException(reportId); }
        // find all report parameters and store them as json string
        final Set<ReportParameterUsage> reportParameterUsages = report.getReportParameterUsages();
        final Map<String, String> stretchyReportParams = new HashMap<>();

        if (reportParameterUsages != null && !reportParameterUsages.isEmpty()) {
            for (final ReportParameterUsage reportParameterUsage : reportParameterUsages) {
                stretchyReportParams.put(reportParameterUsage.getReportParameterName(), "");
            }
        }

        EmailCampaign emailCampaign = EmailCampaign.instance(currentUser, businessRule, report, command);
        emailCampaign.setStretchyReportParamMap(new Gson().toJson(stretchyReportParams));

        this.emailCampaignRepository.save(emailCampaign);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(emailCampaign.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long resourceId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.emailCampaignValidator.validateForUpdate(command.json());
            final EmailCampaign emailCampaign = this.emailCampaignRepository.findOne(resourceId);

            if (emailCampaign == null) { throw new EmailCampaignNotFound(resourceId); }
            if (emailCampaign.isActive()) { throw new EmailCampaignMustBeClosedToEditException(emailCampaign.getId()); }
            final Map<String, Object> changes = emailCampaign.update(command);

            if (changes.containsKey(EmailCampaignValidator.businessRuleId)) {
                final Long newValue = command.longValueOfParameterNamed(EmailCampaignValidator.businessRuleId);
                final Report reportId = this.reportRepository.findOne(newValue);
                if (reportId == null) { throw new ReportNotFoundException(newValue); }
                emailCampaign.updateBusinessRuleId(reportId);

            }

            if (!changes.isEmpty()) {
                this.emailCampaignRepository.saveAndFlush(emailCampaign);
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(resourceId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }

    }

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long resourceId) {
        this.context.authenticatedUser();
        final EmailCampaign emailCampaign = this.emailCampaignRepository.findOne(resourceId);

        if (emailCampaign == null) { throw new EmailCampaignNotFound(resourceId); }
        if (emailCampaign.isActive()) { throw new EmailCampaignMustBeClosedToBeDeletedException(emailCampaign.getId()); }

        /*
         * Do not delete but set a boolean is_visible to zero
         */
        emailCampaign.delete();
        this.emailCampaignRepository.saveAndFlush(emailCampaign);

        return new CommandProcessingResultBuilder() //
                .withEntityId(emailCampaign.getId()) //
                .build();

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
                    EmailCampaign emailCampaign = this.emailCampaignRepository.findOne(campaignId);
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

    @Override
    @CronTarget(jobName = JobName.UPDATE_EMAIL_OUTBOUND_WITH_CAMPAIGN_MESSAGE)
    public void storeTemplateMessageIntoEmailOutBoundTable() throws JobExecutionException {
        final Collection<EmailCampaignData> emailCampaignDataCollection = this.emailCampaignReadPlatformService
                .retrieveAllScheduleActiveCampaign();
        if (emailCampaignDataCollection != null) {
            for (EmailCampaignData emailCampaignData : emailCampaignDataCollection) {
                LocalDateTime tenantDateNow = tenantDateTime();
                LocalDateTime nextTriggerDate = emailCampaignData.getNextTriggerDate().toLocalDateTime();

                logger.info("tenant time " + tenantDateNow.toString() + " trigger time " + nextTriggerDate.toString());
                if (nextTriggerDate.isBefore(tenantDateNow)) {
                    insertDirectCampaignIntoEmailOutboundTable(emailCampaignData.getParamValue(), emailCampaignData.getEmailSubject(),
                            emailCampaignData.getMessage(), emailCampaignData.getCampaignName(), emailCampaignData.getId());
                    this.updateTriggerDates(emailCampaignData.getId());
                }
            }
        }
    }

    private void updateTriggerDates(Long campaignId) {
        final EmailCampaign emailCampaign = this.emailCampaignRepository.findOne(campaignId);
        if (emailCampaign == null) { throw new EmailCampaignNotFound(campaignId); }
        LocalDateTime nextTriggerDate = emailCampaign.getNextTriggerDate();
        emailCampaign.setLastTriggerDate(nextTriggerDate.toDate());
        // calculate new trigger date and insert into next trigger date

        /**
         * next run time has to be in the future if not calculate a new future
         * date
         */
        LocalDate nextRuntime = CalendarUtils.getNextRecurringDate(emailCampaign.getRecurrence(),
                emailCampaign.getNextTriggerDate().toLocalDate(), nextTriggerDate.toLocalDate());
        if (nextRuntime.isBefore(DateUtils.getLocalDateOfTenant())) { // means
                                                                      // next
                                                                      // run
                                                                      // time is
                                                                      // in the
                                                                      // past
                                                                      // calculate
                                                                      // a new
                                                                      // future
                                                                      // date
            nextRuntime = CalendarUtils.getNextRecurringDate(emailCampaign.getRecurrence(),
                    emailCampaign.getNextTriggerDate().toLocalDate(), DateUtils.getLocalDateOfTenant());
        }
        final LocalDateTime getTime = emailCampaign.getRecurrenceStartDateTime();
        final String dateString = nextRuntime.toString() + " " + getTime.getHourOfDay() + ":" + getTime.getMinuteOfHour() + ":"
                + getTime.getSecondOfMinute();
        final DateTimeFormatter simpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        final LocalDateTime newTriggerDateWithTime = LocalDateTime.parse(dateString, simpleDateFormat);

        emailCampaign.setNextTriggerDate(newTriggerDateWithTime.toDate());
        this.emailCampaignRepository.saveAndFlush(emailCampaign);
    }

    @Transactional
    @Override
    public CommandProcessingResult activateEmailCampaign(Long campaignId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();

        this.emailCampaignValidator.validateActivation(command.json());

        final EmailCampaign emailCampaign = this.emailCampaignRepository.findOne(campaignId);

        if (emailCampaign == null) { throw new EmailCampaignNotFound(campaignId); }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

        emailCampaign.activate(currentUser, fmt, activationDate);

        this.emailCampaignRepository.saveAndFlush(emailCampaign);

        if (emailCampaign.isDirect()) {
            insertDirectCampaignIntoEmailOutboundTable(emailCampaign.getParamValue(), emailCampaign.getEmailSubject(),
                    emailCampaign.getEmailMessage(), emailCampaign.getCampaignName(), emailCampaign.getId());
        } else {
            if (emailCampaign.isSchedule()) {

                /**
                 * if recurrence start date is in the future calculate next
                 * trigger date if not use recurrence start date us next trigger
                 * date when activating
                 */
                LocalDate nextTriggerDate = null;
                if (emailCampaign.getRecurrenceStartDateTime().isBefore(tenantDateTime())) {
                    nextTriggerDate = CalendarUtils.getNextRecurringDate(emailCampaign.getRecurrence(),
                            emailCampaign.getRecurrenceStartDate(), DateUtils.getLocalDateOfTenant());
                } else {
                    nextTriggerDate = emailCampaign.getRecurrenceStartDate();
                }
                // to get time of tenant
                final LocalDateTime getTime = emailCampaign.getRecurrenceStartDateTime();

                final String dateString = nextTriggerDate.toString() + " " + getTime.getHourOfDay() + ":" + getTime.getMinuteOfHour() + ":"
                        + getTime.getSecondOfMinute();
                final DateTimeFormatter simpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                final LocalDateTime nextTriggerDateWithTime = LocalDateTime.parse(dateString, simpleDateFormat);

                emailCampaign.setNextTriggerDate(nextTriggerDateWithTime.toDate());
                this.emailCampaignRepository.saveAndFlush(emailCampaign);
            }
        }

        /*
         * if campaign is direct insert campaign message into email outbound
         * table else if its a schedule create a job process for it
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

        final EmailCampaign emailCampaign = this.emailCampaignRepository.findOne(campaignId);
        if (emailCampaign == null) { throw new EmailCampaignNotFound(campaignId); }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
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
    private List<HashMap<String, Object>> getRunReportByServiceImpl(final String reportName, final Map<String, String> queryParams)
            throws IOException {
        final String reportType = "report";

        List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
        final GenericResultsetData results = this.readReportingService.retrieveGenericResultSetForSmsEmailCampaign(reportName, reportType,
                queryParams);
        final String response = this.genericDataService.generateJsonFromGenericResultsetData(results);
        resultList = new ObjectMapper().readValue(response, new TypeReference<List<HashMap<String, Object>>>() {});
        // loop changes array date to string date
        for (HashMap<String, Object> entry : resultList) {
            for (Map.Entry<String, Object> map : entry.entrySet()) {
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
                        campaignMessage = new PreviewCampaignMessage(textMessage, totalMessage);
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

        final EmailCampaign emailCampaign = this.emailCampaignRepository.findOne(campaignId);

        if (emailCampaign == null) { throw new EmailCampaignNotFound(campaignId); }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate reactivationDate = command.localDateValueOfParameterNamed("activationDate");
        emailCampaign.reactivate(currentUser, fmt, reactivationDate);
        if (emailCampaign.isSchedule()) {

            /**
             * if recurrence start date is in the future calculate next trigger
             * date if not use recurrence start date us next trigger date when
             * activating
             */
            LocalDate nextTriggerDate = null;
            if (emailCampaign.getRecurrenceStartDateTime().isBefore(tenantDateTime())) {
                nextTriggerDate = CalendarUtils.getNextRecurringDate(emailCampaign.getRecurrence(), emailCampaign.getRecurrenceStartDate(),
                        DateUtils.getLocalDateOfTenant());
            } else {
                nextTriggerDate = emailCampaign.getRecurrenceStartDate();
            }
            // to get time of tenant
            final LocalDateTime getTime = emailCampaign.getRecurrenceStartDateTime();

            final String dateString = nextTriggerDate.toString() + " " + getTime.getHourOfDay() + ":" + getTime.getMinuteOfHour() + ":"
                    + getTime.getSecondOfMinute();
            final DateTimeFormatter simpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            final LocalDateTime nextTriggerDateWithTime = LocalDateTime.parse(dateString, simpleDateFormat);

            emailCampaign.setNextTriggerDate(nextTriggerDateWithTime.toDate());
            this.emailCampaignRepository.saveAndFlush(emailCampaign);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(emailCampaign.getId()) //
                .build();

    }

    private void handleDataIntegrityIssues(@SuppressWarnings("unused") final JsonCommand command,
            final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();

        throw new PlatformDataIntegrityException("error.msg.email.campaign.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private LocalDateTime tenantDateTime() {
        LocalDateTime today = new LocalDateTime();
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();

        if (tenant != null) {
            final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
            if (zone != null) {
                today = new LocalDateTime(zone);
            }
        }
        return today;
    }

    @Override
    @CronTarget(jobName = JobName.EXECUTE_EMAIL)
    public void sendEmailMessage() throws JobExecutionException {
        if (IPv4Helper.applicationIsNotRunningOnLocalMachine()) { // remove when
                                                                  // testing
                                                                  // locally
            final List<EmailMessage> emailMessages = this.emailMessageRepository
                    .findByStatusType(EmailMessageStatusType.PENDING.getValue()); // retrieve
                                                                                  // all
                                                                                  // pending
                                                                                  // message

            for (final EmailMessage emailMessage : emailMessages) {

                if (isValidEmail(emailMessage.getEmailAddress())) {

                    final EmailCampaign emailCampaign = this.emailCampaignRepository.findOne(emailMessage.getEmailCampaign().getId()); //

                    final ScheduledEmailAttachmentFileFormat emailAttachmentFileFormat = ScheduledEmailAttachmentFileFormat
                            .instance(emailCampaign.getEmailAttachmentFileFormat());

                    final List<File> attachmentList = new ArrayList<>();

                    final StringBuilder errorLog = new StringBuilder();

                    // check if email attachment format exist
                    if (emailAttachmentFileFormat != null && Arrays.asList(ScheduledEmailAttachmentFileFormat.validValues())
                            .contains(emailAttachmentFileFormat.getId())) {

                        final Report stretchyReport = emailCampaign.getStretchyReport();

                        final String reportName = (stretchyReport != null) ? stretchyReport.getReportName() : null;

                        final HashMap<String, String> reportStretchyParams = this
                                .validateStretchyReportParamMap(emailCampaign.getStretchyReportParamMap());

                        // there is a probability that a client has one or more
                        // loans or savings therefore we need to send two or
                        // more attachments
                        if (reportStretchyParams.containsKey("selectLoan") || reportStretchyParams.containsKey("loanId")) {
                            // get all ids of the client loans
                            if (emailMessage.getClient() != null) {

                                final List<Loan> loans = this.loanRepository.findLoanByClientId(emailMessage.getClient().getId());

                                HashMap<String, String> reportParams = this
                                        .replaceStretchyParamsWithActualClientParams(reportStretchyParams, emailMessage.getClient());

                                for (final Loan loan : loans) {
                                    if (loan.isOpen()) { // only send attachment
                                                         // for active loan

                                        if (reportStretchyParams.containsKey("selectLoan")) {

                                            reportParams.put("SelectLoan", loan.getId().toString());

                                        } else if (reportStretchyParams.containsKey("loanId")) {

                                            reportParams.put("loanId", loan.getId().toString());
                                        }
                                        File file = this.generateAttachments(emailCampaign, emailAttachmentFileFormat, reportParams,
                                                reportName, errorLog);

                                        if (file != null) {
                                            attachmentList.add(file);
                                        } else {
                                            errorLog.append(reportParams.toString());
                                        }
                                    }
                                }

                            }
                        } else if (reportStretchyParams.containsKey("savingId")) {
                            if (emailMessage.getClient() != null) {

                                final List<SavingsAccount> savingsAccounts = this.savingsAccountRepository
                                        .findSavingAccountByClientId(emailMessage.getClient().getId());

                                HashMap<String, String> reportParams = this
                                        .replaceStretchyParamsWithActualClientParams(reportStretchyParams, emailMessage.getClient());

                                for (final SavingsAccount savingsAccount : savingsAccounts) {

                                    if (savingsAccount.isActive()) {

                                        reportParams.put("savingId", savingsAccount.getId().toString());

                                        File file = this.generateAttachments(emailCampaign, emailAttachmentFileFormat, reportParams,
                                                reportName, errorLog);

                                        if (file != null) {
                                            attachmentList.add(file);
                                        } else {
                                            errorLog.append(reportParams.toString());
                                        }
                                    }
                                }
                            }
                        } else {
                            if (emailMessage.getClient() != null) {

                                HashMap<String, String> reportParams = this
                                        .replaceStretchyParamsWithActualClientParams(reportStretchyParams, emailMessage.getClient());

                                File file = this.generateAttachments(emailCampaign, emailAttachmentFileFormat, reportParams, reportName,
                                        errorLog);

                                if (file != null) {
                                    attachmentList.add(file);
                                } else {
                                    errorLog.append(reportParams.toString());
                                }
                            }
                        }

                    }

                    final EmailMessageWithAttachmentData emailMessageWithAttachmentData = EmailMessageWithAttachmentData.createNew(
                            emailMessage.getEmailAddress(), emailMessage.getMessage(), emailMessage.getEmailSubject(), attachmentList);

                    if (!attachmentList.isEmpty() && attachmentList.size() > 0) { // only
                                                                                  // send
                                                                                  // email
                                                                                  // message
                                                                                  // if
                                                                                  // there
                                                                                  // is
                                                                                  // an
                                                                                  // attachment
                                                                                  // to
                                                                                  // it

                        this.emailMessageJobEmailService.sendEmailWithAttachment(emailMessageWithAttachmentData);

                        emailMessage.setStatusType(EmailMessageStatusType.SENT.getValue());

                        this.emailMessageRepository.save(emailMessage);
                    } else {
                        emailMessage.updateErrorMessage(errorLog.toString());

                        emailMessage.setStatusType(EmailMessageStatusType.FAILED.getValue());

                        this.emailMessageRepository.save(emailMessage);
                    }
                }
            }

        }

    }

    /**
     * This generates the the report and converts it to a file by passing the
     * parameters below
     * 
     * @param emailCampaign
     * @param emailAttachmentFileFormat
     * @param reportParams
     * @param reportName
     * @param errorLog
     * @return
     */
    private File generateAttachments(final EmailCampaign emailCampaign, final ScheduledEmailAttachmentFileFormat emailAttachmentFileFormat,
            final Map<String, String> reportParams, final String reportName, final StringBuilder errorLog) {

        try {
            final ByteArrayOutputStream byteArrayOutputStream = this.readReportingService.generatePentahoReportAsOutputStream(reportName,
                    emailAttachmentFileFormat.getValue(), reportParams, null, emailCampaign.getApprovedBy(), errorLog);

            final String fileLocation = FileSystemContentRepository.FINERACT_BASE_DIR + File.separator + "";
            final String fileNameWithoutExtension = fileLocation + File.separator + reportName;

            // check if file directory exists, if not create directory
            if (!new File(fileLocation).isDirectory()) {
                new File(fileLocation).mkdirs();
            }

            if (byteArrayOutputStream.size() == 0) {
                errorLog.append("Pentaho report processing failed, empty output stream created");
            } else if (errorLog.length() == 0 && (byteArrayOutputStream.size() > 0)) {
                final String fileName = fileNameWithoutExtension + "." + emailAttachmentFileFormat.getValue();

                final File file = new File(fileName);
                final FileOutputStream outputStream = new FileOutputStream(file);
                byteArrayOutputStream.writeTo(outputStream);

                return file;
            }

        } catch (IOException | PlatformDataIntegrityException e) {
            errorLog.append("The ReportMailingJobWritePlatformServiceImpl.executeReportMailingJobs threw an IOException " + "exception: "
                    + e.getMessage() + " ---------- ");
        }
        return null;
    }

    /**
     * This matches the the actual values to the key in the report stretchy
     * parameters map
     * 
     * @param stretchyParams
     * @param client
     * @return
     */
    private HashMap<String, String> replaceStretchyParamsWithActualClientParams(final HashMap<String, String> stretchyParams,
            final Client client) {

        HashMap<String, String> actualParams = new HashMap<>();

        for (Map.Entry<String, String> entry : stretchyParams.entrySet()) {
            if (entry.getKey().equals("selectOffice")) {
                // most at times the reports are run by picking the office of
                // the staff Id
                if (client.getStaff() != null) {
                    actualParams.put(entry.getKey(), client.getStaff().officeId().toString());
                } else {
                    actualParams.put(entry.getKey(), client.getOffice().getId().toString());
                }

            } else if (entry.getKey().equals("selectClient")) {

                actualParams.put(entry.getKey(), client.getId().toString());

            } else if (entry.getKey().equals("selectLoanofficer")) {

                actualParams.put(entry.getKey(), client.getStaff().getId().toString());

            } else if (entry.getKey().equals("environementUrl")) {

                actualParams.put(entry.getKey(), entry.getKey());
            }
        }
        return actualParams;
    }

    private HashMap<String, String> validateStretchyReportParamMap(final String stretchyParams) {

        HashMap<String, String> stretchyReportParamHashMap = new HashMap<>();

        if (!StringUtils.isEmpty(stretchyParams)) {
            try {
                stretchyReportParamHashMap = new ObjectMapper().readValue(stretchyParams, new TypeReference<HashMap<String, String>>() {});
            }

            catch (Exception e) {
                stretchyReportParamHashMap = null;
            }
        }

        return stretchyReportParamHashMap;
    }

}
