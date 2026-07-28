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
package org.apache.fineract.portfolio.workingcapitalloan;

public final class WorkingCapitalLoanConstants {

    private WorkingCapitalLoanConstants() {
        // Prevent instantiation
    }

    public static final String WCL_RESOURCE_NAME = "WORKINGCAPITALLOAN";

    /**
     * Shared by the create/modify validation error and the schedule-generation exception so both emit the same message.
     */
    public static final String EIR_NOT_CALCULABLE_USER_MESSAGE = "Please check the input values - unable to calculate a valid EIR.";

    // Common request parameters (locale, dateFormat, id)
    public static final String localeParameterName = "locale";
    public static final String dateFormatParameterName = "dateFormat";
    public static final String idParameterName = "id";

    // Working Capital Loan Application parameters
    public static final String clientIdParameterName = "clientId";
    public static final String productIdParameterName = "productId";
    public static final String fundIdParameterName = "fundId";
    public static final String accountNoParameterName = "accountNo";
    public static final String externalIdParameterName = "externalId";
    public static final String submittedOnDateParameterName = "submittedOnDate";
    public static final String expectedDisbursementDateParameterName = "expectedDisbursementDate";
    public static final String submittedOnNoteParameterName = "submittedOnNote";
    public static final String totalPaymentVolumeParamName = "totalPaymentVolume";
    public static final String principalAmountParamName = "principalAmount";

    // Loan commands
    public static final String APPROVE_LOAN_COMMAND = "approve";
    public static final String DISBURSE_LOAN_COMMAND = "disburse";
    public static final String REPAYMENT_LOAN_COMMAND = "repayment";
    public static final String GOODWILL_CREDIT_LOAN_COMMAND = "goodwillCredit";
    public static final String CREDIT_BALANCE_REFUND_COMMAND = "creditBalanceRefund";
    public static final String PAYOUT_REFUND_COMMAND = "payoutRefund";
    public static final String DISCOUNT_FEE_LOAN_COMMAND = "discountFee";
    public static final String DISCOUNT_FEE_ADJUSTMENT_LOAN_COMMAND = "discountFeeAdjustment";

    // Approval / Rejection / Undo-approval parameters
    public static final String RESOURCE_NAME = WCL_RESOURCE_NAME;
    public static final String approvedOnDateParamName = "approvedOnDate";
    public static final String approvedLoanAmountParamName = "approvedLoanAmount";
    public static final String expectedDisbursementDateParamName = "expectedDisbursementDate";
    public static final String discountAmountParamName = "discountAmount";
    public static final String discountExternalIdParameterName = "discountExternalId";
    public static final String noteParamName = "note";
    public static final String rejectedOnDateParamName = "rejectedOnDate";

    // Disbursal / Undo disbursal parameters
    public static final String actualDisbursementDateParamName = "actualDisbursementDate";
    public static final String transactionAmountParamName = "transactionAmount";
    public static final String classificationIdParamName = "classificationId";

    public static final String DISBURSEMENT_CLASSIFICATION_CODE_NAME = "working_capital_loan_disbursement_classification";
    public static final String REPAYMENT_CLASSIFICATION_CODE_NAME = "working_capital_loan_repayment_classification";
    public static final String CREDIT_BALANCE_REFUND_CLASSIFICATION_CODE_NAME = "working_capital_loan_credit_balance_refund_classification";
    public static final String DISCOUNT_FEE_CLASSIFICATION_CODE_NAME = "working_capital_loan_discount_fee_classification";
    public static final String paymentDetailsParamName = "paymentDetails";
    public static final String paymentTypeIdParamName = "paymentTypeId";
    public static final String accountNumberParamName = "accountNumber";
    public static final String checkNumberParamName = "checkNumber";
    public static final String routingCodeParamName = "routingCode";
    public static final String receiptNumberParamName = "receiptNumber";
    public static final String bankNumberParamName = "bankNumber";
    public static final String transactionDateParamName = "transactionDate";
    public static final String transactionTypeParamName = "transactionType";
    public static final String transactionIdParamName = "transactionId";
    public static final String loanStatusParamName = "loanStatus";
    public static final String loanProductRelatedDetailsParamName = "loanProductRelatedDetails";

    // Transaction parameters
    public static final String relatedResourceIdParamName = "relatedResourceId";

    public static final String WRITE_OFF_REASONS = "WriteOffReasons";
    public static final String CHARGE_OFF_REASONS = "ChargeOffReasons";

    // transaction undo parameters
    public static final String reversalExternalIdParamName = "reversalExternalId";

    // Transaction Commands
    public static final String UNDO_COMMAND = "undo";

    // Period payment rate change parameters
    public static final String periodPaymentRateParamName = "periodPaymentRate";
    public static final String previousPeriodPaymentRateParamName = "previousRate";

    // Near breach action parameters
    public static final String nearBreachActionParamName = "action";
    public static final String nearBreachThresholdParamName = "nearBreachThreshold";
    public static final String nearBreachFrequencyParamName = "nearBreachFrequency";
    public static final String nearBreachFrequencyTypeParamName = "nearBreachFrequencyType";

    // Loan origination parameters
    public static final String originatorsParameterName = "originators";
}
