package org.mifosplatform.portfolio.savings.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.portfolio.savings.data.SavingsAccountData;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;

public class SavingsApiConstants {

    public static final String SAVINGS_PRODUCT_RESOURCE_NAME = "savingsproduct";
    public static final String SAVINGS_ACCOUNT_RESOURCE_NAME = "savingsaccount";
    public static final String SAVINGS_ACCOUNT_TRANSACTION_RESOURCE_NAME = "savingsaccount.transaction";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // savings product and account parameters
    public static final String idParamName = "id";
    public static final String accountNoParamName = "accountNo";
    public static final String externalIdParamName = "externalId";
    public static final String clientIdParamName = "clientId";
    public static final String groupIdParamName = "groupId";
    public static final String productIdParamName = "productId";
    public static final String nameParamName = "name";
    public static final String descriptionParamName = "description";
    public static final String currencyCodeParamName = "currencyCode";
    public static final String digitsAfterDecimalParamName = "digitsAfterDecimal";
    public static final String interestRateParamName = "interestRate";
    public static final String interestRatePeriodFrequencyTypeParamName = "interestRatePeriodFrequencyType";
    public static final String minRequiredOpeningBalanceParamName = "minRequiredOpeningBalance";
    public static final String lockinPeriodFrequencyParamName = "lockinPeriodFrequency";
    public static final String lockinPeriodFrequencyTypeParamName = "lockinPeriodFrequencyType";

    // transaction parameters
    public static final String transactionDateParamName = "transactionDate";
    public static final String transactionAmountParamName = "transactionAmount";

    public static final Set<String> SAVINGS_PRODUCT_REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(localeParamName,
            nameParamName, descriptionParamName, currencyCodeParamName, digitsAfterDecimalParamName, interestRateParamName,
            interestRatePeriodFrequencyTypeParamName, minRequiredOpeningBalanceParamName, lockinPeriodFrequencyParamName,
            lockinPeriodFrequencyTypeParamName));

    /**
     * These parameters will match the class level parameters of
     * {@link SavingsProductData}. Where possible, we try to get response
     * parameters to match those of request parameters.
     */
    public static final Set<String> SAVINGS_PRODUCT_RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(idParamName,
            nameParamName, descriptionParamName, "currency", digitsAfterDecimalParamName, interestRateParamName,
            interestRatePeriodFrequencyTypeParamName, minRequiredOpeningBalanceParamName, lockinPeriodFrequencyParamName,
            lockinPeriodFrequencyTypeParamName, "currencyOptions", "interestRatePeriodFrequencyTypeOptions",
            "lockinPeriodFrequencyTypeOptions"));

    public static final Set<String> SAVINGS_ACCOUNT_REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(localeParamName,
            accountNoParamName, externalIdParamName, clientIdParamName, groupIdParamName, productIdParamName, interestRateParamName,
            interestRatePeriodFrequencyTypeParamName, minRequiredOpeningBalanceParamName, lockinPeriodFrequencyParamName,
            lockinPeriodFrequencyTypeParamName));

    /**
     * These parameters will match the class level parameters of
     * {@link SavingsAccountData}. Where possible, we try to get response
     * parameters to match those of request parameters.
     */
    public static final Set<String> SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(idParamName,
            accountNoParamName, externalIdParamName, clientIdParamName, "clientName", groupIdParamName, "groupName", "savingsProductId",
            "savingsProductName", "currency", interestRateParamName, interestRatePeriodFrequencyTypeParamName, "annualInterestRate",
            minRequiredOpeningBalanceParamName, lockinPeriodFrequencyParamName, lockinPeriodFrequencyTypeParamName, "summary",
            "transactions", "productOptions", "interestRatePeriodFrequencyTypeOptions", "lockinPeriodFrequencyTypeOptions"));

    public static final Set<String> SAVINGS_ACCOUNT_TRANSACTION_REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(
            localeParamName, dateFormatParamName, transactionDateParamName, transactionAmountParamName));

    public static final Set<String> SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(idParamName,
            "accountId", accountNoParamName, "currency", "amount"));
}