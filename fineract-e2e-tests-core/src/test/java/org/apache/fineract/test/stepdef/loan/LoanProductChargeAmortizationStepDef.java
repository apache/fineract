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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeUpdateRequest;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.test.data.AccountingRule;
import org.apache.fineract.test.data.accounttype.AccountTypeResolver;
import org.apache.fineract.test.data.accounttype.DefaultAccountType;
import org.apache.fineract.test.factory.LoanProductsRequestFactory;
import org.apache.fineract.test.stepdef.AbstractStepDef;

/**
 * Steps for the {@code enableChargeAmortization} flag of a loan product. The charge used is the one created by
 * {@link ChargeAmortizationStepDef}. In the data tables an empty cell means the field is not sent.
 */
@RequiredArgsConstructor
public class LoanProductChargeAmortizationStepDef extends AbstractStepDef {

    private static final String PRODUCT_ID = "chargeAmortizationLoanProductId";
    private static final String LOCALE_EN = "en";
    private static final String LOAN_SCHEDULE_TYPE = "loanScheduleType";
    private static final String ACCOUNTING_RULE = "accountingRule";
    private static final String ENABLE_CHARGE_AMORTIZATION = "enableChargeAmortization";
    private static final String DEFERRED_INCOME_LIABILITY_MAPPED = "deferredIncomeLiabilityMapped";
    private static final String ATTACH_CHARGE = "attachCharge";

    private final FineractFeignClient fineractClient;
    private final LoanProductsRequestFactory loanProductsRequestFactory;
    private final AccountTypeResolver accountTypeResolver;

    @When("Admin creates a loan product with the following charge amortization settings:")
    public void createLoanProduct(final DataTable table) {
        final PostLoanProductsRequest request = productRequest(table.asMaps().get(0));
        final Long productId = ok(() -> fineractClient.loanProducts().createLoanProduct(request)).getResourceId();
        testContext().set(PRODUCT_ID, productId);
    }

    @Then("Creating a loan product with the following charge amortization settings results an error:")
    public void createLoanProductFails(final DataTable table) {
        final Map<String, String> data = table.asMaps().get(0);
        final PostLoanProductsRequest request = productRequest(data);
        assertFailure(fail(() -> fineractClient.loanProducts().createLoanProduct(request)), data);
    }

    @When("Admin updates the loan product with the following charge amortization settings:")
    public void updateLoanProduct(final DataTable table) {
        final PutLoanProductsProductIdRequest request = updateRequest(table.asMaps().get(0));
        ok(() -> fineractClient.loanProducts().updateLoanProduct(createdProductId(), request));
    }

    @Then("Updating the loan product with the following charge amortization settings results an error:")
    public void updateLoanProductFails(final DataTable table) {
        final Map<String, String> data = table.asMaps().get(0);
        final PutLoanProductsProductIdRequest request = updateRequest(data);
        assertFailure(fail(() -> fineractClient.loanProducts().updateLoanProduct(createdProductId(), request)), data);
    }

    @Then("The loan product has charge amortization {string} and the deferred income liability account mapped {string}")
    public void verifyLoanProduct(final String enabled, final String deferredIncomeLiabilityMapped) {
        final GetLoanProductsProductIdResponse product = ok(() -> fineractClient.loanProducts().retrieveOneLoanProduct(createdProductId()));
        assertThat(product.getEnableChargeAmortization()).as(ENABLE_CHARGE_AMORTIZATION).isEqualTo(Boolean.valueOf(enabled));
        final boolean mapped = product.getAccountingMappings() != null
                && product.getAccountingMappings().getDeferredIncomeLiabilityAccount() != null;
        assertThat(mapped).as("deferred income liability account mapped").isEqualTo(Boolean.parseBoolean(deferredIncomeLiabilityMapped));
    }

