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
package org.apache.fineract.portfolio.savings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.fineract.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;

public class SavingsApiConstants {

    public static final String SAVINGS_PRODUCT_RESOURCE_NAME = "savingsproduct";
    public static final String SAVINGS_ACCOUNT_RESOURCE_NAME = "savingsaccount";
    public static final String SAVINGS_ACCOUNT_TRANSACTION_RESOURCE_NAME = "savingsaccount.transaction";
    public static final String SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME = "savingsaccountcharge";

    // actions
    public static final String postInterestValidationOnClosure = "postInterestValidationOnClosure";
    public static final String summitalAction = ".summital";
    public static final String approvalAction = ".approval";
    public static final String undoApprovalAction = ".undoApproval";
    public static final String rejectAction = ".reject";
    public static final String withdrawnByApplicantAction = ".withdrawnByApplicant";
    public static final String activateAction = ".activate";
    public static final String modifyApplicationAction = ".modify";
    public static final String deleteApplicationAction = ".delete";
    public static final String undoTransactionAction = ".undotransaction";
    public static final String applyAnnualFeeTransactionAction = ".applyannualfee";
    public static final String adjustTransactionAction = ".adjusttransaction";
    public static final String closeAction = ".close";
    public static final String payChargeTransactionAction = ".paycharge";
    public static final String waiveChargeTransactionAction = ".waivecharge";
    public static final String updateMaturityDetailsAction = ".updateMaturityDetails";
    public static final String blockAction = ".block";
    public static final String unblockAction = ".unblock";
    public static final String blockCreditsAction = ".blockCredits";
    public static final String unblockCreditsAction = ".unblockCredits";
    public static final String blockDebitsAction = ".blockDebits";
    public static final String unblockDebitsAction = ".unblockDebits";

    // command
    public static final String COMMAND_UNDO_TRANSACTION = "undo";
    public static final String COMMAND_ADJUST_TRANSACTION = "modify";
    public static final String COMMAND_WAIVE_CHARGE = "waive";
    public static final String COMMAND_PAY_CHARGE = "paycharge";
    public static final String COMMAND_INACTIVATE_CHARGE = "inactivate";
    public static final String COMMAND_HOLD_AMOUNT = "holdAmount";
    public static final String COMMAND_RELEASE_AMOUNT = "releaseAmount";
    public static final String COMMAND_UNBLOCK_ACCOUNT= "unblock";
    public static final String COMMAND_BLOCK_ACCOUNT = "block";
    public static final String COMMAND_BLOCK_CREDIT = "blockCredit";
    public static final String COMMAND_BLOCK_DEBIT = "blockDebit";
    public static final String COMMAND_UNBLOCK_DEBIT = "unblockDebit";
    public static final String COMMAND_UNBLOCK_CREDIT = "unblockCredit";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";
    public static final String monthDayFormatParamName = "monthDayFormat";
    public static final String staffIdParamName = "savingsOfficerId";

    // savings product and account parameters
    public static final String idParamName = "id";
    public static final String accountNoParamName = "accountNo";
    public static final String externalIdParamName = "externalId";
    public static final String statusParamName = "status";
    public static final String subStatusParamName = "subStatus";
    public static final String clientIdParamName = "clientId";
    public static final String groupIdParamName = "groupId";
    public static final String productIdParamName = "productId";
    public static final String fieldOfficerIdParamName = "fieldOfficerId";

    public static final String submittedOnDateParamName = "submittedOnDate";
    public static final String rejectedOnDateParamName = "rejectedOnDate";
    public static final String withdrawnOnDateParamName = "withdrawnOnDate";
    public static final String approvedOnDateParamName = "approvedOnDate";
    public static final String activatedOnDateParamName = "activatedOnDate";
    public static final String closedOnDateParamName = "closedOnDate";

