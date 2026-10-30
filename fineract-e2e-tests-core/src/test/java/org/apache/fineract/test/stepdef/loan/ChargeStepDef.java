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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.After;
import io.cucumber.java.en.When;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetChargesResponse;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargeProductResolver;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ChargeStepDef extends AbstractStepDef {

    private static final String REMEMBERED_CHARGE_PREFIX = "rememberedCharge:";

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private ChargeProductResolver chargeProductResolver;

    @When("Admin remembers charge {string} for restoration")
    public void rememberChargeForRestoration(final String chargeType) {
        final ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        final Long chargeId = chargeProductResolver.resolve(chargeProductType);
        final GetChargesResponse charge = ok(() -> fineractClient.charges().retrieveOneCharge(chargeId));
        assertThat(charge).as("Charge %s before modification", chargeId).isNotNull();
        assertThat(charge.getId()).as("Charge ID before modification").isEqualTo(chargeId);
        assertThat(charge.getAmount()).as("Charge %s amount before modification", chargeId).isNotNull();
        assertThat(charge.getChargeCalculationType()).as("Charge %s calculation type before modification", chargeId).isNotNull();
        assertThat(charge.getChargeCalculationType().getId()).as("Charge %s calculation type ID before modification", chargeId).isNotNull();
        assertThat(testContext().get().containsKey(REMEMBERED_CHARGE_PREFIX + chargeId))
                .as("Charge %s has already been remembered for restoration", chargeId).isFalse();
        testContext().set(REMEMBERED_CHARGE_PREFIX + chargeId, charge);
    }

    @After(value = "@ProgressiveLoanBackdatedPeriodicAccrual", order = Integer.MAX_VALUE - 1)
    public void restoreRememberedCharges() {
        final List<GetChargesResponse> charges = testContext().get().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(REMEMBERED_CHARGE_PREFIX)).map(entry -> (GetChargesResponse) entry.getValue())
                .toList();
        final IllegalStateException cleanupFailure = new IllegalStateException("Unable to restore remembered charges");
        charges.forEach((final GetChargesResponse charge) -> {
            try {
                final ChargeRequest request = new ChargeRequest().amount(charge.getAmount())
                        .chargeCalculationType(Math.toIntExact(charge.getChargeCalculationType().getId())).locale("en");
                ok(() -> fineractClient.charges().updateCharge(charge.getId(), request));
            } catch (final RuntimeException | AssertionError exception) {
                log.error("Failed to restore remembered charge {}", charge.getId(), exception);
                cleanupFailure.addSuppressed(new IllegalStateException("Charge " + charge.getId(), exception));
            }
        });
        if (cleanupFailure.getSuppressed().length > 0) {
            throw cleanupFailure;
        }
    }

    @When("Admin updates charge {string} with {string} calculation type and {double} % of transaction amount")
    public void updateCharge(String chargeType, String chargeCalculationType, double amount) {
        ChargeRequest disbursementChargeUpdateRequest = new ChargeRequest();
        ChargeCalculationType chargeProductTypeValue = ChargeCalculationType.valueOf(chargeCalculationType);
        disbursementChargeUpdateRequest.chargeCalculationType(chargeProductTypeValue.value).amount(amount).locale("en");

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeId = chargeProductResolver.resolve(chargeProductType);

        ok(() -> fineractClient.charges().updateCharge(chargeId, disbursementChargeUpdateRequest));
    }

    @When("Admin updates charge {string} with {string} calculation type and {double} EUR amount")
    public void updateChargeWithFlatAmount(String chargeType, String chargeCalculationType, double flatAmount) {
        ChargeRequest disbursementChargeUpdateRequest = new ChargeRequest();
        ChargeCalculationType chargeProductTypeValue = ChargeCalculationType.valueOf(chargeCalculationType);
        disbursementChargeUpdateRequest.chargeCalculationType(chargeProductTypeValue.value).amount(flatAmount).locale("en");

        ChargeProductType chargeProductType = ChargeProductType.valueOf(chargeType);
        Long chargeId = chargeProductResolver.resolve(chargeProductType);

        ok(() -> fineractClient.charges().updateCharge(chargeId, disbursementChargeUpdateRequest));
    }

}
