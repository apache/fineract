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
package org.apache.fineract.notification.service;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.eventandlistener.NotificationEventPublisher;
import org.apache.fineract.portfolio.businessevent.BusinessEventListener;
import org.apache.fineract.portfolio.businessevent.domain.client.ClientCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.deposit.FixedDepositAccountCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.deposit.RecurringDepositAccountCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.group.CentersCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.group.GroupsCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.loan.LoanApprovedBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.loan.LoanCloseAsRescheduleBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.loan.LoanCloseBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.loan.LoanCreatedBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.loan.product.LoanProductCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.loan.transaction.LoanTransactionMakeRepaymentPostBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.savings.SavingsApproveBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.savings.SavingsCloseBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.savings.SavingsCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.savings.SavingsPostInterestBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.savings.transaction.SavingsDepositBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.share.ShareAccountApproveBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.share.ShareAccountCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.share.ShareProductDividentsCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDomainServiceImpl implements NotificationDomainService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final PlatformSecurityContext context;
    private final NotificationEventPublisher notificationEventPublisher;
    private final AppUserRepository appUserRepository;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(ClientCreateBusinessEvent.class, new ClientCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsApproveBusinessEvent.class, new SavingsAccountApprovedListener());
        businessEventNotifierService.addPostBusinessEventListener(CentersCreateBusinessEvent.class, new CenterCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(GroupsCreateBusinessEvent.class, new GroupCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsDepositBusinessEvent.class, new SavingsAccountDepositListener());
        businessEventNotifierService.addPostBusinessEventListener(ShareProductDividentsCreateBusinessEvent.class,
                new ShareProductDividendCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(FixedDepositAccountCreateBusinessEvent.class,
                new FixedDepositAccountCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(RecurringDepositAccountCreateBusinessEvent.class,
                new RecurringDepositAccountCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsPostInterestBusinessEvent.class,
                new SavingsPostInterestListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanCreatedBusinessEvent.class, new LoanCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanApprovedBusinessEvent.class, new LoanApprovedListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanCloseBusinessEvent.class, new LoanClosedListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanCloseAsRescheduleBusinessEvent.class,
                new LoanCloseAsRescheduledListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanTransactionMakeRepaymentPostBusinessEvent.class,
                new LoanMakeRepaymentListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanProductCreateBusinessEvent.class, new LoanProductCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsCreateBusinessEvent.class, new SavingsAccountCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(SavingsCloseBusinessEvent.class, new SavingsAccountClosedListener());
        businessEventNotifierService.addPostBusinessEventListener(ShareAccountCreateBusinessEvent.class, new ShareAccountCreatedListener());
        businessEventNotifierService.addPostBusinessEventListener(ShareAccountApproveBusinessEvent.class,
                new ShareAccountApprovedListener());
    }

    private class ClientCreatedListener implements BusinessEventListener<ClientCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(ClientCreateBusinessEvent event) {
            Client client = event.get();
            buildNotification("ACTIVATE_CLIENT", "client", client.getId(), "New client created", "created",
                    context.authenticatedUser().getId(), client.getOffice().getId());
        }
    }

    private class CenterCreatedListener implements BusinessEventListener<CentersCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(CentersCreateBusinessEvent event) {
            CommandProcessingResult commandProcessingResult = event.get();
            buildNotification("ACTIVATE_CENTER", "center", commandProcessingResult.getGroupId(), "New center created", "created",
                    context.authenticatedUser().getId(), commandProcessingResult.getOfficeId());
        }
    }

    private class GroupCreatedListener implements BusinessEventListener<GroupsCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(GroupsCreateBusinessEvent event) {
            CommandProcessingResult commandProcessingResult = event.get();
            buildNotification("ACTIVATE_GROUP", "group", commandProcessingResult.getGroupId(), "New group created", "created",
                    context.authenticatedUser().getId(), commandProcessingResult.getOfficeId());
        }
    }

    private class SavingsAccountDepositListener implements BusinessEventListener<SavingsDepositBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsDepositBusinessEvent event) {
            SavingsAccountTransaction savingsAccountTransaction = event.get();
            buildNotification("READ_SAVINGSACCOUNT", "savingsAccount", savingsAccountTransaction.getSavingsAccount().getId(),
                    "Deposit made", "depositMade", context.authenticatedUser().getId(),
                    savingsAccountTransaction.getSavingsAccount().officeId());
        }
    }

    private class ShareProductDividendCreatedListener implements BusinessEventListener<ShareProductDividentsCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(ShareProductDividentsCreateBusinessEvent event) {
            Long shareProductId = event.get();
            buildNotification("READ_DIVIDEND_SHAREPRODUCT", "shareProduct", shareProductId, "Dividend posted to account", "dividendPosted",
                    context.authenticatedUser().getId(), context.authenticatedUser().getOffice().getId());
        }
    }

    private class FixedDepositAccountCreatedListener implements BusinessEventListener<FixedDepositAccountCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(FixedDepositAccountCreateBusinessEvent event) {
            FixedDepositAccount fixedDepositAccount = event.get();
            buildNotification("APPROVE_FIXEDDEPOSITACCOUNT", "fixedDeposit", fixedDepositAccount.getId(),
                    "New fixed deposit account created", "created", context.authenticatedUser().getId(), fixedDepositAccount.officeId());
        }
    }

    private class RecurringDepositAccountCreatedListener implements BusinessEventListener<RecurringDepositAccountCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(RecurringDepositAccountCreateBusinessEvent event) {
            RecurringDepositAccount recurringDepositAccount = event.get();
            buildNotification("APPROVE_RECURRINGDEPOSITACCOUNT", "recurringDepositAccount", recurringDepositAccount.getId(),
                    "New recurring deposit account created", "created", context.authenticatedUser().getId(),
                    recurringDepositAccount.officeId());
        }
    }

    private class SavingsAccountApprovedListener implements BusinessEventListener<SavingsApproveBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsApproveBusinessEvent event) {
            SavingsAccount savingsAccount = event.get();
            if (savingsAccount.depositAccountType().equals(DepositAccountType.FIXED_DEPOSIT)) {

                buildNotification("ACTIVATE_FIXEDDEPOSITACCOUNT", "fixedDeposit", savingsAccount.getId(), "Fixed deposit account approved",
                        "approved", context.authenticatedUser().getId(), savingsAccount.officeId());
            } else if (savingsAccount.depositAccountType().equals(DepositAccountType.RECURRING_DEPOSIT)) {

                buildNotification("ACTIVATE_RECURRINGDEPOSITACCOUNT", "recurringDepositAccount", savingsAccount.getId(),
                        "Recurring deposit account approved", "approved", context.authenticatedUser().getId(), savingsAccount.officeId());
            } else if (savingsAccount.depositAccountType().equals(DepositAccountType.SAVINGS_DEPOSIT)) {

                buildNotification("ACTIVATE_SAVINGSACCOUNT", "savingsAccount", savingsAccount.getId(), "Savings account approved",
                        "approved", context.authenticatedUser().getId(), savingsAccount.officeId());
            }
        }
    }

    private class SavingsPostInterestListener implements BusinessEventListener<SavingsPostInterestBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsPostInterestBusinessEvent event) {
            SavingsAccount savingsAccount = event.get();
            buildNotification("READ_SAVINGSACCOUNT", "savingsAccount", savingsAccount.getId(), "Interest posted to account",
                    "interestPosted", context.authenticatedUser().getId(), savingsAccount.officeId());
        }
    }

    private class LoanCreatedListener implements BusinessEventListener<LoanCreatedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanCreatedBusinessEvent event) {
            Loan loan = event.get();
            buildNotification("APPROVE_LOAN", "loan", loan.getId(), "New loan created", "created", context.authenticatedUser().getId(),
                    loan.getOfficeId());
        }
    }

    private class LoanApprovedListener implements BusinessEventListener<LoanApprovedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanApprovedBusinessEvent event) {
            Loan loan = event.get();
            buildNotification("DISBURSE_LOAN", "loan", loan.getId(), "New loan approved", "approved", context.authenticatedUser().getId(),
                    loan.getOfficeId());
        }
    }

    private class LoanClosedListener implements BusinessEventListener<LoanCloseBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanCloseBusinessEvent event) {
            Loan loan = event.get();
            buildNotification("READ_LOAN", "loan", loan.getId(), "Loan closed", "loanClosed", context.authenticatedUser().getId(),
                    loan.getOfficeId());
        }
    }

    private class LoanCloseAsRescheduledListener implements BusinessEventListener<LoanCloseAsRescheduleBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanCloseAsRescheduleBusinessEvent event) {
            Loan loan = event.get();
            buildNotification("READ_Rescheduled Loans", "loan", loan.getId(), "Loan has been rescheduled", "loanRescheduled",
                    context.authenticatedUser().getId(), loan.getOfficeId());
        }
    }

    private class LoanMakeRepaymentListener implements BusinessEventListener<LoanTransactionMakeRepaymentPostBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanTransactionMakeRepaymentPostBusinessEvent event) {
            Loan loan = event.get().getLoan();
            buildNotification("READ_LOAN", "loan", loan.getId(), "Repayment made", "repaymentMade", context.authenticatedUser().getId(),
                    loan.getOfficeId());
        }
    }

    private class LoanProductCreatedListener implements BusinessEventListener<LoanProductCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanProductCreateBusinessEvent event) {
            LoanProduct loanProduct = event.get();
            buildNotification("READ_LOANPRODUCT", "loanProduct", loanProduct.getId(), "New loan product created", "created",
                    context.authenticatedUser().getId(), context.authenticatedUser().getOffice().getId());
        }
    }

    private class SavingsAccountCreatedListener implements BusinessEventListener<SavingsCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsCreateBusinessEvent event) {
            SavingsAccount savingsAccount = event.get();
            buildNotification("APPROVE_SAVINGSACCOUNT", "savingsAccount", savingsAccount.getId(), "New savings account created", "created",
                    context.authenticatedUser().getId(), savingsAccount.officeId());
        }
    }

    private class SavingsAccountClosedListener implements BusinessEventListener<SavingsCloseBusinessEvent> {

        @Override
        public void onBusinessEvent(SavingsCloseBusinessEvent event) {
            SavingsAccount savingsAccount = event.get();
            buildNotification("READ_SAVINGSACCOUNT", "savingsAccount", savingsAccount.getId(), "Savings has gone into dormant", "closed",
                    context.authenticatedUser().getId(), savingsAccount.officeId());
        }
    }

    private class ShareAccountCreatedListener implements BusinessEventListener<ShareAccountCreateBusinessEvent> {

        @Override
        public void onBusinessEvent(ShareAccountCreateBusinessEvent event) {
            ShareAccount shareAccount = event.get();
            buildNotification("APPROVE_SHAREACCOUNT", "shareAccount", shareAccount.getId(), "New share account created", "created",
                    context.authenticatedUser().getId(), shareAccount.getOfficeId());
        }
    }

    private class ShareAccountApprovedListener implements BusinessEventListener<ShareAccountApproveBusinessEvent> {

        @Override
        public void onBusinessEvent(ShareAccountApproveBusinessEvent event) {
            ShareAccount shareAccount = event.get();
            buildNotification("ACTIVATE_SHAREACCOUNT", "shareAccount", shareAccount.getId(), "Share account approved", "approved",
                    context.authenticatedUser().getId(), shareAccount.getOfficeId());
        }
    }

    private void buildNotification(String permission, String objectType, Long objectIdentifier, String notificationContent,
            String eventType, Long appUserId, Long officeId) {

        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        Set<Long> userIds = getNotifiableUserIds(officeId, permission);
        NotificationData notificationData = new NotificationData(objectType, objectIdentifier, eventType, appUserId, notificationContent,
                false, false, tenantIdentifier, officeId, userIds);
        try {
            notificationEventPublisher.broadcastNotification(notificationData);
        } catch (Exception e) {
            // We want to avoid rethrowing the exception to stop the business transaction from rolling back
            log.error("Error while broadcasting notification event", e);
        }
    }

    private Set<Long> getNotifiableUserIds(Long officeId, String permission) {
        Collection<AppUser> users = appUserRepository.findByOfficeId(officeId);
        Collection<AppUser> usersWithPermission = users.stream().filter(aU -> aU.hasAnyPermission(permission, "ALL_FUNCTIONS")).toList();
        return usersWithPermission.stream().map(AppUser::getId).collect(toSet());
    }
}
