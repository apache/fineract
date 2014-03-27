package org.mifosplatform.integrationtests;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.charges.ChargesHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes" })
public class ChargesTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testChargesForSavings() {

        // Testing Creation, Updation and Deletion of Specified due date Charge
        final Integer specifiedDueDateChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsSpecifiedDueDateJSON());
        Assert.assertNotNull(specifiedDueDateChargeId);

        HashMap changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, specifiedDueDateChargeId,
                ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        Integer chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, specifiedDueDateChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", specifiedDueDateChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Savings Activation Charge
        final Integer savingsActivationChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsActivationFeeJSON());
        Assert.assertNotNull(savingsActivationChargeId);

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, savingsActivationChargeId,
                ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, savingsActivationChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", savingsActivationChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Charge for Withdrawal Fee
        final Integer withdrawalFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsWithdrawalFeeJSON());
        Assert.assertNotNull(withdrawalFeeChargeId);

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, withdrawalFeeChargeId,
                ChargesHelper.getModifyWithdrawalFeeChargeJSON());
        Assert.assertNotNull(changes);

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, withdrawalFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", withdrawalFeeChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Charge for Annual Fee
        final Integer annualFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsAnnualFeeJSON());
        Assert.assertNotNull(annualFeeChargeId);

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, annualFeeChargeId, ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, annualFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", annualFeeChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Charge for Monthly Fee
        final Integer monthlyFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsMonthlyFeeJSON());
        Assert.assertNotNull(monthlyFeeChargeId);

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, monthlyFeeChargeId, ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, monthlyFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", monthlyFeeChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Charge for Overdraft Fee
        final Integer overdraftFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsOverdraftFeeJSON());
        Assert.assertNotNull(overdraftFeeChargeId);

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, overdraftFeeChargeId,
                ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, overdraftFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", overdraftFeeChargeId, chargeIdAfterDeletion);
    }

    @Test
    public void testChargesForLoans() {

        // Testing Creation, Updation and Deletion of Disbursement Charge
        final Integer disbursementChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanDisbursementJSON());
        Assert.assertNotNull(disbursementChargeId);

        HashMap changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, disbursementChargeId,
                ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        Integer chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, disbursementChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", disbursementChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Specified due date Charge
        final Integer specifiedDueDateChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON());
        Assert.assertNotNull(specifiedDueDateChargeId);

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, specifiedDueDateChargeId,
                ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, specifiedDueDateChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", specifiedDueDateChargeId, chargeIdAfterDeletion);
        
        // Testing Creation, Updation and Deletion of Installment Fee Charge
        final Integer installmentFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanInstallmentFeeJSON());
        Assert.assertNotNull(installmentFeeChargeId);

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, installmentFeeChargeId,
                ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, installmentFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", installmentFeeChargeId, chargeIdAfterDeletion);
        
     // Testing Creation, Updation and Deletion of Overdue Installment Fee Charge
        final Integer overdueFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanOverdueFeeJSON());
        Assert.assertNotNull(overdueFeeChargeId);

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, overdueFeeChargeId,
                ChargesHelper.getModifyChargeJSON());
        Assert.assertNotNull(changes);

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, overdueFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", overdueFeeChargeId, chargeIdAfterDeletion);
    }
}
