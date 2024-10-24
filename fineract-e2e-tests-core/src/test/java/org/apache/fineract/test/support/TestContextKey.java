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
package org.apache.fineract.test.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TestContextKey {

    public static final String CLIENT_CREATE_RESPONSE = "clientCreateResponse";
    public static final String CLIENT_CREATE_SECOND_CLIENT_RESPONSE = "clientCreateSecondClientResponse";
    public static final String LOAN_CREATE_RESPONSE = "loanCreateResponse";
    public static final String LOAN_CREATE_SECOND_LOAN_RESPONSE = "loanCreateSecondLoanResponse";
    public static final String LOAN_MODIFY_RESPONSE = "loanModifyResponse";
    public static final String ADD_DUE_DATE_CHARGE_RESPONSE = "addDueDateChargeResponse";
    public static final String ADD_PROCESSING_FEE_RESPONSE = "addProcessingFeeResponse";
    public static final String ADD_NSF_FEE_RESPONSE = "addNsfFeeResponse";
    public static final String WAIVE_CHARGE_RESPONSE = "waiveChargeResponse";
    public static final String UNDO_WAIVE_RESPONSE = "waiveNsfFeeResponse";
    public static final String LOAN_APPROVAL_RESPONSE = "loanApprovalResponse";
    public static final String LOAN_APPROVAL_SECOND_LOAN_RESPONSE = "loanApprovalSecondLoanResponse";
    public static final String LOAN_UNDO_APPROVAL_RESPONSE = "loanUndoApprovalResponse";
    public static final String LOAN_DISBURSE_RESPONSE = "loanDisburseResponse";
    public static final String LOAN_DISBURSE_SECOND_LOAN_RESPONSE = "loanDisburseSecondLoanResponse";
    public static final String LOAN_UNDO_DISBURSE_RESPONSE = "loanUndoDisburseResponse";
    public static final String LOAN_REPAYMENT_RESPONSE = "loanRepaymentResponse";
    public static final String LOAN_PAYMENT_TRANSACTION_RESPONSE = "loanPaymentTransactionResponse";
    public static final String LOAN_REFUND_RESPONSE = "loanRefundResponse";
    public static final String LOAN_REAGING_RESPONSE = "loanReAgingResponse";
    public static final String LOAN_REAGING_UNDO_RESPONSE = "loanReAgingUndoResponse";
    public static final String LOAN_REAMORTIZATION_RESPONSE = "loanReAmortizationResponse";
    public static final String LOAN_REAMORTIZATION_UNDO_RESPONSE = "loanReAmortizationUndoResponse";
    public static final String BUSINESS_DATE_RESPONSE = "businessDateResponse";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1 = "loanProductCreateResponseLP1";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_DUE_DATE = "loanProductCreateResponseLP1DueDate";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE = "loanProductCreateResponseLP1PaymentStrategyDueInAdvance";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_INTEREST_FLAT = "loanProductCreateResponseLP1PaymentStrategyDueInAdvanceInterestFlat";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE = "loanProductCreateResponseLP1PaymentStrategyDueInAdvancePenaltyInterestPrincipalFee";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_PAYMENT_STRATEGY_DUE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_INTEREST_FLAT = "loanProductCreateResponseLP1PaymentStrategyDueInAdvancePenaltyInterestPrincipalFeeInterestFlat";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT = "loanProductCreateResponseLP1InterestFlat";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT = "loanProductCreateResponseLP1InterestFlatOverdueFromAmount";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_FLAT_OVERDUE_FROM_AMOUNT_INTEREST = "loanProductCreateResponseLP1InterestFlatOverdueFromAmountInterest";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_MULTI_DISBURSE = "loanProductCreateResponseLP1InterestFlatMultiDisburse";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_PERIOD_SAME_AS_PAYMENT = "loanProductCreateResponseLP1InterestDecliningPeriodSameAsPayment";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_1MONTH_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_MONTHLY = "loanProductCreateResponseLP1InterestDecliningPeriodSameAsPaymentRecalculation";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE = "loanProductCreateResponseLP1InterestDecliningBalanceDailyRecalculationCompoundingNone";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADVANCED_CUSTOM_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE = "loanProductCreateResponseLP2ProgressiveLoanScheduleCustomPaymentAllocation";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE = "loanProductCreateResponseLP1InterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNone";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_SAR_RECALCULATION_SAME_AS_REPAYMENT_COMPOUNDING_NONE_MULTI_DISBURSEMENT = "loanProductCreateResponseLP1InterestDecliningBalanceDailyRecalculationSameAsRepaymentCompoundingNoneMultiDisbursement";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_REDUCE_NR_INSTALLMENTS = "loanProductCreateResponseLP1InterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleReduceNrInstallments";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_BALANCE_DAILY_RECALCULATION_COMPOUNDING_NONE_RESCHEDULE_NEXT_REPAYMENTS = "loanProductCreateResponseLP1InterestDecliningBalanceDailyRecalculationCompoundingNoneRescheduleNextRepayments";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP1_INTEREST_DECLINING_PERIOD_DAILY = "loanProductCreateResponseLP1InterestDecliningPeriodDaily";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_AUTO_ADVANCED_PAYMENT_ALLOCATION = "loanProductCreateResponseLP2DownPaymentAutoAdvancedPaymentAllocation";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION = "loanProductCreateResponseLP2DownPaymentAdvancedPaymentAllocation";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT = "loanProductCreateResponseLP2DownPayment";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_INTEREST = "loanProductCreateResponseLP2DownPaymentInterest";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_INTEREST_AUTO = "loanProductCreateResponseLP2DownPaymentInterestAuto";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_AUTO = "loanProductCreateResponseLP2DownPaymentAuto";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE = "loanProductCreateResponseLP2DownPaymentProgressiveLoanSchedule";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE_VERTICAL = "loanProductCreateResponseLP2DownPaymentProgressiveLoanScheduleVertical";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADVANCED_PAYMENT_ALLOCATION_PROGRESSIVE_LOAN_SCHEDULE_INSTALLMENT_LEVEL_DELINQUENCY = "loanProductCreateResponseLP2DownPaymentProgressiveLoanScheduleInstallmentLevelDelinquency";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADV_PMT_ALLOC_PROG_SCHEDULE_HOR_INST_LVL_DELINQUENCY_CREDIT_ALLOCATION = "loanProductCreateResponseLP2DownPaymentProgressiveLoanScheduleHorizontalInstallmentLevelDelinquencyCreditAllocation";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_DOWNPAYMENT_ADV_PMT_ALLOC_FIXED_LENGTH = "loanProductCreateResponseLP2DownPaymentProgressiveLoanScheduleFixedLength";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_TILL_PRECLOSE = "loanProductCreateResponseLP2AdvancedPaymentInterestDailyEmi36030InterestRecalculationTillPreClose";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PYMNT_INTEREST_DAILY_EMI_360_30_INTEREST_RECALCULATION_TILL_REST_FREQUENCY = "loanProductCreateResponseLP2AdvancedPaymentInterestDailyEmi36030InterestRecalculationTillRestFrequency";
    public static final String DEFAULT_LOAN_PRODUCT_CREATE_RESPONSE_LP2_ADV_PMT_ALLOC_PROGRESSIVE_LOAN_SCHEDULE_HORIZONTAL = "loanProductCreateResponseLP2ProgressiveLoanScheduleHorizontal";
    public static final String CHARGE_FOR_LOAN_PERCENT_LATE_CREATE_RESPONSE = "ChargeForLoanPercentLateCreateResponse";
    public static final String CHARGE_FOR_LOAN_PERCENT_LATE_AMOUNT_PLUS_INTEREST_CREATE_RESPONSE = "ChargeForLoanPercentLateAmountPlusInterestCreateResponse";
    public static final String CHARGE_FOR_LOAN_PERCENT_PROCESSING_CREATE_RESPONSE = "ChargeForLoanPercentProcessingCreateResponse";
    public static final String CHARGE_FOR_LOAN_FIXED_LATE_CREATE_RESPONSE = "ChargeForLoanFixedLateCreateResponse";
    public static final String CHARGE_FOR_LOAN_FIXED_RETURNED_PAYMENT_CREATE_RESPONSE = "ChargeForLoanFixedReturnedPaymentCreateResponse";
    public static final String CHARGE_FOR_LOAN_SNOOZE_FEE_CREATE_RESPONSE = "ChargeForLoanSnoozeFeeCreateResponse";
    public static final String CHARGE_FOR_LOAN_NSF_FEE_CREATE_RESPONSE = "ChargeForLoanNsfFeeCreateResponse";
    public static final String CHARGE_FOR_LOAN_DISBURSEMENET_FEE_CREATE_RESPONSE = "ChargeForLoanDisbursementCreateResponse";
    public static final String CHARGE_FOR_LOAN_INSTALLMENT_FEE_CREATE_RESPONSE = "ChargeForLoanInstallmentCreateResponse";
    public static final String CHARGE_FOR_CLIENT_FIXED_FEE_CREATE_RESPONSE = "ChargeForClientFixedFeeCreateResponse";
    public static final String LOAN_RESPONSE = "loanResponse";
    public static final String LOAN_REPAYMENT_UNDO_RESPONSE = "loanRepaymentUndoResponse";
    public static final String LOAN_TRANSACTION_UNDO_RESPONSE = "loanTransactionUndoResponse";
    public static final String LOAN_CHARGEBACK_RESPONSE = "loanChargebackResponse";
    public static final String LOAN_CHARGE_ADJUSTMENT_RESPONSE = "loanChargeAdjustmentResponse";
    public static final String PUT_CURRENCIES_RESPONSE = "putCurrenciesResponse";
    public static final String BATCH_API_CALL_RESPONSE = "batchApiCallResponse";
    public static final String BATCH_API_CALL_IDEMPOTENCY_KEY = "batchApiIdempotencyKey";
    public static final String BATCH_API_CALL_IDEMPOTENCY_KEY_2 = "batchApiIdempotencyKey2";
    public static final String BATCH_API_CALL_CLIENT_EXTERNAL_ID = "batchApiClientExternalId";
    public static final String BATCH_API_CALL_CLIENT_EXTERNAL_ID_2 = "batchApiClientExternalId2";
    public static final String BATCH_API_CALL_LOAN_EXTERNAL_ID = "batchApiLoanExternalId";
    public static final String BATCH_API_CALL_LOAN_EXTERNAL_ID_2 = "batchApiLoanExternalId2";
    public static final String EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE = "eurSavingsAccountCreateResponse";
    public static final String USD_SAVINGS_ACCOUNT_CREATE_RESPONSE = "usdSavingsAccountCreateResponse";
    public static final String EUR_SAVINGS_ACCOUNT_APPROVE_RESPONSE = "eurSavingsAccountApproveResponse";
    public static final String USD_SAVINGS_ACCOUNT_APPROVE_RESPONSE = "usdSavingsAccountApproveResponse";
    public static final String EUR_SAVINGS_ACCOUNT_ACTIVATED_RESPONSE = "eurSavingsAccountActivateResponse";
    public static final String USD_SAVINGS_ACCOUNT_ACTIVATED_RESPONSE = "usdSavingsAccountActivateResponse";
    public static final String EUR_SAVINGS_ACCOUNT_DEPOSIT_RESPONSE = "eurSavingsAccountDepositResponse";
    public static final String USD_SAVINGS_ACCOUNT_DEPOSIT_RESPONSE = "usdSavingsAccountDepositResponse";
    public static final String EUR_SAVINGS_ACCOUNT_WITHDRAW_RESPONSE = "eurSavingsAccountWithdrawResponse";
    public static final String USD_SAVINGS_ACCOUNT_WITHDRAW_RESPONSE = "usdSavingsAccountWithdrawResponse";
    public static final String LOAN_FRAUD_MODIFY_RESPONSE = "loanFraudModifyResponse";
    public static final String DEFAULT_SAVINGS_PRODUCT_CREATE_RESPONSE_EUR = "defaultSavingsProductCreateResponseEur";
    public static final String DEFAULT_SAVINGS_PRODUCT_CREATE_RESPONSE_USD = "defaultSavingsProductCreateResponseUsd";
    public static final String TRANSACTION_IDEMPOTENCY_KEY = "transactionIdempotencyKey";
    public static final String LOAN_CHARGE_OFF_RESPONSE = "loanChargeOffResponse";
    public static final String LOAN_CHARGE_OFF_UNDO_RESPONSE = "loanChargeOffUndoResponse";
    public static final String CREATED_SIMPLE_USER_RESPONSE = "createdSimpleUserResponse";
    public static final String ASSET_EXTERNALIZATION_RESPONSE = "assetExternalizationResponse";
    public static final String ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_USER_GENERATED = "assetExternalizationTransferExternalIdUserGenerated";
    public static final String ASSET_EXTERNALIZATION_TRANSFER_EXTERNAL_ID_FROM_RESPONSE = "assetExternalizationTransferExternalIdFromResponse";
    public static final String ASSET_EXTERNALIZATION_SALES_TRANSFER_EXTERNAL_ID_FROM_RESPONSE = "assetExternalizationSalesTransferExternalIdFromResponse";
    public static final String ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_EXTERNAL_ID_FROM_RESPONSE = "assetExternalizationBuybackTransferExternalIdFromResponse";
    public static final String ASSET_EXTERNALIZATION_BUYBACK_TRANSFER_PREFIX = "assetExternalizationTransferPrefix";
    public static final String ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID = "assetExternalizationOwnerExternalId";
    public static final String TRANSACTION_EVENT = "transactionEvent";
    public static final String LOAN_WRITE_OFF_RESPONSE = "loanWriteOffResponse";
    public static final String LOAN_DELINQUENCY_ACTION_RESPONSE = "loanDelinquencyActionResponse";

}
