package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.accounting.Account;
import org.mifosplatform.integrationtests.common.accounting.AccountHelper;
import org.mifosplatform.integrationtests.common.accounting.FinancialActivityAccountsHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class OfficeAccountingMappingsTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    /**
    @SuppressWarnings("unchecked")
    @Test
    public void testFinancialActivityAccounts() {

        AccountHelper accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        Account transferAccount = accountHelper.createLiabilityAccount();
        Assert.assertNotNull(transferAccount);

        FinancialActivityAccountsHelper accountMappingHelper = new FinancialActivityAccountsHelper(this.requestSpec,
                new ResponseSpecBuilder().build());
        Integer transferAccountMappingId = (Integer) accountMappingHelper.createOfficeAccountMapping(officeId,
                transferAccount.getAccountID(), CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(transferAccountMappingId);

        HashMap mappingDetails = accountMappingHelper.getOfficeAccountMapping(transferAccountMappingId);
        Assert.assertEquals(officeId, ((HashMap) mappingDetails.get("officeData")).get("id"));
        Assert.assertEquals(transferAccount.getAccountID(), ((HashMap) mappingDetails.get("glAccountData")).get("id"));

        Account transferAccount2 = accountHelper.createLiabilityAccount();
        Assert.assertNotNull(transferAccount2);

        HashMap changes = (HashMap) accountMappingHelper.updateOfficeAccountMapping(transferAccountMappingId, officeId,
                transferAccount2.getAccountID(), CommonConstants.RESPONSE_CHANGES);
        Assert.assertEquals(transferAccount2.getAccountID(), changes.get("liabilityTransferInSuspenseAccountId"));

        mappingDetails = accountMappingHelper.getOfficeAccountMapping(transferAccountMappingId);
        Assert.assertEquals(officeId, ((HashMap) mappingDetails.get("officeData")).get("id"));
        Assert.assertEquals(transferAccount2.getAccountID(), ((HashMap) mappingDetails.get("glAccountData")).get("id"));

        List<HashMap> error1 = (List<HashMap>) accountMappingHelper.updateOfficeAccountMapping(transferAccountMappingId, officeId2,
                transferAccount2.getAccountID(), CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.officeToAccountMapping.update.of.officeId.is.not.supported",
                error1.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        List<HashMap> error2 = (List<HashMap>) accountMappingHelper.createOfficeAccountMapping(officeId, transferAccount.getAccountID(),
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.officeToAccountMapping.exists.for.office", error2.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        Account fundAccount = accountHelper.createAssetAccount();
        Assert.assertNotNull(fundAccount);

        List<HashMap> error3 = (List<HashMap>) accountMappingHelper.createOfficeAccountMapping(officeId2, fundAccount.getAccountID(),
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.liabilityTransferInSuspenseAccountId.invalid.account.type",
                error3.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }**/

}
