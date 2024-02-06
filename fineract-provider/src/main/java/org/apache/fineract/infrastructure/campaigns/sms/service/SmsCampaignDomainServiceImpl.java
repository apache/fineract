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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaignRepository;
import org.apache.fineract.infrastructure.campaigns.sms.exception.SmsRuntimeException;
import org.apache.fineract.infrastructure.campaigns.sms.serialization.SmsCampaignValidator;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientActivateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientRejectBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanApprovedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanRejectedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionMakeRepaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.SavingsActivateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.SavingsRejectBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.transaction.SavingsDepositBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.transaction.SavingsWithdrawalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.office.exception.OfficeNotFoundException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTypeException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsCampaignDomainServiceImpl implements SmsCampaignDomainService {

    private final SmsCampaignRepository smsCampaignRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final OfficeRepository officeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final SmsCampaignWritePlatformService smsCampaignWritePlatformCommandHandler;
    private final GroupRepository groupRepository;

    private final SmsMessageScheduledJobService smsMessageScheduledJobService;
    private final SmsCampaignValidator smsCampaignValidator;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(LoanApprovedBusinessEvent.class, new SendSmsOnLoanApproved());
        businessEventNotifierService.addPostBusinessEventListener(LoanRejectedBusinessEvent.class, new SendSmsOnLoanRejected());
        businessEventNotifierService.addPostBusinessEventListener(LoanTransactionMakeRepaymentPostBusinessEvent.class,
                new SendSmsOnLoanRepayment());
        businessEventNotifierService.addPostBusinessEventListener(ClientActivateBusinessEvent.class, new ClientActivatedListener());
        businessEventNotifierService.addPostBusinessEventListener(ClientRejectBusinessEvent.class, new ClientRejectedListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsActivateBusinessEvent.class,
                new SavingsAccountActivatedListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsRejectBusinessEvent.class, new SavingsAccountRejectedListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsDepositBusinessEvent.class,
                new DepositSavingsAccountTransactionListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsWithdrawalBusinessEvent.class,
                new NonDepositSavingsAccountTransactionListener());
    }

    private void notifyRejectedLoanOwner(Loan loan) {
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Loan Rejected");
        if (smsCampaigns.size() > 0) {
            for (SmsCampaign campaign : smsCampaigns) {
                if (campaign.isActive()) {
                    SmsCampaignDomainServiceImpl.this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(loan,
                            campaign);
                }
            }
        }
    }

    private void notifyAcceptedLoanOwner(Loan loan) {
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Loan Approved");
        if (smsCampaigns.size() > 0) {
            for (SmsCampaign campaign : smsCampaigns) {
                this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(loan, campaign);
            }
        }
    }

    private void notifyClientActivated(final Client client) {
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Client Activated");
        if (smsCampaigns.size() > 0) {
            for (SmsCampaign campaign : smsCampaigns) {
                this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(client, campaign);
            }
        }

    }

    private void notifyClientRejected(final Client client) {
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Client Rejected");
        if (smsCampaigns.size() > 0) {
            for (SmsCampaign campaign : smsCampaigns) {
                this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(client, campaign);
            }
        }

    }

    private void notifySavingsAccountActivated(final SavingsAccount savingsAccount) {
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Savings Activated");
        if (smsCampaigns.size() > 0) {
            for (SmsCampaign campaign : smsCampaigns) {
                this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(savingsAccount, campaign);
            }
        }

    }

    private void notifySavingsAccountRejected(final SavingsAccount savingsAccount) {
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Savings Rejected");
        if (smsCampaigns.size() > 0) {
            for (SmsCampaign campaign : smsCampaigns) {
                this.smsCampaignWritePlatformCommandHandler.insertDirectCampaignIntoSmsOutboundTable(savingsAccount, campaign);
            }
        }

    }

    private void sendSmsForLoanRepayment(LoanTransaction loanTransaction) {
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns("Loan Repayment");
        if (smsCampaigns.size() > 0) {
            for (SmsCampaign smsCampaign : smsCampaigns) {
                try {
                    Loan loan = loanTransaction.getLoan();
                    final Set<Client> groupClients = new HashSet<>();
                    if (loan.hasInvalidLoanType()) {
                        throw new InvalidLoanTypeException("Loan Type cannot be Invalid for the Triggered Sms Campaign");
                    }
                    if (loan.isGroupLoan()) {
                        Group group = this.groupRepository.findById(loan.getGroupId())
                                .orElseThrow(() -> new GroupNotFoundException(loan.getGroupId()));
                        groupClients.addAll(group.getClientMembers());
                    } else {
                        groupClients.add(loan.client());
                    }
                    HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                            new TypeReference<>() {

                            });

                    if (!groupClients.isEmpty()) {
                        for (Client client : groupClients) {
                            HashMap<String, Object> smsParams = processRepaymentDataForSms(loanTransaction, client);
                            for (Map.Entry<String, String> entry : campaignParams.entrySet()) {
                                String value = entry.getValue();
                                String spvalue = null;
                                boolean spkeycheck = smsParams.containsKey(entry.getKey());
                                if (spkeycheck) {
                                    spvalue = smsParams.get(entry.getKey()).toString();
                                }
                                if (spkeycheck && !(value.equals("-1") || spvalue.equals(value))) {
                                    if (entry.getKey().equals("officeId")) {
                                        Long officeId = Long.valueOf(value);
                                        Office campaignOffice = this.officeRepository.findById(Long.valueOf(value))
                                                .orElseThrow(() -> new OfficeNotFoundException(officeId));
                                        if (campaignOffice.doesNotHaveAnOfficeInHierarchyWithId(client.getOffice().getId())) {
                                            throw new SmsRuntimeException("error.msg.no.office", "Office not found for the id");
                                        }
                                    } else {
                                        throw new SmsRuntimeException("error.msg.no.id.attribute", "Office Id attribute is notfound");
                                    }
                                }
                            }
                            String message = this.smsCampaignWritePlatformCommandHandler.compileSmsTemplate(smsCampaign.getMessage(),
                                    smsCampaign.getCampaignName(), smsParams);
                            Object mobileNo = smsParams.get("mobileNo");
                            if (this.smsCampaignValidator.isValidNotificationOrSms(client, smsCampaign, mobileNo)) {
                                String mobileNumber = null;
                                if (mobileNo != null) {
                                    mobileNumber = mobileNo.toString();
                                }
                                SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, message, mobileNumber, smsCampaign,
                                        smsCampaign.isNotification());
                                Map<SmsCampaign, Collection<SmsMessage>> smsDataMap = new HashMap<>();
                                smsDataMap.put(smsCampaign, Collections.singletonList(smsMessage));
                                this.smsMessageScheduledJobService.sendTriggeredMessages(smsDataMap);
                            }
                        }
                    }
                } catch (final IOException e) {
                    log.error("smsParams does not contain the key: ", e);
                } catch (final RuntimeException e) {
                    log.debug("Client Office Id and SMS Campaign Office id doesn't match ", e);
                }
            }
        }
    }

    private void sendSmsForSavingsTransaction(final SavingsAccountTransaction savingsTransaction, boolean isDeposit) {
        String campaignName = isDeposit ? "Savings Deposit" : "Savings Withdrawal";
        List<SmsCampaign> smsCampaigns = retrieveSmsCampaigns(campaignName);
        if (smsCampaigns.size() > 0) {
            for (SmsCampaign smsCampaign : smsCampaigns) {
                try {
                    final SavingsAccount savingsAccount = savingsTransaction.getSavingsAccount();
                    final Client client = savingsAccount.getClient();
                    HashMap<String, String> campaignParams = new ObjectMapper().readValue(smsCampaign.getParamValue(),
                            new TypeReference<>() {

                            });
                    HashMap<String, Object> smsParams = processSavingsTransactionDataForSms(savingsTransaction, client);
                    for (Map.Entry<String, String> entry : campaignParams.entrySet()) {
                        String value = entry.getValue();
                        String spvalue = null;
                        boolean spkeycheck = smsParams.containsKey(entry.getKey());
                        if (spkeycheck) {
                            spvalue = smsParams.get(entry.getKey()).toString();
                        }
                        if (spkeycheck && !(value.equals("-1") || spvalue.equals(value))) {
                            if (entry.getKey().equals("officeId")) {
                                Long officeId = Long.valueOf(value);
                                Office campaignOffice = this.officeRepository.findById(officeId)
                                        .orElseThrow(() -> new OfficeNotFoundException(officeId));
                                if (campaignOffice.doesNotHaveAnOfficeInHierarchyWithId(client.getOffice().getId())) {
                                    throw new SmsRuntimeException("error.msg.no.office", "Office not found for the id");
                                }
                            } else {
                                throw new SmsRuntimeException("error.msg.no.id.attribute", "Office Id attribute is notfound");
                            }
                        }
                    }
                    String message = this.smsCampaignWritePlatformCommandHandler.compileSmsTemplate(smsCampaign.getMessage(),
                            smsCampaign.getCampaignName(), smsParams);
                    Object mobileNo = smsParams.get("mobileNo");
                    if (this.smsCampaignValidator.isValidNotificationOrSms(client, smsCampaign, mobileNo)) {
                        String mobileNumber = null;
                        if (mobileNo != null) {
                            mobileNumber = mobileNo.toString();
                        }
                        SmsMessage smsMessage = SmsMessage.pendingSms(null, null, client, null, message, mobileNumber, smsCampaign,
                                smsCampaign.isNotification());
                        this.smsMessageRepository.save(smsMessage);
                        Collection<SmsMessage> messages = new ArrayList<>();
                        messages.add(smsMessage);
                        Map<SmsCampaign, Collection<SmsMessage>> smsDataMap = new HashMap<>();
                        smsDataMap.put(smsCampaign, messages);
                        this.smsMessageScheduledJobService.sendTriggeredMessages(smsDataMap);
                    }
                } catch (final IOException e) {
                    log.error("smsParams does not contain the key: ", e);
                } catch (final RuntimeException e) {
                    log.debug("Client Office Id and SMS Campaign Office id doesn't match ", e);
                }
            }
        }
    }

    private List<SmsCampaign> retrieveSmsCampaigns(String paramValue) {
        List<SmsCampaign> smsCampaigns = smsCampaignRepository.findActiveSmsCampaigns("%" + paramValue + "%",
                SmsCampaignTriggerType.TRIGGERED.getValue());
        return smsCampaigns;
    }

    private HashMap<String, Object> processRepaymentDataForSms(final LoanTransaction loanTransaction, Client groupClient) {

        HashMap<String, Object> smsParams = new HashMap<String, Object>();
        Loan loan = loanTransaction.getLoan();
        final Client client;
        if (loan.isGroupLoan() && groupClient != null) {
            client = groupClient;
        } else if (loan.isIndividualLoan()) {
            client = loan.getClient();
        } else {
            throw new InvalidParameterException("");
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM:d:yyyy");

        smsParams.put("id", loanTransaction.getLoan().getClientId());
        smsParams.put("firstname", client.getFirstname());
        smsParams.put("middlename", client.getMiddlename());
        smsParams.put("lastname", client.getLastname());
        smsParams.put("FullName", client.getDisplayName());
        smsParams.put("mobileNo", client.mobileNo());
        smsParams.put("LoanAmount", loan.getPrincipal());
        smsParams.put("LoanOutstanding", loanTransaction.getOutstandingLoanBalance());
        smsParams.put("loanId", loan.getId());
        smsParams.put("LoanAccountId", loan.getAccountNumber());
        smsParams.put("officeId", client.getOffice().getId());

        if (client.getStaff() != null) {
            smsParams.put("loanOfficerId", client.getStaff().getId());
        } else {
            smsParams.put("loanOfficerId", -1);
        }

        smsParams.put("repaymentAmount", loanTransaction.getAmount(loan.getCurrency()));
        smsParams.put("RepaymentDate", loanTransaction.getCreatedDateTime().toLocalDate().format(dateFormatter));
        smsParams.put("RepaymentTime", loanTransaction.getCreatedDateTime().toLocalTime().format(timeFormatter));

        if (loanTransaction.getPaymentDetail() != null) {
            smsParams.put("receiptNumber", loanTransaction.getPaymentDetail().getReceiptNumber());
        } else {
            smsParams.put("receiptNumber", -1);
        }
        return smsParams;
    }

    private HashMap<String, Object> processSavingsTransactionDataForSms(final SavingsAccountTransaction savingsAccountTransaction,
            Client client) {

        // {{savingsId}} {{id}} {{firstname}} {{middlename}} {{lastname}}
        // {{FullName}} {{mobileNo}} {{savingsAccountId}} {{depositAmount}}
        // {{balance}}

        // transactionDate
        HashMap<String, Object> smsParams = new HashMap<>();
        SavingsAccount savingsAccount = savingsAccountTransaction.getSavingsAccount();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM:d:yyyy");
        smsParams.put("clientId", client.getId());
        smsParams.put("firstname", client.getFirstname());
        smsParams.put("middlename", client.getMiddlename());
        smsParams.put("lastname", client.getLastname());
        smsParams.put("FullName", client.getDisplayName());
        smsParams.put("mobileNo", client.mobileNo());
        smsParams.put("savingsId", savingsAccount.getId());
        smsParams.put("savingsAccountNo", savingsAccount.getAccountNumber());
        smsParams.put("withdrawAmount", savingsAccountTransaction.getAmount(savingsAccount.getCurrency()));
        smsParams.put("depositAmount", savingsAccountTransaction.getAmount(savingsAccount.getCurrency()));
        smsParams.put("balance", savingsAccount.getWithdrawableBalance());
        smsParams.put("officeId", client.getOffice().getId());
        smsParams.put("transactionDate", savingsAccountTransaction.getTransactionDate().format(dateFormatter));
        smsParams.put("savingsTransactionId", savingsAccountTransaction.getId());

        if (client.getStaff() != null) {
            smsParams.put("loanOfficerId", client.getStaff().getId());
        } else {
            smsParams.put("loanOfficerId", -1);
        }

        if (savingsAccountTransaction.getPaymentDetail() != null) {
            smsParams.put("receiptNumber", savingsAccountTransaction.getPaymentDetail().getReceiptNumber());
        } else {
            smsParams.put("receiptNumber", -1);
        }
        return smsParams;
    }

    private final class SendSmsOnLoanApproved implements BusinessEventListener<LoanApprovedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanApprovedBusinessEvent event) {
            Loan loan = event.get();
            notifyAcceptedLoanOwner(loan);
        }
    }

    private final class SendSmsOnLoanRejected implements BusinessEventListener<LoanRejectedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanRejectedBusinessEvent event) {
            Loan loan = event.get();
            notifyRejectedLoanOwner(loan);
        }
    }

    private final class SendSmsOnLoanRepayment implements BusinessEventListener<LoanTransactionMakeRepaymentPostBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanTransactionMakeRepaymentPostBusinessEvent event) {
            sendSmsForLoanRepayment(event.get());
        }
    }

    private final class ClientActivatedListener implements BusinessEventListener<ClientActivateBusinessEvent> {

        @Override
        public void onBusinessEvent(ClientActivateBusinessEvent event) {
            notifyClientActivated(event.get());
        }
    }

    private final class ClientRejectedListener implements BusinessEventListener<ClientRejectBusinessEvent> {

        @Override
        public void onBusinessEvent(ClientRejectBusinessEvent event) {
            notifyClientRejected(event.get());
        }
    }

    private final class SavingsAccountActivatedListener implements BusinessEventListener<SavingsActivateBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsActivateBusinessEvent event) {
            notifySavingsAccountActivated(event.get());
        }
    }

    private final class SavingsAccountRejectedListener implements BusinessEventListener<SavingsRejectBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsRejectBusinessEvent event) {
            notifySavingsAccountRejected(event.get());
        }
    }

    private final class DepositSavingsAccountTransactionListener implements BusinessEventListener<SavingsDepositBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsDepositBusinessEvent event) {
            sendSmsForSavingsTransaction(event.get(), true);
        }
    }

    private final class NonDepositSavingsAccountTransactionListener implements BusinessEventListener<SavingsWithdrawalBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsWithdrawalBusinessEvent event) {
            sendSmsForSavingsTransaction(event.get(), false);
        }
    }
}