    @Then("Amortizing the charge over the full loan term results an error:")
    public void amortizeChargeFails(final DataTable table) {
        final ChargeUpdateRequest request = new ChargeUpdateRequest().locale(LOCALE_EN).amortizationPeriodType("FULL_LOAN_TERM");
        assertFailure(fail(() -> fineractClient.charges().updateCharge(createdChargeId(), request)), table.asMaps().get(0));
    }

    private PostLoanProductsRequest productRequest(final Map<String, String> data) {
        final PostLoanProductsRequest request = "CUMULATIVE".equals(cell(data, LOAN_SCHEDULE_TYPE))
                ? loanProductsRequestFactory.defaultLoanProductsRequestLP1()
                : loanProductsRequestFactory.defaultLoanProductsRequestLP2EmiAdvancedPaymentAllocation();
        final String accountingRule = cell(data, ACCOUNTING_RULE);
        if (accountingRule != null) {
            request.accountingRule(AccountingRule.valueOf(accountingRule).value);
            if (!accountingRule.startsWith("ACCRUAL")) {
                // accrual activity posting may not even be sent for a product without accrual accounting
                request.enableAccrualActivityPosting(null);
            }
        }
        final String enableChargeAmortization = cell(data, ENABLE_CHARGE_AMORTIZATION);
        return request.enableChargeAmortization(enableChargeAmortization == null ? null : Boolean.valueOf(enableChargeAmortization)) //
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccountId(data)) //
                .charges(Boolean.parseBoolean(cell(data, ATTACH_CHARGE)) ? List.of(new LoanProductChargeData().id(createdChargeId()))
                        : List.of());
    }

    private PutLoanProductsProductIdRequest updateRequest(final Map<String, String> data) {
        final PutLoanProductsProductIdRequest request = new PutLoanProductsProductIdRequest().locale(LOCALE_EN)
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccountId(data));
        final String accountingRule = cell(data, ACCOUNTING_RULE);
        if (accountingRule != null) {
            request.accountingRule(AccountingRule.valueOf(accountingRule).value);
        }
        final String enableChargeAmortization = cell(data, ENABLE_CHARGE_AMORTIZATION);
        if (enableChargeAmortization != null) {
            request.enableChargeAmortization(Boolean.valueOf(enableChargeAmortization));
        }
        if (Boolean.parseBoolean(cell(data, ATTACH_CHARGE))) {
            request.currencyCode(LoanProductsRequestFactory.CURRENCY_CODE)
                    .charges(List.of(new LoanProductChargeData().id(createdChargeId())));
        }
        return request;
    }

    private Long deferredIncomeLiabilityAccountId(final Map<String, String> data) {
        return Boolean.parseBoolean(cell(data, DEFERRED_INCOME_LIABILITY_MAPPED))
                ? accountTypeResolver.resolve(DefaultAccountType.DEFERRED_CAPITALIZED_INCOME)
                : null;
    }

    private Long createdProductId() {
        final Long productId = testContext().get(PRODUCT_ID);
        assertThat(productId).as("a loan product must be created first in this scenario").isNotNull();
        return productId;
    }

    private Long createdChargeId() {
        final Long chargeId = testContext().get(ChargeAmortizationStepDef.CHARGE_ID);
        assertThat(chargeId).as("a charge must be created first in this scenario").isNotNull();
        return chargeId;
    }

    private static void assertFailure(final CallFailedRuntimeException failure, final Map<String, String> data) {
        final int expectedHttpCode = Integer.parseInt(data.get("httpCode"));
        final String expectedErrorMessage = data.get("errorMessage").trim();
        assertThat(failure.getStatus()).as("HTTP status code, response: " + failure.getMessage()).isEqualTo(expectedHttpCode);
        assertThat(failure.getMessage()).as("Error message should contain: " + expectedErrorMessage).contains(expectedErrorMessage);
    }

    private static String cell(final Map<String, String> data, final String column) {
        final String value = data.get(column);
        return StringUtils.isBlank(value) ? null : value.trim();
    }
}
