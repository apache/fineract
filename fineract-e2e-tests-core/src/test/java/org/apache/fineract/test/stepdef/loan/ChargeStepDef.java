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
package org.apache.fineract.test.stepdef.loan;

import io.cucumber.java.en.When;
import java.io.IOException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.PutChargesChargeIdResponse;
import org.apache.fineract.client.services.ChargesApi;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class ChargeStepDef extends AbstractStepDef {

    @Autowired
    private ChargesApi chargesApi;

    @When("Admin updates charge {string} with {string} calculation type and {double} % of transaction amount")
    public void updateCharge(String chargeType, String chargeCalculationType, double amount) throws IOException {
        ChargeRequest disbursementChargeUpdateRequest = new ChargeRequest();
        ChargeCalculationType chargeProductTypeValue = ChargeCalculationType.valueOf(chargeCalculationType);
        disbursementChargeUpdateRequest.chargeCalculationType(chargeProductTypeValue.value).amount(amount).locale("en");

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeId = chargeProductType.getValue();

        Response<PutChargesChargeIdResponse> responseDisbursementCharge = chargesApi.updateCharge(chargeId, disbursementChargeUpdateRequest)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(responseDisbursementCharge);
    }

    @When("Admin updates charge {string} with {string} calculation type and {double} EUR amount")
    public void updateChargeWithFlatAmount(String chargeType, String chargeCalculationType, double flatAmount) throws IOException {
        ChargeRequest disbursementChargeUpdateRequest = new ChargeRequest();
        ChargeCalculationType chargeProductTypeValue = ChargeCalculationType.valueOf(chargeCalculationType);
        disbursementChargeUpdateRequest.chargeCalculationType(chargeProductTypeValue.value).amount(flatAmount).locale("en");

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeId = chargeProductType.getValue();

        Response<PutChargesChargeIdResponse> responseDisbursementCharge = chargesApi.updateCharge(chargeId, disbursementChargeUpdateRequest)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(responseDisbursementCharge);
    }
}
