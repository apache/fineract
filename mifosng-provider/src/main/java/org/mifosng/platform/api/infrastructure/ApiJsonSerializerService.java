package org.mifosng.platform.api.infrastructure;

import java.util.Collection;
import java.util.Set;

import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.AppUserData;
import org.mifosng.platform.api.data.AuthenticatedUserData;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.ClientLoanAccountSummaryCollectionData;
import org.mifosng.platform.api.data.ConfigurationData;
import org.mifosng.platform.api.data.DepositAccountData;
import org.mifosng.platform.api.data.DepositProductData;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.GroupData;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.data.OfficeData;
import org.mifosng.platform.api.data.OfficeTransactionData;
import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.RoleData;
import org.mifosng.platform.api.data.SavingProductData;

public interface ApiJsonSerializerService {

	String serializeAuthenticatedUserDataToJson(boolean prettyPrint, AuthenticatedUserData authenticatedUserData);
	
	String serializeGenericResultsetDataToJson(boolean prettyPrint, GenericResultsetData result);

	String serializeAdditionalFieldsSetDataToJson(boolean prettyPrint, Collection<AdditionalFieldsSetData> result);
	
	String serializePermissionDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<PermissionData> permissions);

	String serializeRoleDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<RoleData> roles);

	String serializeRoleDataToJson(boolean prettyPrint, Set<String> responseParameters, RoleData role);

	String serializeAppUserDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<AppUserData> users);

	String serializeAppUserDataToJson(boolean prettyPrint, Set<String> responseParameters, AppUserData user);

	String serializeOfficeDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<OfficeData> offices);

	String serializeOfficeDataToJson(boolean prettyPrint, Set<String> responseParameters, OfficeData office);

	String serializeOfficeTransactionDataToJson(boolean prettyPrint, Set<String> responseParameters, OfficeTransactionData officeTransaction);

	String serializeConfigurationDataToJson(boolean prettyPrint, Set<String> responseParameters, ConfigurationData configuration);

	String serializeFundDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<FundData> funds);

	String serializeFundDataToJson(boolean prettyPrint, Set<String> responseParameters, FundData fund);

	String serializeLoanProductDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<LoanProductData> products);
	
	String serializeLoanProductDataToJson(boolean prettyPrint, Set<String> responseParameters, LoanProductData loanProduct);

	String serializeSavingProductDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<SavingProductData> products);

	String serializeSavingProductDataToJson(boolean prettyPrint, Set<String> responseParameters, SavingProductData savingProduct);

	String serializeDepositProductDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<DepositProductData> products);

	String serializeDepositProductDataToJson(boolean prettyPrint, Set<String> responseParameters, DepositProductData depositProduct);

	String serializeDepositAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<DepositAccountData> accounts);

	String serializeDepositAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, DepositAccountData account);

	String serializeClientDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<ClientData> clients);

	String serializeClientDataToJson(boolean prettyPrint, Set<String> responseParameters, ClientData clientData);

	String serializeClientLoanAccountSummaryCollectionDataToJson(boolean prettyPrint, Set<String> responseParameters, ClientLoanAccountSummaryCollectionData clientAccount);

	String serializeGroupDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GroupData> groups);

	String serializeGroupDataToJson(boolean prettyPrint, Set<String> responseParameters, GroupData group);
}