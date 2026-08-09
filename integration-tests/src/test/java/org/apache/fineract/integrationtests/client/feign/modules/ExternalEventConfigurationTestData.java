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
package org.apache.fineract.integrationtests.client.feign.modules;

import java.util.Set;

/**
 * A deliberate duplicate of the server's event catalogue: adding or removing an external event type is expected to fail
 * {@code ExternalEventConfigurationIntegrationTest} until this set is updated to match.
 */
public final class ExternalEventConfigurationTestData {

    public static final String CENTERS_CREATE_EVENT = "CentersCreateBusinessEvent";
    public static final String CLIENT_ACTIVATE_EVENT = "ClientActivateBusinessEvent";

    public static final Set<String> DEFAULT_DISABLED_EVENT_TYPES = Set.of(//
            CENTERS_CREATE_EVENT, //
            CLIENT_ACTIVATE_EVENT, //
            "ClientCloseBusinessEvent", //
            "ClientCreateBusinessEvent", //
            "ClientReactivateBusinessEvent", //
            "ClientRejectBusinessEvent", //
            "ClientUndoRejectionBusinessEvent", //
            "ClientUndoWithdrawalBusinessEvent", //
            "ClientWithdrawBusinessEvent", //
            "DocumentCreatedBusinessEvent", //
            "DocumentDeletedBusinessEvent", //
            "FixedDepositAccountCreateBusinessEvent", //
            "GroupsCreateBusinessEvent", //
            "LoanAcceptTransferBusinessEvent", //
            "LoanAddChargeBusinessEvent", //
            "LoanAdjustTransactionBusinessEvent", //
            "LoanApplicationModifiedBusinessEvent", //
            "LoanApplyOverdueChargeBusinessEvent", //
            "LoanApprovedBusinessEvent", //
            "LoanBalanceChangedBusinessEvent", //
            "LoanChargebackTransactionBusinessEvent", //
            "LoanChargePaymentPostBusinessEvent", //
            "LoanChargePaymentPreBusinessEvent", //
            "LoanChargeRefundBusinessEvent", //
            "LoanCloseAsRescheduleBusinessEvent", //
            "LoanCloseBusinessEvent", //
            "LoanCreatedBusinessEvent", //
            "LoanCreditBalanceRefundPostBusinessEvent", //
            "LoanCreditBalanceRefundPreBusinessEvent", //
            "LoanDeleteChargeBusinessEvent", //
            "LoanDisbursalBusinessEvent", //
            "LoanDisbursalTransactionBusinessEvent", //
            "WorkingCapitalLoanDisbursalTransactionBusinessEvent", //
            "WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent", //
            "LoanForeClosurePostBusinessEvent", //
            "LoanForeClosurePreBusinessEvent", //
            "LoanInitiateTransferBusinessEvent", //
            "LoanInterestRecalculationBusinessEvent", //
            "LoanProductCreateBusinessEvent", //
            "LoanReassignOfficerBusinessEvent", //
            "LoanRefundPostBusinessEvent", //
            "LoanRefundPreBusinessEvent", //
            "LoanRejectedBusinessEvent", //
            "LoanRejectTransferBusinessEvent", //
            "LoanRemoveOfficerBusinessEvent", //
            "LoanRepaymentDueBusinessEvent", //
            "LoanRepaymentOverdueBusinessEvent", //
            "LoanRescheduledDueCalendarChangeBusinessEvent", //
            "LoanRescheduledDueHolidayBusinessEvent", //
            "LoanScheduleVariationsAddedBusinessEvent", //
            "LoanScheduleVariationsDeletedBusinessEvent", //
            "LoanStatusChangedBusinessEvent", //
            "LoanTransactionGoodwillCreditPostBusinessEvent", //
            "LoanTransactionGoodwillCreditPreBusinessEvent", //
            "LoanTransactionMakeRepaymentPostBusinessEvent", //
            "LoanTransactionMakeRepaymentPreBusinessEvent", //
            "WorkingCapitalLoanRepaymentTransactionBusinessEvent", //
            "WorkingCapitalLoanCreditBalanceRefundTransactionBusinessEvent", //
            "LoanTransactionMerchantIssuedRefundPostBusinessEvent", //
            "LoanTransactionMerchantIssuedRefundPreBusinessEvent", //
            "LoanTransactionPayoutRefundPostBusinessEvent", //
            "LoanTransactionPayoutRefundPreBusinessEvent", //
            "LoanTransactionRecoveryPaymentPostBusinessEvent", //
            "LoanTransactionRecoveryPaymentPreBusinessEvent", //
            "LoanUndoApprovalBusinessEvent", //
            "LoanUndoDisbursalBusinessEvent", //
            "LoanUndoLastDisbursalBusinessEvent", //
            "LoanUndoWrittenOffBusinessEvent", //
            "LoanUpdateChargeBusinessEvent", //
            "LoanUpdateDisbursementDataBusinessEvent", //
            "LoanWaiveChargeBusinessEvent", //
            "LoanWaiveChargeUndoBusinessEvent", //
            "LoanWaiveInterestBusinessEvent", //
            "LoanWithdrawnByApplicantBusinessEvent", //
            "LoanWithdrawTransferBusinessEvent", //
            "LoanWrittenOffPostBusinessEvent", //
            "LoanWrittenOffPreBusinessEvent", //
            "RecurringDepositAccountCreateBusinessEvent", //
            "SavingsActivateBusinessEvent", //
            "SavingsApproveBusinessEvent", //
            "SavingsCloseBusinessEvent", //
            "SavingsCreateBusinessEvent", //
            "SavingsDepositBusinessEvent", //
            "SavingsPostInterestBusinessEvent", //
            "SavingsRejectBusinessEvent", //
            "SavingsWithdrawalBusinessEvent", //
            "ShareAccountApproveBusinessEvent", //
            "ShareAccountCreateBusinessEvent", //
            "ShareProductDividentsCreateBusinessEvent", //
            "LoanChargeAdjustmentPostBusinessEvent", //
            "LoanChargeAdjustmentPreBusinessEvent", //
            "LoanDelinquencyRangeChangeBusinessEvent", //
            "LoanAccountsStayedLockedBusinessEvent", //
            "LoanChargeOffPreBusinessEvent", //
            "LoanChargeOffPostBusinessEvent", //
            "LoanUndoChargeOffBusinessEvent", //
            "LoanAccrualTransactionCreatedBusinessEvent", //
            "LoanAccrualAdjustmentTransactionBusinessEvent", //
            "LoanRescheduledDueAdjustScheduleBusinessEvent", //
            "LoanOwnershipTransferBusinessEvent", //
            "LoanAccountSnapshotBusinessEvent", //
            "LoanTransactionDownPaymentPreBusinessEvent", //
            "LoanTransactionDownPaymentPostBusinessEvent", //
            "LoanAccountDelinquencyPauseChangedBusinessEvent", //
            "LoanAccountCustomSnapshotBusinessEvent", //
            "LoanReAgeTransactionBusinessEvent", //
            "LoanUndoReAgeTransactionBusinessEvent", //
            "LoanReAmortizeTransactionBusinessEvent", //
            "LoanUndoReAmortizeTransactionBusinessEvent", //
            "LoanReAgeBusinessEvent", //
            "LoanUndoReAgeBusinessEvent", //
            "LoanReAmortizeBusinessEvent", //
            "LoanUndoReAmortizeBusinessEvent", //
            "LoanTransactionInterestPaymentWaiverPostBusinessEvent", //
            "LoanTransactionInterestPaymentWaiverPreBusinessEvent", //
            "LoanTransactionAccrualActivityPostBusinessEvent", //
            "LoanTransactionAccrualActivityPreBusinessEvent", //
            "LoanTransactionInterestRefundPostBusinessEvent", //
            "LoanTransactionInterestRefundPreBusinessEvent", //
            "LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent", //
            "LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent", //
            "LoanTransactionContractTerminationPostBusinessEvent", //
            "LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent", //
            "LoanCapitalizedIncomeTransactionCreatedBusinessEvent", //
            "LoanUndoContractTerminationBusinessEvent", //
            "LoanBuyDownFeeTransactionCreatedBusinessEvent", //
            "LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent", //
            "LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent", //
            "LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent", //
            "LoanApprovedAmountChangedBusinessEvent", //
            "SavingsAccountsStayedLockedBusinessEvent", //
            "SavingsAccountForceWithdrawalBusinessEvent", //
            "WorkingCapitalLoanDiscountFeeTransactionBusinessEvent", //
            "WorkingCapitalLoanDiscountFeeAdjustmentTransactionBusinessEvent", //
            "WorkingCapitalLoanChargeAdjustmentPreBusinessEvent", //
            "WorkingCapitalLoanChargeAdjustmentPostBusinessEvent", //
            "WorkingCapitalLoanCreatedBusinessEvent", //
            "WorkingCapitalLoanApplicationModifiedBusinessEvent", //
            "WorkingCapitalLoanApprovedBusinessEvent", //
            "WorkingCapitalLoanUndoApprovalBusinessEvent", //
            "WorkingCapitalLoanRejectedBusinessEvent", //
            "WorkingCapitalLoanDisbursalBusinessEvent", //
            "WorkingCapitalLoanUndoDisbursalBusinessEvent", //
            "WorkingCapitalLoanStatusChangedBusinessEvent", //
            "WorkingCapitalLoanBalanceChangedBusinessEvent", //
            "WorkingCapitalLoanDelinquencyRangeChangeBusinessEvent", //
            "WorkingCapitalLoanWrittenOffBusinessEvent", //
            "WorkingCapitalLoanUndoWrittenOffBusinessEvent", //
            "WorkingCapitalLoanPeriodPaymentRateChangedBusinessEvent", //
            "WorkingCapitalLoanDelinquencyScheduleChangedBusinessEvent", //
            "WorkingCapitalLoanDelinquencyDisableBusinessEvent", //
            "WorkingCapitalLoanDelinquencyEnableBusinessEvent", //
            "WorkingCapitalLoanBreachScheduleChangedBusinessEvent", //
            "WorkingCapitalLoanBreachDisableBusinessEvent", //
            "WorkingCapitalLoanBreachEnableBusinessEvent", //
            "WorkingCapitalLoanChargeOffBusinessEvent", //
            "WorkingCapitalLoanFraudChangedBusinessEvent", //
            "WorkingCapitalLoanPayoutRefundTransactionBusinessEvent", //
            "WorkingCapitalLoanGoodwillCreditTransactionBusinessEvent", //
            "WorkingCapitalLoanTransactionReversedBusinessEvent", //
            "WorkingCapitalLoanChargeOffTransactionBusinessEvent", //
            "WorkingCapitalLoanDiscountFeeAmortizationTransactionBusinessEvent", //
            "WorkingCapitalLoanDiscountFeeAmortizationAdjustmentTransactionBusinessEvent", //
            "WorkingCapitalLoanAddChargeBusinessEvent", //
            "WorkingCapitalLoanJournalEntryCreatedBusinessEvent", //
            "WorkingCapitalLoanBreachPastDueChangeBusinessEvent", //
            "WorkingCapitalLoanUndoChargeOffBusinessEvent", //
            "WorkingCapitalLoanBreachChangeBusinessEvent", //
            "WorkingCapitalLoanNearBreachChangeBusinessEvent", //
            "WorkingCapitalLoanBreachPauseBusinessEvent", //
            "WorkingCapitalLoanBreachResumeBusinessEvent", //
            "WorkingCapitalLoanBreachRescheduleBusinessEvent", //
            "WorkingCapitalLoanBreachResetBusinessEvent", //
            "WorkingCapitalLoanBreachUndoResetBusinessEvent", //
            "WorkingCapitalLoanDelinquencyPauseBusinessEvent", //
            "WorkingCapitalLoanDelinquencyResumeBusinessEvent", //
            "WorkingCapitalLoanDelinquencyRescheduleBusinessEvent", //
            "WorkingCapitalLoanDelinquencyResetBusinessEvent", //
            "WorkingCapitalLoanDelinquencyUndoResetBusinessEvent"//
    );

    private ExternalEventConfigurationTestData() {}
}
