package org.mifosplatform.integrationtests.common.accounting;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class AccountHelper {

    private final String CREATE_GL_ACCOUNT_URL = "/mifosng-provider/api/v1/glaccounts?tenantIdentifier=default";
    private final String GL_ACCOUNT_ID_RESPONSE = "resourceId";

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public AccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Account createAssetAccount() {
        final String assetAccountJSON = new GLAccountBuilder().withAccountTypeAsAsset().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, this.CREATE_GL_ACCOUNT_URL,
                assetAccountJSON, this.GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.ASSET);
    }

    public Account createIncomeAccount() {
        final String assetAccountJSON = new GLAccountBuilder().withAccountTypeAsIncome().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, this.CREATE_GL_ACCOUNT_URL,
                assetAccountJSON, this.GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.INCOME);
    }

    public Account createExpenseAccount() {
        final String assetAccountJSON = new GLAccountBuilder().withAccountTypeAsExpense().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, this.CREATE_GL_ACCOUNT_URL,
                assetAccountJSON, this.GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.EXPENSE);
    }

}
