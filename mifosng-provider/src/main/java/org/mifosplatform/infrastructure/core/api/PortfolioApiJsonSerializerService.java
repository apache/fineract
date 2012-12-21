package org.mifosplatform.infrastructure.core.api;

import java.util.Collection;
import java.util.Set;

import org.mifosplatform.portfolio.loanaccount.gaurantor.data.GuarantorData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductData;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountData;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductData;

public interface PortfolioApiJsonSerializerService {

    String serializeSavingProductDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<SavingProductData> products);

    String serializeSavingProductDataToJson(boolean prettyPrint, Set<String> responseParameters, SavingProductData savingProduct);

    String serializeDepositProductDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<DepositProductData> products);

    String serializeDepositProductDataToJson(boolean prettyPrint, Set<String> responseParameters, DepositProductData depositProduct);

    String serializeDepositAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<DepositAccountData> accounts);

    String serializeDepositAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, DepositAccountData account);

    String serializeSavingAccountsDataToJson(boolean prettyPrint, Set<String> responseParameters, SavingAccountData account);

    String serializeSavingAccountsDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<SavingAccountData> accounts);

    String serializeGuarantorDataToJson(boolean prettyPrint, Set<String> responseParameters, GuarantorData guarantorData);

    String serializeSavingScheduleDataToJson(boolean prettyPrint, Set<String> responseParameters, SavingScheduleData savingScheduleData);

}