    public static final String activeParamName = "active";
    public static final String nameParamName = "name";
    public static final String shortNameParamName = "shortName";
    public static final String descriptionParamName = "description";
    public static final String currencyCodeParamName = "currencyCode";
    public static final String digitsAfterDecimalParamName = "digitsAfterDecimal";
    public static final String inMultiplesOfParamName = "inMultiplesOf";
    public static final String nominalAnnualInterestRateParamName = "nominalAnnualInterestRate";
    public static final String interestCompoundingPeriodTypeParamName = "interestCompoundingPeriodType";
    public static final String interestPostingPeriodTypeParamName = "interestPostingPeriodType";
    public static final String interestCalculationTypeParamName = "interestCalculationType";
    public static final String interestCalculationDaysInYearTypeParamName = "interestCalculationDaysInYearType";
    public static final String minRequiredOpeningBalanceParamName = "minRequiredOpeningBalance";
    public static final String lockinPeriodFrequencyParamName = "lockinPeriodFrequency";
    public static final String lockinPeriodFrequencyTypeParamName = "lockinPeriodFrequencyType";
    public static final String withdrawalFeeAmountParamName = "withdrawalFeeAmount";
    public static final String withdrawalFeeTypeParamName = "withdrawalFeeType";
    public static final String withdrawalFeeForTransfersParamName = "withdrawalFeeForTransfers";
    public static final String feeAmountParamName = "feeAmount";// to be deleted
    public static final String feeOnMonthDayParamName = "feeOnMonthDay";
    public static final String feeIntervalParamName = "feeInterval";
    public static final String accountingRuleParamName = "accountingRule";
    public static final String paymentTypeIdParamName = "paymentTypeId";
    public static final String transactionAccountNumberParamName = "accountNumber";
    public static final String checkNumberParamName = "checkNumber";
    public static final String routingCodeParamName = "routingCode";
    public static final String receiptNumberParamName = "receiptNumber";
    public static final String bankNumberParamName = "bankNumber";
    public static final String allowOverdraftParamName = "allowOverdraft";
    public static final String overdraftLimitParamName = "overdraftLimit";
    public static final String nominalAnnualInterestRateOverdraftParamName = "nominalAnnualInterestRateOverdraft";
    public static final String minOverdraftForInterestCalculationParamName = "minOverdraftForInterestCalculation";
    public static final String minRequiredBalanceParamName = "minRequiredBalance";
    public static final String enforceMinRequiredBalanceParamName = "enforceMinRequiredBalance";
    public static final String minBalanceForInterestCalculationParamName = "minBalanceForInterestCalculation";
    public static final String withdrawBalanceParamName = "withdrawBalance";
    public static final String onHoldFundsParamName = "onHoldFunds";
    public static final String withHoldTaxParamName = "withHoldTax";
    public static final String taxGroupIdParamName = "taxGroupId";

    // transaction parameters
    public static final String transactionDateParamName = "transactionDate";
    public static final String transactionAmountParamName = "transactionAmount";
    public static final String paymentDetailDataParamName = "paymentDetailData";
    public static final String runningBalanceParamName = "runningBalance";
    public static final String reversedParamName = "reversed";
    public static final String dateParamName = "date";

    // charges parameters
    public static final String chargeIdParamName = "chargeId";
    public static final String chargesParamName = "charges";
    public static final String savingsAccountChargeIdParamName = "savingsAccountChargeId";
    public static final String chargeNameParamName = "name";
    public static final String penaltyParamName = "penalty";
    public static final String chargeTimeTypeParamName = "chargeTimeType";
    public static final String dueAsOfDateParamName = "dueDate";
    public static final String chargeCalculationTypeParamName = "chargeCalculationType";
    public static final String percentageParamName = "percentage";
    public static final String amountPercentageAppliedToParamName = "amountPercentageAppliedTo";
    public static final String currencyParamName = "currency";
    public static final String amountWaivedParamName = "amountWaived";
    public static final String amountWrittenOffParamName = "amountWrittenOff";
    public static final String amountOutstandingParamName = "amountOutstanding";
    public static final String amountOrPercentageParamName = "amountOrPercentage";
    public static final String amountParamName = "amount";
    public static final String amountPaidParamName = "amountPaid";
    public static final String chargeOptionsParamName = "chargeOptions";
    public static final String chargePaymentModeParamName = "chargePaymentMode";

    public static final String noteParamName = "note";

    // Savings account associations
    public static final String transactions = "transactions";
    public static final String charges = "charges";
    public static final String linkedAccount = "linkedAccount";

    // Savings on hold transaction
    public static final String onHoldTransactionTypeParamName = "transactionType";
    public static final String onHoldTransactionDateParamName = "transactionDate";
    public static final String onHoldReversedParamName = "reversed";
    
    // Savings Dormancy
    public static final String isDormancyTrackingActiveParamName = "isDormancyTrackingActive";
    public static final String daysToInactiveParamName = "daysToInactive";
    public static final String daysToDormancyParamName = "daysToDormancy";
    public static final String daysToEscheatParamName = "daysToEscheat";

    public static final String datatables = "datatables";
    
    public static final String ERROR_MSG_SAVINGS_ACCOUNT_NOT_ACTIVE = "not.in.active.state";
    
    public static final Set<String> SAVINGS_ACCOUNT_HOLD_AMOUNT_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(transactionDateParamName, dateFormatParamName, localeParamName, transactionAmountParamName));
}