/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.accounting.common.AccountingConstants.SAVINGS_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.portfolio.savings.data.SavingsAccountData;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;

public class SavingsApiConstants {

    public static final String SAVINGS_PRODUCT_RESOURCE_NAME = "savingsproduct";
    public static final String SAVINGS_ACCOUNT_RESOURCE_NAME = "savingsaccount";
    public static final String SAVINGS_ACCOUNT_TRANSACTION_RESOURCE_NAME = "savingsaccount.transaction";
    public static final String SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME = "savingsaccountcharge";

    // actions
    public static String summitalAction = ".summital";
    public static String approvalAction = ".approval";
    public static String undoApprovalAction = ".undoApproval";
    public static String rejectAction = ".reject";
    public static String withdrawnByApplicantAction = ".withdrawnByApplicant";
    public static String activateAction = ".activate";
    public static String modifyApplicationAction = ".modify";
    public static String deleteApplicationAction = ".delete";
    public static String undoTransactionAction = ".undotransaction";
    public static String applyAnnualFeeTransactionAction = ".applyannualfee";
    public static String adjustTransactionAction = ".adjusttransaction";
    public static String closeAction = ".close";
    public static String payChargeTransactionAction = ".paycharge";
    public static String waiveChargeTransactionAction = ".waivecharge";
    public static String updateMaturityDetailsAction = ".updateMaturityDetails";

    // command
    public static String COMMAND_UNDO_TRANSACTION = "undo";
    public static String COMMAND_ADJUST_TRANSACTION = "modify";
    public static String COMMAND_WAIVE_CHARGE = "waive";
    public static String COMMAND_PAY_CHARGE = "paycharge";
    public static String COMMAND_INACTIVATE_CHARGE = "inactivate";

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
    public static final String minRequiredBalanceParamName = "minRequiredBalance";
    public static final String enforceMinRequiredBalanceParamName = "enforceMinRequiredBalance";
    public static final String minBalanceForInterestCalculationParamName = "minBalanceForInterestCalculation";
    public static final String withdrawBalanceParamName = "withdrawBalance";
    public static final String onHoldFundsParamName = "onHoldFunds";

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

