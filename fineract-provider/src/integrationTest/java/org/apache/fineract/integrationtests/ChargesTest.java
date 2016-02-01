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
package org.apache.fineract.integrationtests;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    public void testChargesForLoans() {

        // Retrieving all Charges
        ArrayList<HashMap> allChargesData = ChargesHelper.getCharges(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(allChargesData);

        // Testing Creation, Updation and Deletion of Disbursement Charge
        final Integer disbursementChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanDisbursementJSON());
        Assert.assertNotNull(disbursementChargeId);

        // Updating Charge Amount
        HashMap changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, disbursementChargeId,
                ChargesHelper.getModifyChargeJSON());

        HashMap chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, disbursementChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, disbursementChargeId,
                ChargesHelper.getModifyChargeAsPecentageAmountJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, disbursementChargeId);

        HashMap chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargePaymentMode");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargePaymentMode"));

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, disbursementChargeId,
                ChargesHelper.getModifyChargeAsPecentageLoanAmountWithInterestJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, disbursementChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, disbursementChargeId,
                ChargesHelper.getModifyChargeAsPercentageInterestJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, disbursementChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        Integer chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, disbursementChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", disbursementChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Specified due date Charge
        final Integer specifiedDueDateChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON());
        Assert.assertNotNull(specifiedDueDateChargeId);

        // Updating Charge Amount
        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, specifiedDueDateChargeId,
                ChargesHelper.getModifyChargeJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, specifiedDueDateChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, specifiedDueDateChargeId,
                ChargesHelper.getModifyChargeAsPecentageAmountJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, specifiedDueDateChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargePaymentMode");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargePaymentMode"));

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, specifiedDueDateChargeId,
                ChargesHelper.getModifyChargeAsPecentageLoanAmountWithInterestJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, specifiedDueDateChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, specifiedDueDateChargeId,
                ChargesHelper.getModifyChargeAsPercentageInterestJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, specifiedDueDateChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, specifiedDueDateChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", specifiedDueDateChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Installment Fee Charge
        final Integer installmentFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanInstallmentFeeJSON());

        // Updating Charge Amount
        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, installmentFeeChargeId,
                ChargesHelper.getModifyChargeJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, installmentFeeChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, installmentFeeChargeId,
                ChargesHelper.getModifyChargeAsPecentageAmountJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, installmentFeeChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargePaymentMode");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargePaymentMode"));

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, installmentFeeChargeId,
                ChargesHelper.getModifyChargeAsPecentageLoanAmountWithInterestJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, installmentFeeChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, installmentFeeChargeId,
                ChargesHelper.getModifyChargeAsPercentageInterestJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, installmentFeeChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, installmentFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", installmentFeeChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Overdue Installment Fee
        // Charge
        final Integer overdueFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanOverdueFeeJSON());
        Assert.assertNotNull(overdueFeeChargeId);

        // Updating Charge Amount
        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, overdueFeeChargeId, ChargesHelper.getModifyChargeJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, overdueFeeChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, overdueFeeChargeId,
                ChargesHelper.getModifyChargeAsPecentageAmountJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, overdueFeeChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargePaymentMode");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargePaymentMode"));

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, overdueFeeChargeId,
                ChargesHelper.getModifyChargeAsPecentageLoanAmountWithInterestJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, overdueFeeChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, overdueFeeChargeId,
                ChargesHelper.getModifyChargeAsPercentageInterestJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, overdueFeeChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, overdueFeeChargeId,
                ChargesHelper.getModifyChargeFeeFrequencyAsYearsJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, overdueFeeChargeId);

        chargeChangedData = (HashMap) chargeDataAfterChanges.get("feeFrequency");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("feeFrequency"));

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, overdueFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", overdueFeeChargeId, chargeIdAfterDeletion);
    }

    @Test
    public void testChargesForSavings() {

        // Testing Creation, Updation and Deletion of Specified due date Charge
        final Integer specifiedDueDateChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsSpecifiedDueDateJSON());
        Assert.assertNotNull(specifiedDueDateChargeId);

        // Updating Charge Amount
        HashMap changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, specifiedDueDateChargeId,
                ChargesHelper.getModifyChargeJSON());

        HashMap chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, specifiedDueDateChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        Integer chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, specifiedDueDateChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", specifiedDueDateChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Savings Activation Charge
        final Integer savingsActivationChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsActivationFeeJSON());
        Assert.assertNotNull(savingsActivationChargeId);

        // Updating Charge Amount
        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, savingsActivationChargeId,
                ChargesHelper.getModifyChargeJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, savingsActivationChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, savingsActivationChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", savingsActivationChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Charge for Withdrawal Fee
        final Integer withdrawalFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsWithdrawalFeeJSON());
        Assert.assertNotNull(withdrawalFeeChargeId);

        // Updating Charge-Calculation-Type to Withdrawal-Fee
        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, withdrawalFeeChargeId,
                ChargesHelper.getModifyWithdrawalFeeSavingsChargeJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, withdrawalFeeChargeId);

        HashMap chargeChangedData = (HashMap) chargeDataAfterChanges.get("chargeCalculationType");
        Assert.assertEquals("Verifying Charge after Modification", chargeChangedData.get("id"), changes.get("chargeCalculationType"));

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, withdrawalFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", withdrawalFeeChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Charge for Annual Fee
        final Integer annualFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsAnnualFeeJSON());
        Assert.assertNotNull(annualFeeChargeId);

        // Updating Charge Amount
        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, annualFeeChargeId, ChargesHelper.getModifyChargeJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, annualFeeChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, annualFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", annualFeeChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Charge for Monthly Fee
        final Integer monthlyFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsMonthlyFeeJSON());
        Assert.assertNotNull(monthlyFeeChargeId);

        // Updating Charge Amount
        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, monthlyFeeChargeId, ChargesHelper.getModifyChargeJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, monthlyFeeChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, monthlyFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", monthlyFeeChargeId, chargeIdAfterDeletion);

        // Testing Creation, Updation and Deletion of Charge for Overdraft Fee
        final Integer overdraftFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsOverdraftFeeJSON());
        Assert.assertNotNull(overdraftFeeChargeId);

        // Updating Charge Amount
        changes = ChargesHelper.updateCharges(this.requestSpec, this.responseSpec, overdraftFeeChargeId,
                ChargesHelper.getModifyChargeJSON());

        chargeDataAfterChanges = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, overdraftFeeChargeId);
        Assert.assertEquals("Verifying Charge after Modification", chargeDataAfterChanges.get("amount"), changes.get("amount"));

        chargeIdAfterDeletion = ChargesHelper.deleteCharge(this.responseSpec, this.requestSpec, overdraftFeeChargeId);
        Assert.assertEquals("Verifying Charge ID after deletion", overdraftFeeChargeId, chargeIdAfterDeletion);
    }
}
