package org.mifosplatform.integrationtests.common.accounting;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.mifosplatform.integrationtests.common.Utils;

public class AccountHelper {

    private final String CREATE_GL_ACCOUNT_URL = "/mifosng-provider/api/v1/glaccounts?tenantIdentifier=default";
    private final String GL_ACCOUNT_ID_RESPONSE = "resourceId";

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    public AccountHelper(RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Account createAssetAccount(){
        String assetAccountJSON= new GLAccountBuilder()
                .withAccountTypeAsAsset()
                .build();
        Integer accountID = Utils.performServerPost(requestSpec, responseSpec, CREATE_GL_ACCOUNT_URL, assetAccountJSON, GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.ASSET);
    }
    public Account createIncomeAccount(){
        String assetAccountJSON= new GLAccountBuilder()
                .withAccountTypeAsIncome()
                .build();
        Integer accountID = Utils.performServerPost(requestSpec, responseSpec, CREATE_GL_ACCOUNT_URL, assetAccountJSON, GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.INCOME);
    }
    public Account createExpenseAccount(){
        String assetAccountJSON= new GLAccountBuilder()
                .withAccountTypeAsExpense()
                .build();
        Integer accountID = Utils.performServerPost(requestSpec, responseSpec, CREATE_GL_ACCOUNT_URL, assetAccountJSON, GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.EXPENSE);
    }

}
