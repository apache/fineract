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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.jms.Queue;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.data.TopicSubscriberData;
import org.apache.fineract.notification.eventandlistener.NotificationEventService;
import org.apache.fineract.notification.eventandlistener.SpringEventPublisher;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BusinessEntity;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BusinessEvents;
import org.apache.fineract.portfolio.common.service.BusinessEventListener;
import org.apache.fineract.portfolio.common.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationDomainServiceImpl implements NotificationDomainService {

    private final BusinessEventNotifierService businessEventNotifierService;
    final PlatformSecurityContext context;
    private final RoleRepository roleRepository;
    private final OfficeRepository officeRepository;
    private final TopicSubscriberReadPlatformService topicSubscriberReadPlatformService;
    private final NotificationEventService notificationEvent;
    private final SpringEventPublisher springEventPublisher;

    @Autowired
    public NotificationDomainServiceImpl(final BusinessEventNotifierService businessEventNotifierService,
            final PlatformSecurityContext context, final RoleRepository roleRepository,
            final TopicSubscriberReadPlatformService topicSubscriberReadPlatformService, final OfficeRepository officeRepository,
            final NotificationEventService notificationEvent, final SpringEventPublisher springEventPublisher) {

        this.businessEventNotifierService = businessEventNotifierService;
        this.context = context;
        this.roleRepository = roleRepository;
        this.topicSubscriberReadPlatformService = topicSubscriberReadPlatformService;
        this.officeRepository = officeRepository;
        this.notificationEvent = notificationEvent;
        this.springEventPublisher = springEventPublisher;
    }

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.CLIENTS_CREATE, new ClientCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.SAVINGS_APPROVE, new SavingsAccountApprovedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.CENTERS_CREATE, new CenterCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.GROUPS_CREATE, new GroupCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.SAVINGS_DEPOSIT, new SavingsAccountDepositListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.SHARE_PRODUCT_DIVIDENDS_CREATE,
                new ShareProductDividendCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.FIXED_DEPOSIT_ACCOUNT_CREATE,
                new FixedDepositAccountCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.RECURRING_DEPOSIT_ACCOUNT_CREATE,
                new RecurringDepositAccountCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.SAVINGS_POST_INTEREST, new SavingsPostInterestListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_CREATE, new LoanCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_APPROVED, new LoanApprovedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_CLOSE, new LoanClosedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_CLOSE_AS_RESCHEDULE,
                new LoanCloseAsRescheduledListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_MAKE_REPAYMENT, new LoanMakeRepaymentListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.LOAN_PRODUCT_CREATE, new LoanProductCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.SAVINGS_CREATE, new SavingsAccountCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.SAVINGS_CLOSE, new SavingsAccountClosedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.SHARE_ACCOUNT_CREATE, new ShareAccountCreatedListener());
        businessEventNotifierService.addBusinessEventPostListeners(BusinessEvents.SHARE_ACCOUNT_APPROVE,
                new ShareAccountApprovedListener());
    }

    private abstract static class NotificationBusinessEventAdapter implements BusinessEventListener {

        @Override
        public void businessEventToBeExecuted(Map<BusinessEventNotificationConstants.BusinessEntity, Object> businessEventEntity) {}
    }

    private class ClientCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            Client client;
            Object entity = businessEventEntity.get(BusinessEventNotificationConstants.BusinessEntity.CLIENT);
            if (entity != null) {
                client = (Client) entity;
                buildNotification("ACTIVATE_CLIENT", "client", client.getId(), "New client created", "created",
                        context.authenticatedUser().getId(), client.getOffice().getId());
            }
        }
    }

    private class CenterCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEventNotificationConstants.BusinessEntity, Object> businessEventEntity) {
            CommandProcessingResult commandProcessingResult;
            Object entity = businessEventEntity.get(BusinessEntity.GROUP);
            if (entity != null) {
                commandProcessingResult = (CommandProcessingResult) entity;
                buildNotification("ACTIVATE_CENTER", "center", commandProcessingResult.getGroupId(), "New center created", "created",
                        context.authenticatedUser().getId(), commandProcessingResult.getOfficeId());
            }
        }
    }

    private class GroupCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEventNotificationConstants.BusinessEntity, Object> businessEventEntity) {
            CommandProcessingResult commandProcessingResult;
            Object entity = businessEventEntity.get(BusinessEntity.GROUP);
            if (entity != null) {
                commandProcessingResult = (CommandProcessingResult) entity;
                buildNotification("ACTIVATE_GROUP", "group", commandProcessingResult.getGroupId(), "New group created", "created",
                        context.authenticatedUser().getId(), commandProcessingResult.getOfficeId());
            }
        }
    }

    private class SavingsAccountDepositListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            SavingsAccountTransaction savingsAccountTransaction;
            Object entity = businessEventEntity.get(BusinessEntity.SAVINGS_TRANSACTION);
            if (entity != null) {
                savingsAccountTransaction = (SavingsAccountTransaction) entity;
                buildNotification("READ_SAVINGSACCOUNT", "savingsAccount", savingsAccountTransaction.getSavingsAccount().getId(),
                        "Deposit made", "depositMade", context.authenticatedUser().getId(),
                        savingsAccountTransaction.getSavingsAccount().officeId());
            }
        }
    }

    private class ShareProductDividendCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            Long shareProductId;
            Object entity = businessEventEntity.get(BusinessEntity.SHARE_PRODUCT);
            if (entity != null) {
                shareProductId = (Long) entity;
                buildNotification("READ_DIVIDEND_SHAREPRODUCT", "shareProduct", shareProductId, "Dividend posted to account",
                        "dividendPosted", context.authenticatedUser().getId(), context.authenticatedUser().getOffice().getId());
            }
        }
    }

    private class FixedDepositAccountCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            FixedDepositAccount fixedDepositAccount;
            Object entity = businessEventEntity.get(BusinessEntity.DEPOSIT_ACCOUNT);
            if (entity != null) {
                fixedDepositAccount = (FixedDepositAccount) entity;
                buildNotification("APPROVE_FIXEDDEPOSITACCOUNT", "fixedDeposit", fixedDepositAccount.getId(),
                        "New fixed deposit account created", "created", context.authenticatedUser().getId(),
                        fixedDepositAccount.officeId());
            }
        }
    }

    private class RecurringDepositAccountCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            RecurringDepositAccount recurringDepositAccount;
            Object entity = businessEventEntity.get(BusinessEntity.DEPOSIT_ACCOUNT);
            if (entity != null) {
                recurringDepositAccount = (RecurringDepositAccount) entity;
                buildNotification("APPROVE_RECURRINGDEPOSITACCOUNT", "recurringDepositAccount", recurringDepositAccount.getId(),
                        "New recurring deposit account created", "created", context.authenticatedUser().getId(),
                        recurringDepositAccount.officeId());
            }
        }
    }

    private class SavingsAccountApprovedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            SavingsAccount savingsAccount;
            Object entity = businessEventEntity.get(BusinessEntity.SAVING);
            if (entity != null) {
                savingsAccount = (SavingsAccount) entity;
                if (savingsAccount.depositAccountType().equals(DepositAccountType.FIXED_DEPOSIT)) {

                    buildNotification("ACTIVATE_FIXEDDEPOSITACCOUNT", "fixedDeposit", savingsAccount.getId(),
                            "Fixed deposit account approved", "approved", context.authenticatedUser().getId(), savingsAccount.officeId());
                } else if (savingsAccount.depositAccountType().equals(DepositAccountType.RECURRING_DEPOSIT)) {

                    buildNotification("ACTIVATE_RECURRINGDEPOSITACCOUNT", "recurringDepositAccount", savingsAccount.getId(),
                            "Recurring deposit account approved", "approved", context.authenticatedUser().getId(),
                            savingsAccount.officeId());
                } else if (savingsAccount.depositAccountType().equals(DepositAccountType.SAVINGS_DEPOSIT)) {

                    buildNotification("ACTIVATE_SAVINGSACCOUNT", "savingsAccount", savingsAccount.getId(), "Savings account approved",
                            "approved", context.authenticatedUser().getId(), savingsAccount.officeId());
                }
            }
        }
    }

    private class SavingsPostInterestListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            SavingsAccount savingsAccount;
            Object entity = businessEventEntity.get(BusinessEntity.SAVING);
            if (entity != null) {
                savingsAccount = (SavingsAccount) entity;
                buildNotification("READ_SAVINGSACCOUNT", "savingsAccount", savingsAccount.getId(), "Interest posted to account",
                        "interestPosted", context.authenticatedUser().getId(), savingsAccount.officeId());
            }
        }
    }

    private class LoanCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            Loan loan;
            Object entity = businessEventEntity.get(BusinessEntity.LOAN);
            if (entity != null) {
                loan = (Loan) entity;
                buildNotification("APPROVE_LOAN", "loan", loan.getId(), "New loan created", "created", context.authenticatedUser().getId(),
                        loan.getOfficeId());
            }

        }
    }

    private class LoanApprovedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            Loan loan;
            Object entity = businessEventEntity.get(BusinessEntity.LOAN);
            if (entity != null) {
                loan = (Loan) entity;
                buildNotification("DISBURSE_LOAN", "loan", loan.getId(), "New loan approved", "approved",
                        context.authenticatedUser().getId(), loan.getOfficeId());
            }
        }
    }

    private class LoanClosedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {

            Loan loan;
            Object entity = businessEventEntity.get(BusinessEntity.LOAN);
            if (entity != null) {
                loan = (Loan) entity;
                buildNotification("READ_LOAN", "loan", loan.getId(), "Loan closed", "loanClosed", context.authenticatedUser().getId(),
                        loan.getOfficeId());
            }
        }
    }

    private class LoanCloseAsRescheduledListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            Loan loan;
            Object entity = businessEventEntity.get(BusinessEntity.LOAN);
            if (entity != null) {
                loan = (Loan) entity;
                buildNotification("READ_Rescheduled Loans", "loan", loan.getId(), "Loan has been rescheduled", "loanRescheduled",
                        context.authenticatedUser().getId(), loan.getOfficeId());
            }
        }
    }

    private class LoanMakeRepaymentListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            Loan loan;
            Object entity = businessEventEntity.get(BusinessEntity.LOAN);
            if (entity != null) {
                loan = (Loan) entity;
                buildNotification("READ_LOAN", "loan", loan.getId(), "Repayment made", "repaymentMade", context.authenticatedUser().getId(),
                        loan.getOfficeId());
            }
        }
    }

    private class LoanProductCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {

            LoanProduct loanProduct;
            Object entity = businessEventEntity.get(BusinessEntity.LOAN_PRODUCT);
            if (entity != null) {
                loanProduct = (LoanProduct) entity;
                buildNotification("READ_LOANPRODUCT", "loanProduct", loanProduct.getId(), "New loan product created", "created",
                        context.authenticatedUser().getId(), context.authenticatedUser().getOffice().getId());
            }
        }
    }

    private class SavingsAccountCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            SavingsAccount savingsAccount;
            Object entity = businessEventEntity.get(BusinessEntity.SAVING);
            if (entity != null) {
                savingsAccount = (SavingsAccount) entity;
                buildNotification("APPROVE_SAVINGSACCOUNT", "savingsAccount", savingsAccount.getId(), "New savings account created",
                        "created", context.authenticatedUser().getId(), savingsAccount.officeId());
            }
        }
    }

    private class SavingsAccountClosedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            SavingsAccount savingsAccount;
            Object entity = businessEventEntity.get(BusinessEntity.SAVING);
            if (entity != null) {
                savingsAccount = (SavingsAccount) entity;
                buildNotification("READ_SAVINGSACCOUNT", "savingsAccount", savingsAccount.getId(), "Savings has gone into dormant",
                        "closed", context.authenticatedUser().getId(), savingsAccount.officeId());
            }
        }
    }

    private class ShareAccountCreatedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            ShareAccount shareAccount;
            Object entity = businessEventEntity.get(BusinessEntity.SHARE_ACCOUNT);
            if (entity != null) {
                shareAccount = (ShareAccount) entity;
                buildNotification("APPROVE_SHAREACCOUNT", "shareAccount", shareAccount.getId(), "New share account created", "created",
                        context.authenticatedUser().getId(), shareAccount.getOfficeId());
            }
        }
    }

    private class ShareAccountApprovedListener extends NotificationBusinessEventAdapter {

        @Override
        public void businessEventWasExecuted(Map<BusinessEntity, Object> businessEventEntity) {
            ShareAccount shareAccount;
            Object entity = businessEventEntity.get(BusinessEntity.SHARE_ACCOUNT);
            if (entity != null) {
                shareAccount = (ShareAccount) entity;
                buildNotification("ACTIVATE_SHAREACCOUNT", "shareAccount", shareAccount.getId(), "Share account approved", "approved",
                        context.authenticatedUser().getId(), shareAccount.getOfficeId());
            }
        }
    }

    private void buildNotification(String permission, String objectType, Long objectIdentifier, String notificationContent,
            String eventType, Long appUserId, Long officeId) {

        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        Queue queue = new ActiveMQQueue("NotificationQueue");
        List<Long> userIds = retrieveSubscribers(officeId, permission);
        NotificationData notificationData = new NotificationData(objectType, objectIdentifier, eventType, appUserId, notificationContent,
                false, false, tenantIdentifier, officeId, userIds);
        try {
            this.notificationEvent.broadcastNotification(queue, notificationData);
        } catch (Exception e) {
            this.springEventPublisher.broadcastNotification(notificationData);
        }
    }

    private List<Long> retrieveSubscribers(Long officeId, String permission) {

        Set<TopicSubscriberData> topicSubscribers = new HashSet<>();
        List<Long> subscriberIds = new ArrayList<>();
        Long entityId = officeId;
        String entityType = "";
        if (officeRepository.findById(entityId).get().getParent() == null) {
            entityType = "OFFICE";
        } else {
            entityType = "BRANCH";
        }
        List<Role> allRoles = roleRepository.findAll();
        for (Role curRole : allRoles) {
            if (curRole.hasPermissionTo(permission) || curRole.hasPermissionTo("ALL_FUNCTIONS")) {
                String memberType = curRole.getName();
                topicSubscribers.addAll(topicSubscriberReadPlatformService.getSubscribers(entityId, entityType, memberType));
            }
        }

        for (TopicSubscriberData topicSubscriber : topicSubscribers) {
            subscriberIds.add(topicSubscriber.getUserId());
        }
        return subscriberIds;
    }
}
