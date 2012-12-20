package org.mifosplatform.infrastructure.core.api;

import java.util.Collection;
import java.util.Set;

import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.security.data.AuthenticatedUserData;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryCollectionData;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.loanaccount.gaurantor.data.GuarantorData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductData;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountData;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductData;

public interface PortfolioApiJsonSerializerService {

    String serializeAuthenticatedUserDataToJson(boolean prettyPrint, AuthenticatedUserData authenticatedUserData);

    String serializeGenericResultsetDataToJson(boolean prettyPrint, GenericResultsetData result);

    String serializeDatatableDataToJson(boolean prettyPrint, Collection<DatatableData> result);

    String serializeSavingProductDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<SavingProductData> products);

    String serializeSavingProductDataToJson(boolean prettyPrint, Set<String> responseParameters, SavingProductData savingProduct);

    String serializeDepositProductDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<DepositProductData> products);

    String serializeDepositProductDataToJson(boolean prettyPrint, Set<String> responseParameters, DepositProductData depositProduct);

    String serializeDepositAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<DepositAccountData> accounts);

    String serializeDepositAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, DepositAccountData account);

    String serializeClientAccountSummaryCollectionDataToJson(boolean prettyPrint, Set<String> responseParameters,
            ClientAccountSummaryCollectionData clientAccount);

    String serializeGroupAccountSummaryCollectionDataToJson(boolean prettyPrint, Set<String> responseParameters,
            GroupAccountSummaryCollectionData groupAccount);

    String serializeGroupDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GroupData> groups);

    String serializeGroupDataToJson(boolean prettyPrint, Set<String> responseParameters, GroupData group);

    String serializeDocumentDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<DocumentData> documentDatas);

    String serializeDocumentDataToJson(boolean prettyPrint, Set<String> responseParameters, DocumentData documentData);

    String serializeSavingAccountsDataToJson(boolean prettyPrint, Set<String> responseParameters, SavingAccountData account);

    String serializeSavingAccountsDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<SavingAccountData> accounts);

    String serializeGuarantorDataToJson(boolean prettyPrint, Set<String> responseParameters, GuarantorData guarantorData);

    String serializeSavingScheduleDataToJson(boolean prettyPrint, Set<String> responseParameters, SavingScheduleData savingScheduleData);

}