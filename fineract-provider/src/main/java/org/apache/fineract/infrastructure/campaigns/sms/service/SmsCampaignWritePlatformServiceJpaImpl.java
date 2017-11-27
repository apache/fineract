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
package org.apache.fineract.infrastructure.campaigns.sms.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignStatus;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.data.CampaignPreviewData;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsCampaignMustBeClosedToBeDeletedException;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsCampaignMustBeClosedToEditException;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsCampaignNotFound;
import org.apache.fineract.infrastructure.campaigns.sms.serialization.SmsCampaignValidator;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.domain.ReportRepository;
import org.apache.fineract.infrastructure.dataqueries.exception.ReportNotFoundException;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistration;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistrationRepositoryWrapper;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTypeException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.useradministration.domain.AppUser;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.JsonElement;

@Service
public class SmsCampaignWritePlatformServiceJpaImpl implements SmsCampaignWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(SmsCampaignWritePlatformServiceJpaImpl.class);

    private final PlatformSecurityContext context;

    private final SmsCampaignRepository smsCampaignRepository;
    private final SmsCampaignValidator smsCampaignValidator;
    private final ReportRepository reportRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final GroupRepository groupRepository;
    private final ReadReportingService readReportingService;
    private final GenericDataService genericDataService;
    private final FromJsonHelper fromJsonHelper;
    private final DeviceRegistrationRepositoryWrapper deviceRegistrationRepository;

    private final SmsMessageScheduledJobService smsMessageScheduledJobService;
    
    @Autowired
    public SmsCampaignWritePlatformServiceJpaImpl(final PlatformSecurityContext context, final SmsCampaignRepository smsCampaignRepository,
            final SmsCampaignValidator smsCampaignValidator, final ReportRepository reportRepository,
            final SmsMessageRepository smsMessageRepository, final ClientRepositoryWrapper clientRepositoryWrapper,
            final ReadReportingService readReportingService, final GenericDataService genericDataService,
            final FromJsonHelper fromJsonHelper, final GroupRepository groupRepository,
            final SmsMessageScheduledJobService smsMessageScheduledJobService, final DeviceRegistrationRepositoryWrapper deviceRegistrationRepository) {
        this.context = context;
        this.smsCampaignRepository = smsCampaignRepository;
        this.smsCampaignValidator = smsCampaignValidator;
        this.reportRepository = reportRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.readReportingService = readReportingService;
        this.genericDataService = genericDataService;
        this.fromJsonHelper = fromJsonHelper;
        this.groupRepository = groupRepository;
        this.smsMessageScheduledJobService = smsMessageScheduledJobService ;
        this.deviceRegistrationRepository = deviceRegistrationRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(JsonCommand command) {

        final AppUser currentUser = this.context.authenticatedUser();
        this.smsCampaignValidator.validateCreate(command.json());
        final Long runReportId = command.longValueOfParameterNamed(SmsCampaignValidator.runReportId);
        Report report = this.reportRepository.findOne(runReportId);
        if (report == null) { throw new ReportNotFoundException(runReportId); }
        SmsCampaign smsCampaign = SmsCampaign.instance(currentUser, report, command);
        if (smsCampaign.getRecurrenceStartDate() != null && smsCampaign.getRecurrenceStartDate().isBefore(DateUtils.getLocalDateOfTenant())) { throw new GeneralPlatformDomainRuleException(
                "error.msg.campaign.recurrenceStartDate.in.the.past", "Recurrence start date cannot be the past date.",
                smsCampaign.getRecurrenceStartDate()); }
        this.smsCampaignRepository.save(smsCampaign);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(smsCampaign.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long resourceId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.smsCampaignValidator.validateForUpdate(command.json());
            final SmsCampaign smsCampaign = this.smsCampaignRepository.findOne(resourceId);

            if (smsCampaign == null) { throw new SmsCampaignNotFound(resourceId); }
            if (smsCampaign.isActive()) { throw new SmsCampaignMustBeClosedToEditException(smsCampaign.getId()); }
            final Map<String, Object> changes = smsCampaign.update(command);

            if (changes.containsKey(SmsCampaignValidator.runReportId)) {
                final Long newValue = command.longValueOfParameterNamed(SmsCampaignValidator.runReportId);
                final Report reportId = this.reportRepository.findOne(newValue);
                if (reportId == null) { throw new ReportNotFoundException(newValue); }
                ;
                smsCampaign.updateBusinessRuleId(reportId);
            }

            if (!changes.isEmpty()) {
                this.smsCampaignRepository.saveAndFlush(smsCampaign);
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
        final SmsCampaign smsCampaign = this.smsCampaignRepository.findOne(resourceId);

        if (smsCampaign == null) { throw new SmsCampaignNotFound(resourceId); }
        if (smsCampaign.isActive()) { throw new SmsCampaignMustBeClosedToBeDeletedException(smsCampaign.getId()); }

        /*
         * Do not delete but set a boolean is_visible to zero
         */
        smsCampaign.delete();
        this.smsCampaignRepository.saveAndFlush(smsCampaign);

        return new CommandProcessingResultBuilder() //
                .withEntityId(smsCampaign.getId()) //
                .build();

    }

    private void insertDirectCampaignIntoSmsOutboundTable(SmsCampaign smsCampaign) {
        try {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});

            HashMap<String, String> queryParamForRunReport = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});

            List<HashMap<String, Object>> runReportObject = this.getRunReportByServiceImpl(campaignParams.get("reportName"),
                    queryParamForRunReport);

            if (runReportObject != null) {
                for (HashMap<String, Object> entry : runReportObject) {
                    String textMessage = this.compileSmsTemplate(smsCampaign.getMessage(), smsCampaign.getCampaignName(), entry);
                    Integer clientId = (Integer) entry.get("id");
                    Object mobileNo = entry.get("mobileNo");

                    Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId.longValue());
                    if (this.smsCampaignValidator.isValidNotificationOrSms(client, smsCampaign, mobileNo)) {
//                        String countryCode = this.smsReadPlatformService.retrieveCountryCode(client.getOffice().getId()).getCountryCode();
                    	String mobileNumber = null;
                    	if(mobileNo != null){
                    		mobileNumber = mobileNo.toString();
                    	}
                        SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, textMessage, mobileNumber,
                                smsCampaign, smsCampaign.isNotification());
                        this.smsMessageRepository.save(smsMessage);
                    }
                }
            }
        } catch (final IOException e) {
        	logger.error(e.getMessage());
        }

    }



    @Override
    public void insertDirectCampaignIntoSmsOutboundTable(final Loan loan, final SmsCampaign smsCampaign) {
        try {
            if (loan.hasInvalidLoanType()) { throw new InvalidLoanTypeException("Loan Type cannot be 0 for the Triggered Sms Campaign"); }

            Set<Client> clientSet = new HashSet<>();

            HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});
            campaignParams.put("loanId", loan.getId().toString());
            
            HashMap<String, String> queryParamForRunReport = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});
            queryParamForRunReport.put("loanId", loan.getId().toString());

            if (loan.isGroupLoan()) {
                Group group = this.groupRepository.findOne(loan.getGroupId());
                clientSet.addAll(group.getClientMembers());
                queryParamForRunReport.put("groupId", group.getId().toString());
            } else {
                Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(loan.getClientId());
                clientSet.add(client);
            }

            for (Client client : clientSet) {
                campaignParams.put("clientId", client.getId().toString());
                queryParamForRunReport.put("clientId", client.getId().toString());
                
                List<HashMap<String, Object>> runReportObject = this.getRunReportByServiceImpl(campaignParams.get("reportName"),
                        queryParamForRunReport);

                if (runReportObject != null && runReportObject.size() > 0) {
                    for (HashMap<String, Object> entry : runReportObject) {
                        String textMessage = this.compileSmsTemplate(smsCampaign.getMessage(), smsCampaign.getCampaignName(), entry);
                        Object mobileNo = entry.get("mobileNo");
                        
                        if (this.smsCampaignValidator.isValidNotificationOrSms(client, smsCampaign, mobileNo)) {
                        	String mobileNumber = null;
                        	if(mobileNo != null){
                        		mobileNumber = mobileNo.toString();
                        	}
                            SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, textMessage, mobileNumber,
                                    smsCampaign, smsCampaign.isNotification());
                            smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
                            this.smsMessageRepository.save(smsMessage);
                            Collection<SmsMessage> messages = new ArrayList<>() ;
                            messages.add(smsMessage) ;
                            Map<SmsCampaign, Collection<SmsMessage>> smsDataMap = new HashMap<>() ;
                            smsDataMap.put(smsCampaign, messages) ;
                             this.smsMessageScheduledJobService.sendTriggeredMessages(smsDataMap);
                        }
                    }
                }
            }
        } catch (final IOException e) {
        	logger.error(e.getMessage()) ;
        } catch (final RuntimeException e) {
        	logger.error(e.getMessage()) ;
        }
    }

    @Override
    public void insertDirectCampaignIntoSmsOutboundTable(final Client client, final SmsCampaign smsCampaign) {
        try {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});
            campaignParams.put("clientId", client.getId().toString());
            HashMap<String, String> queryParamForRunReport = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});
            
			campaignParams.put("clientId", client.getId().toString());
			queryParamForRunReport.put("clientId", client.getId().toString());
			
			List<HashMap<String, Object>> runReportObject = this
					.getRunReportByServiceImpl(campaignParams.get("reportName"), queryParamForRunReport);

			if (runReportObject != null && runReportObject.size() > 0) {
				for (HashMap<String, Object> entry : runReportObject) {
					String textMessage = this.compileSmsTemplate(smsCampaign.getMessage(),
							smsCampaign.getCampaignName(), entry);
					Object mobileNo = entry.get("mobileNo");

					if (this.smsCampaignValidator.isValidNotificationOrSms(client, smsCampaign, mobileNo)) {
						String mobileNumber = null;
                    	if(mobileNo != null){
                    		mobileNumber = mobileNo.toString();
                    	}
						SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, textMessage,
								mobileNumber, smsCampaign, smsCampaign.isNotification());
						smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
						this.smsMessageRepository.save(smsMessage);
						Collection<SmsMessage> messages = new ArrayList<>();
						messages.add(smsMessage);
						Map<SmsCampaign, Collection<SmsMessage>> smsDataMap = new HashMap<>();
						smsDataMap.put(smsCampaign, messages);
						this.smsMessageScheduledJobService.sendTriggeredMessages(smsDataMap);
					}
				}
			}
        } catch (final IOException e) {
        	logger.error(e.getMessage()) ;
        } catch (final RuntimeException e) {
        	logger.error(e.getMessage()) ;
        }
    }
    
    @Override
    public void insertDirectCampaignIntoSmsOutboundTable(final SavingsAccount savingsAccount, final SmsCampaign smsCampaign) {
        try {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});
            campaignParams.put("savingsId", savingsAccount.getId().toString());
            HashMap<String, String> queryParamForRunReport = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                    new TypeReference<HashMap<String, String>>() {});
            queryParamForRunReport.put("savingsId", savingsAccount.getId().toString());
            
            Client client = savingsAccount.getClient() ;
			List<HashMap<String, Object>> runReportObject = this
					.getRunReportByServiceImpl(campaignParams.get("reportName"), queryParamForRunReport);

			if (runReportObject != null && runReportObject.size() > 0) {
				for (HashMap<String, Object> entry : runReportObject) {
					String textMessage = this.compileSmsTemplate(smsCampaign.getMessage(),
							smsCampaign.getCampaignName(), entry);
					Object mobileNo = entry.get("mobileNo");

					if (this.smsCampaignValidator.isValidNotificationOrSms(client, smsCampaign, mobileNo)) {
						String mobileNumber = null;
                    	if(mobileNo != null){
                    		mobileNumber = mobileNo.toString();
                    	}
						SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, textMessage,
								mobileNumber, smsCampaign, smsCampaign.isNotification());
						smsMessage.setStatusType(SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue());
						this.smsMessageRepository.save(smsMessage);
						Collection<SmsMessage> messages = new ArrayList<>();
						messages.add(smsMessage);
						Map<SmsCampaign, Collection<SmsMessage>> smsDataMap = new HashMap<>();
						smsDataMap.put(smsCampaign, messages);
						this.smsMessageScheduledJobService.sendTriggeredMessages(smsDataMap);
					}
				}
			}
        } catch (final IOException e) {
        	logger.error(e.getMessage()) ;
        } catch (final RuntimeException e) {
        	logger.error(e.getMessage()) ;
        }
    }
    
    private void updateTriggerDates(Long campaignId) {
        final SmsCampaign smsCampaign = this.smsCampaignRepository.findOne(campaignId);
        if (smsCampaign == null) { throw new SmsCampaignNotFound(campaignId); }
        LocalDateTime nextTriggerDate = smsCampaign.getNextTriggerDate();
        smsCampaign.setLastTriggerDate(nextTriggerDate.toDate());
        // calculate new trigger date and insert into next trigger date

        /**
         * next run time has to be in the future if not calculate a new future
         * date
         */
        LocalDate nextRuntime = CalendarUtils.getNextRecurringDate(smsCampaign.getRecurrence(), smsCampaign.getNextTriggerDate()
                .toLocalDate(), nextTriggerDate.toLocalDate());
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
            nextRuntime = CalendarUtils.getNextRecurringDate(smsCampaign.getRecurrence(), smsCampaign.getNextTriggerDate().toLocalDate(),
                    DateUtils.getLocalDateOfTenant());
        }
        final LocalDateTime getTime = smsCampaign.getRecurrenceStartDateTime();
        final String dateString = nextRuntime.toString() + " " + getTime.getHourOfDay() + ":" + getTime.getMinuteOfHour() + ":"
                + getTime.getSecondOfMinute();
        final DateTimeFormatter simpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        final LocalDateTime newTriggerDateWithTime = LocalDateTime.parse(dateString, simpleDateFormat);

        smsCampaign.setNextTriggerDate(newTriggerDateWithTime.toDate());
        this.smsCampaignRepository.saveAndFlush(smsCampaign);
    }

    @Transactional
    @Override
    public CommandProcessingResult activateSmsCampaign(Long campaignId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();

        this.smsCampaignValidator.validateActivation(command.json());

        final SmsCampaign smsCampaign = this.smsCampaignRepository.findOne(campaignId);

        if (smsCampaign == null) { throw new SmsCampaignNotFound(campaignId); }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

        smsCampaign.activate(currentUser, fmt, activationDate);

        this.smsCampaignRepository.saveAndFlush(smsCampaign);

        if (smsCampaign.isDirect()) {
            insertDirectCampaignIntoSmsOutboundTable(smsCampaign);
        } else if (smsCampaign.isSchedule()) {

            /**
             * if recurrence start date is in the future calculate next trigger
             * date if not use recurrence start date us next trigger date when
             * activating
             */
            LocalDate nextTriggerDate = null;
            if (smsCampaign.getRecurrenceStartDateTime().isBefore(tenantDateTime())) {
                nextTriggerDate = CalendarUtils.getNextRecurringDate(smsCampaign.getRecurrence(), smsCampaign.getRecurrenceStartDate(),
                        DateUtils.getLocalDateOfTenant());
            } else {
                nextTriggerDate = smsCampaign.getRecurrenceStartDate();
            }
            // to get time of tenant
            final LocalDateTime getTime = smsCampaign.getRecurrenceStartDateTime();

            final String dateString = nextTriggerDate.toString() + " " + getTime.getHourOfDay() + ":" + getTime.getMinuteOfHour() + ":"
                    + getTime.getSecondOfMinute();
            final DateTimeFormatter simpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            final LocalDateTime nextTriggerDateWithTime = LocalDateTime.parse(dateString, simpleDateFormat);

            smsCampaign.setNextTriggerDate(nextTriggerDateWithTime.toDate());
            this.smsCampaignRepository.saveAndFlush(smsCampaign);
        }

        /*
         * if campaign is direct insert campaign message into sms outbound table
         * else if its a schedule create a job process for it
         */
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(smsCampaign.getId()) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult closeSmsCampaign(Long campaignId, JsonCommand command) {

        final AppUser currentUser = this.context.authenticatedUser();
        this.smsCampaignValidator.validateClosedDate(command.json());

        final SmsCampaign smsCampaign = this.smsCampaignRepository.findOne(campaignId);
        if (smsCampaign == null) { throw new SmsCampaignNotFound(campaignId); }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate closureDate = command.localDateValueOfParameterNamed("closureDate");

        smsCampaign.close(currentUser, fmt, closureDate);

        this.smsCampaignRepository.saveAndFlush(smsCampaign);
//        this.serviceui.sendMessagesToGateway();

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(smsCampaign.getId()) //
                .build();
    }

    @Override
    public String compileSmsTemplate(final String textMessageTemplate, final String campaignName, final Map<String, Object> smsParams) {
        final MustacheFactory mf = new DefaultMustacheFactory();
        final Mustache mustache = mf.compile(new StringReader(textMessageTemplate), campaignName);

        final StringWriter stringWriter = new StringWriter();
        mustache.execute(stringWriter, smsParams);

        return stringWriter.toString();
    }

    private List<HashMap<String, Object>> getRunReportByServiceImpl(final String reportName, final Map<String, String> queryParams)
            throws IOException {
        final String reportType = "report";

        List<HashMap<String, Object>> resultList = new ArrayList<>();
        final GenericResultsetData results = this.readReportingService.retrieveGenericResultSetForSmsEmailCampaign(reportName, reportType,
                queryParams);

        try {
            final String response = this.genericDataService.generateJsonFromGenericResultsetData(results);
            resultList = new ObjectMapper().readValue(response, new TypeReference<List<HashMap<String, Object>>>() {});
        } catch (JsonParseException e) {
            logger.info("Conversion of report query results to JSON failed: " + e.getMessage() + " - Location: " + e.getLocation());
            return resultList;
        }
        // loop changes array date to string date
        for (HashMap<String, Object> entry : resultList) {
            for (Map.Entry<String, Object> map : entry.entrySet()) {
                String key = map.getKey();
                Object ob = map.getValue();
                if (ob instanceof ArrayList && ((ArrayList) ob).size() == 3) {
                    String changeArrayDateToStringDate = ((ArrayList) ob).get(2).toString() + "-" + ((ArrayList) ob).get(1).toString()
                            + "-" + ((ArrayList) ob).get(0).toString();
                    entry.put(key, changeArrayDateToStringDate);
                }
            }
        }
        return resultList;
    }

    @Override
    public CampaignPreviewData previewMessage(final JsonQuery query) {
        CampaignPreviewData campaignMessage = null;
        this.context.authenticatedUser();
        this.smsCampaignValidator.validatePreviewMessage(query.json());
        // final String smsParams =
        // this.fromJsonHelper.extractJsonObjectNamed("paramValue",
        // query.parsedJson()).getAsString();
        final JsonElement smsParamsElement = this.fromJsonHelper
                .extractJsonObjectNamed(SmsCampaignValidator.paramValue, query.parsedJson());
        String smsParams = smsParamsElement.toString();
        final String textMessageTemplate = this.fromJsonHelper.extractStringNamed("message", query.parsedJson());

        try {
            HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsParams,
                    new TypeReference<HashMap<String, String>>() {});

            HashMap<String, String> queryParamForRunReport = new ObjectMapper().readValue(smsParams,
                    new TypeReference<HashMap<String, String>>() {});

            List<HashMap<String, Object>> runReportObject = this.getRunReportByServiceImpl(campaignParams.get("reportName"),
                    queryParamForRunReport);

            if (runReportObject != null && !runReportObject.isEmpty()) {
                for (HashMap<String, Object> entry : runReportObject) {
                    // add string object to campaignParam object
                    String textMessage = this.compileSmsTemplate(textMessageTemplate, "SmsCampaign", entry);
                    if (!textMessage.isEmpty()) {
                        final Integer totalMessage = runReportObject.size();
                        campaignMessage = new CampaignPreviewData(textMessage, totalMessage);
                        break;
                    }
                }
            } else {
                campaignMessage = new CampaignPreviewData(textMessageTemplate, 0);
            }
        } catch (final IOException e) {
            // TODO throw something here
        }
        return campaignMessage;
    }

    @Transactional
    @Override
    public CommandProcessingResult reactivateSmsCampaign(final Long campaignId, JsonCommand command) {

        this.smsCampaignValidator.validateActivation(command.json());

        final AppUser currentUser = this.context.authenticatedUser();

        final SmsCampaign smsCampaign = this.smsCampaignRepository.findOne(campaignId);

        if (smsCampaign == null) { throw new SmsCampaignNotFound(campaignId); }

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
        final LocalDate reactivationDate = command.localDateValueOfParameterNamed("activationDate");
        smsCampaign.reactivate(currentUser, fmt, reactivationDate);
        if (smsCampaign.isDirect()) {
            insertDirectCampaignIntoSmsOutboundTable(smsCampaign);
        } else if (smsCampaign.isSchedule()) {

            /**
             * if recurrence start date is in the future calculate next trigger
             * date if not use recurrence start date us next trigger date when
             * activating
             */
            LocalDate nextTriggerDate = null;
            if (smsCampaign.getRecurrenceStartDateTime().isBefore(tenantDateTime())) {
                nextTriggerDate = CalendarUtils.getNextRecurringDate(smsCampaign.getRecurrence(), smsCampaign.getRecurrenceStartDate(),
                        DateUtils.getLocalDateOfTenant());
            } else {
                nextTriggerDate = smsCampaign.getRecurrenceStartDate();
            }
            // to get time of tenant
            final LocalDateTime getTime = smsCampaign.getRecurrenceStartDateTime();

            final String dateString = nextTriggerDate.toString() + " " + getTime.getHourOfDay() + ":" + getTime.getMinuteOfHour() + ":"
                    + getTime.getSecondOfMinute();
            final DateTimeFormatter simpleDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            final LocalDateTime nextTriggerDateWithTime = LocalDateTime.parse(dateString, simpleDateFormat);

            smsCampaign.setNextTriggerDate(nextTriggerDateWithTime.toDate());
        }
        this.smsCampaignRepository.saveAndFlush(smsCampaign);

        return new CommandProcessingResultBuilder() //
                .withEntityId(smsCampaign.getId()) //
                .build();

    }

    @Override
    @CronTarget(jobName = JobName.UPDATE_SMS_OUTBOUND_WITH_CAMPAIGN_MESSAGE)
    public void storeTemplateMessageIntoSmsOutBoundTable() throws JobExecutionException {
        final Collection<SmsCampaign> smsCampaignDataCollection = this.smsCampaignRepository.findByTriggerTypeAndStatus(
                SmsCampaignTriggerType.SCHEDULE.getValue(), SmsCampaignStatus.ACTIVE.getValue());
        if (smsCampaignDataCollection != null) {
            for (SmsCampaign smsCampaign : smsCampaignDataCollection) {
                LocalDateTime tenantDateNow = tenantDateTime();
                LocalDateTime nextTriggerDate = smsCampaign.getNextTriggerDate();

                logger.info("tenant time " + tenantDateNow.toString() + " trigger time " + nextTriggerDate.toString() + JobName.UPDATE_SMS_OUTBOUND_WITH_CAMPAIGN_MESSAGE.name());
                if (nextTriggerDate.isBefore(tenantDateNow)) {
                    insertDirectCampaignIntoSmsOutboundTable(smsCampaign);
                    this.updateTriggerDates(smsCampaign.getId());
                }
            }
        }
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();

        throw new PlatformDataIntegrityException("error.msg.sms.campaign.unknown.data.integrity.issue",
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
    
//    private String formatDestinationPhoneNumber(String phoneNumber, String countryCallingCode) {
//        StringBuilder formatedPhoneNumber = new StringBuilder("+");
//
//        try {
//            Long phoneNumberToLong = Long.parseLong(phoneNumber);
//            Long countryCallingCodeToLong = Long.parseLong(countryCallingCode);
//            formatedPhoneNumber.append(Long.toString(countryCallingCodeToLong));
//            formatedPhoneNumber.append(Long.toString(phoneNumberToLong));
//        }
//
//        catch (Exception e) {
//            logger.error("Invalid phone number or country calling code, must contain only numbers", e);
//        }
//
//        return formatedPhoneNumber.toString();
//    }
}
