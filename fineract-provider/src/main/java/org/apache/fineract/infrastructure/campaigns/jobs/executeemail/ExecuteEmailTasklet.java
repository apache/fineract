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
package org.apache.fineract.infrastructure.campaigns.jobs.executeemail;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailMessageWithAttachmentData;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaign;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessage;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageRepository;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailMessageStatusType;
import org.apache.fineract.infrastructure.campaigns.email.domain.ScheduledEmailAttachmentFileFormat;
import org.apache.fineract.infrastructure.campaigns.email.service.EmailMessageJobEmailService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.dataqueries.domain.Report;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import org.apache.fineract.infrastructure.reportmailingjob.helper.IPv4Helper;
import org.apache.fineract.infrastructure.reportmailingjob.validation.ReportMailingJobValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class ExecuteEmailTasklet implements Tasklet {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailCampaignRepository emailCampaignRepository;
    private final LoanRepository loanRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final EmailMessageJobEmailService emailMessageJobEmailService;
    private final ReadReportingService readReportingService;
    private final ReportMailingJobValidator reportMailingJobValidator;
    private final FineractProperties fineractProperties;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (IPv4Helper.applicationIsNotRunningOnLocalMachine()) {
            final List<EmailMessage> emailMessages = emailMessageRepository.findByStatusType(EmailMessageStatusType.PENDING.getValue());
            for (final EmailMessage emailMessage : emailMessages) {
                if (isValidEmail(emailMessage.getEmailAddress())) {
                    final EmailCampaign emailCampaign = emailCampaignRepository.findById(emailMessage.getEmailCampaign().getId())
                            .orElse(null); //
                    ScheduledEmailAttachmentFileFormat emailAttachmentFileFormat = null;
                    if (emailCampaign.getEmailAttachmentFileFormat() != null) {
                        emailAttachmentFileFormat = ScheduledEmailAttachmentFileFormat
                                .instance(emailCampaign.getEmailAttachmentFileFormat());
                    }
                    final List<File> attachmentList = new ArrayList<>();
                    final StringBuilder errorLog = new StringBuilder();
                    if (emailAttachmentFileFormat != null && Arrays.asList(ScheduledEmailAttachmentFileFormat.validValues())
                            .contains(emailAttachmentFileFormat.getId())) {
                        final Report stretchyReport = emailCampaign.getStretchyReport();
                        final String reportName = (stretchyReport != null) ? stretchyReport.getReportName() : null;
                        final HashMap<String, String> reportStretchyParams = reportMailingJobValidator
                                .validateStretchyReportParamMap(emailCampaign.getStretchyReportParamMap());
                        if (reportStretchyParams.containsKey("selectLoan") || reportStretchyParams.containsKey("loanId")) {
                            if (emailMessage.getClient() != null) {
                                final List<Loan> loans = loanRepository.findLoanByClientId(emailMessage.getClient().getId());
                                HashMap<String, String> reportParams = replaceStretchyParamsWithActualClientParams(reportStretchyParams,
                                        emailMessage.getClient());
                                for (final Loan loan : loans) {
                                    if (loan.isOpen()) {
                                        if (reportStretchyParams.containsKey("selectLoan")) {
                                            reportParams.put("SelectLoan", loan.getId().toString());
                                        } else if (reportStretchyParams.containsKey("loanId")) {
                                            reportParams.put("loanId", loan.getId().toString());
                                        }
                                        File file = generateAttachments(emailCampaign, emailAttachmentFileFormat, reportParams, reportName,
                                                errorLog);
                                        if (file != null) {
                                            attachmentList.add(file);
                                        } else {
                                            errorLog.append(reportParams);
                                        }
                                    }
                                }
                            }
                        } else if (reportStretchyParams.containsKey("savingId")) {
                            if (emailMessage.getClient() != null) {
                                final List<SavingsAccount> savingsAccounts = savingsAccountRepository
                                        .findSavingAccountByClientId(emailMessage.getClient().getId());
                                HashMap<String, String> reportParams = replaceStretchyParamsWithActualClientParams(reportStretchyParams,
                                        emailMessage.getClient());
                                for (final SavingsAccount savingsAccount : savingsAccounts) {
                                    if (savingsAccount.isActive()) {
                                        reportParams.put("savingId", savingsAccount.getId().toString());
                                        File file = generateAttachments(emailCampaign, emailAttachmentFileFormat, reportParams, reportName,
                                                errorLog);
                                        if (file != null) {
                                            attachmentList.add(file);
                                        } else {
                                            errorLog.append(reportParams);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (emailMessage.getClient() != null) {
                                HashMap<String, String> reportParams = replaceStretchyParamsWithActualClientParams(reportStretchyParams,
                                        emailMessage.getClient());
                                File file = generateAttachments(emailCampaign, emailAttachmentFileFormat, reportParams, reportName,
                                        errorLog);
                                if (file != null) {
                                    attachmentList.add(file);
                                } else {
                                    errorLog.append(reportParams);
                                }
                            }
                        }
                    }
                    final EmailMessageWithAttachmentData emailMessageWithAttachmentData = EmailMessageWithAttachmentData.createNew(
                            emailMessage.getEmailAddress(), emailMessage.getMessage(), emailMessage.getEmailSubject(), attachmentList);
                    try {
                        emailMessageJobEmailService.sendEmailWithAttachment(emailMessageWithAttachmentData);
                        emailMessage.setStatusType(EmailMessageStatusType.SENT.getValue());
                        emailMessageRepository.save(emailMessage);
                    } catch (Exception e) {
                        emailMessage.setErrorMessage(e.getMessage());
                        emailMessage.setStatusType(EmailMessageStatusType.FAILED.getValue());
                        emailMessageRepository.save(emailMessage);
                    }
                }
            }
        }
        return RepeatStatus.FINISHED;
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

    private HashMap<String, String> replaceStretchyParamsWithActualClientParams(final HashMap<String, String> stretchyParams,
            final Client client) {
        HashMap<String, String> actualParams = new HashMap<>();
        for (Map.Entry<String, String> entry : stretchyParams.entrySet()) {
            switch (entry.getKey()) {
                case "selectOffice":
                    if (client.getStaff() != null) {
                        actualParams.put(entry.getKey(), client.getStaff().officeId().toString());
                    } else {
                        actualParams.put(entry.getKey(), client.getOffice().getId().toString());
                    }
                break;
                case "selectClient":
                    actualParams.put(entry.getKey(), client.getId().toString());
                break;
                case "selectLoanofficer":
                    actualParams.put(entry.getKey(), client.getStaff().getId().toString());
                break;
                case "environementUrl":
                    actualParams.put(entry.getKey(), entry.getKey());
                break;
                default:
                    log.warn("Query parameter could not be mapped: {}", entry.getKey());
            }
        }
        return actualParams;
    }

    private File generateAttachments(final EmailCampaign emailCampaign, final ScheduledEmailAttachmentFileFormat emailAttachmentFileFormat,
            final Map<String, String> reportParams, final String reportName, final StringBuilder errorLog) {
        if (reportName == null) {
            return null;
        }
        try {
            final ByteArrayOutputStream byteArrayOutputStream = readReportingService.generatePentahoReportAsOutputStream(reportName,
                    emailAttachmentFileFormat.getValue(), reportParams, null, emailCampaign.getApprovedBy(), errorLog);
            final String fileLocation = fineractProperties.getContent().getFilesystem().getRootFolder() + File.separator + "";
            final String fileNameWithoutExtension = fileLocation + File.separator + reportName;
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
            errorLog.append("The ReportMailingJobWritePlatformServiceImpl.executeReportMailingJobs threw an IOException " + "exception: ")
                    .append(e.getMessage()).append(" ---------- ");
        }
        return null;
    }
}