    public static final Set<String> SAVINGS_PRODUCT_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            monthDayFormatParamName, nameParamName, shortNameParamName, descriptionParamName, currencyCodeParamName,
            digitsAfterDecimalParamName, inMultiplesOfParamName, nominalAnnualInterestRateParamName,
            interestCompoundingPeriodTypeParamName, interestPostingPeriodTypeParamName, interestCalculationTypeParamName,
            interestCalculationDaysInYearTypeParamName, minRequiredOpeningBalanceParamName, lockinPeriodFrequencyParamName,
            lockinPeriodFrequencyTypeParamName, withdrawalFeeAmountParamName, withdrawalFeeTypeParamName,
            withdrawalFeeForTransfersParamName, feeAmountParamName, feeOnMonthDayParamName, accountingRuleParamName, chargesParamName,
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_FEES.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_PENALTIES.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INTEREST_ON_SAVINGS.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_CONTROL.getValue(), SAVINGS_PRODUCT_ACCOUNTING_PARAMS.TRANSFERS_SUSPENSE.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.SAVINGS_REFERENCE.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.FEE_INCOME_ACCOUNT_MAPPING.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.PENALTY_INCOME_ACCOUNT_MAPPING.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.OVERDRAFT_PORTFOLIO_CONTROL.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.LOSSES_WRITTEN_OFF.getValue(),
            SAVINGS_PRODUCT_ACCOUNTING_PARAMS.INCOME_FROM_INTEREST.getValue(), allowOverdraftParamName, overdraftLimitParamName,
            minRequiredBalanceParamName, enforceMinRequiredBalanceParamName, minBalanceForInterestCalculationParamName));

    /**
     * These parameters will match the class level parameters of
     * {@link SavingsProductData}. Where possible, we try to get response
     * parameters to match those of request parameters.
     */
    public static final Set<String> SAVINGS_PRODUCT_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(idParamName, nameParamName,
            shortNameParamName, descriptionParamName, "currency", digitsAfterDecimalParamName, inMultiplesOfParamName,
            nominalAnnualInterestRateParamName, interestCompoundingPeriodTypeParamName, interestPostingPeriodTypeParamName,
            interestCalculationTypeParamName, interestCalculationDaysInYearTypeParamName, minRequiredOpeningBalanceParamName,
            lockinPeriodFrequencyParamName, lockinPeriodFrequencyTypeParamName, withdrawalFeeAmountParamName, withdrawalFeeTypeParamName,
            withdrawalFeeForTransfersParamName, feeAmountParamName, feeOnMonthDayParamName, "currencyOptions",
            "interestCompoundingPeriodTypeOptions", "interestPostingPeriodTypeOptions", "interestCalculationTypeOptions",
            "interestCalculationDaysInYearTypeOptions", "lockinPeriodFrequencyTypeOptions", "withdrawalFeeTypeOptions"));

    public static final Set<String> SAVINGS_ACCOUNT_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, monthDayFormatParamName, staffIdParamName, accountNoParamName, externalIdParamName, clientIdParamName,
            groupIdParamName, productIdParamName, fieldOfficerIdParamName, submittedOnDateParamName, nominalAnnualInterestRateParamName,
            interestCompoundingPeriodTypeParamName, interestPostingPeriodTypeParamName, interestCalculationTypeParamName,
            interestCalculationDaysInYearTypeParamName, minRequiredOpeningBalanceParamName, lockinPeriodFrequencyParamName,
            lockinPeriodFrequencyTypeParamName,
            // withdrawalFeeAmountParamName, withdrawalFeeTypeParamName,
            withdrawalFeeForTransfersParamName, feeAmountParamName, feeOnMonthDayParamName, chargesParamName, allowOverdraftParamName,
            overdraftLimitParamName, minRequiredBalanceParamName, enforceMinRequiredBalanceParamName));

    /**
     * These parameters will match the class level parameters of
     * {@link SavingsAccountData}. Where possible, we try to get response
     * parameters to match those of request parameters.
     */
    public static final Set<String> SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(idParamName, accountNoParamName,
            externalIdParamName, statusParamName, activatedOnDateParamName, staffIdParamName, clientIdParamName, "clientName",
            groupIdParamName, "groupName", "savingsProductId", "savingsProductName", "currency", nominalAnnualInterestRateParamName,
            interestCompoundingPeriodTypeParamName, interestCalculationTypeParamName, interestCalculationDaysInYearTypeParamName,
            minRequiredOpeningBalanceParamName, lockinPeriodFrequencyParamName, lockinPeriodFrequencyTypeParamName,
            withdrawalFeeAmountParamName, withdrawalFeeTypeParamName, withdrawalFeeForTransfersParamName, feeAmountParamName,
            feeOnMonthDayParamName, "summary", "transactions", "productOptions", "interestCompoundingPeriodTypeOptions",
            "interestPostingPeriodTypeOptions", "interestCalculationTypeOptions", "interestCalculationDaysInYearTypeOptions",
            "lockinPeriodFrequencyTypeOptions", "withdrawalFeeTypeOptions", "withdrawalFee", "annualFee", onHoldFundsParamName));

    public static final Set<String> SAVINGS_ACCOUNT_TRANSACTION_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, transactionDateParamName, transactionAmountParamName, paymentTypeIdParamName,
            transactionAccountNumberParamName, checkNumberParamName, routingCodeParamName, receiptNumberParamName, bankNumberParamName));

    public static final Set<String> SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(idParamName, "accountId", accountNoParamName, "currency", "amount", dateParamName, paymentDetailDataParamName,
                    runningBalanceParamName, reversedParamName));

    public static final Set<String> SAVINGS_ACCOUNT_ACTIVATION_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, activatedOnDateParamName));

    public static final Set<String> SAVINGS_ACCOUNT_CLOSE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(localeParamName,
            dateFormatParamName, closedOnDateParamName, noteParamName, paymentTypeIdParamName, withdrawBalanceParamName,
            transactionAccountNumberParamName, checkNumberParamName, routingCodeParamName, receiptNumberParamName, bankNumberParamName));

    public static final Set<String> SAVINGS_ACCOUNT_CHARGES_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(chargeIdParamName,
            savingsAccountChargeIdParamName, chargeNameParamName, penaltyParamName, chargeTimeTypeParamName, dueAsOfDateParamName,
            chargeCalculationTypeParamName, percentageParamName, amountPercentageAppliedToParamName, currencyParamName,
            amountWaivedParamName, amountWrittenOffParamName, amountOutstandingParamName, amountOrPercentageParamName, amountParamName,
            amountPaidParamName, chargeOptionsParamName));

    public static final Set<String> SAVINGS_ACCOUNT_CHARGES_ADD_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(chargeIdParamName,
            amountParamName, dueAsOfDateParamName, dateFormatParamName, localeParamName, feeOnMonthDayParamName, monthDayFormatParamName,
            feeIntervalParamName));

    public static final Set<String> SAVINGS_ACCOUNT_CHARGES_PAY_CHARGE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            amountParamName, dueAsOfDateParamName, dateFormatParamName, localeParamName));

    public static final Set<String> SAVINGS_ACCOUNT_ON_HOLD_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(idParamName,
            amountParamName, onHoldTransactionTypeParamName, onHoldTransactionDateParamName, onHoldReversedParamName));
}