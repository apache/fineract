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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsTemplateResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansTemplateResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers the optional {@code charges} parameter of the Working Capital loan product, mirroring how a term loan product
 * declares the charges it offers. The association is a catalogue: it never constrains what can later be applied to an
 * account, so these tests assert the product contract only.
 */
public class WorkingCapitalLoanProductChargesTest {

    private static final String WCLP_CHARGE_NOT_APPLICABLE = "error.msg.wclp.charge.not.applicable.to.working.capital.loan";
    private static final String WCLP_CHARGE_INVALID_CURRENCY = "error.msg.charge.attach.to.working.capital.loan.product.invalid.currency";

    private static final Integer CHARGE_APPLIES_TO_LOAN = 1;
    private static final Integer CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE = 2;
    private static final Integer CHARGE_CALCULATION_TYPE_FLAT = 1;
    // Mandatory for chargeAppliesTo=LOAN; Working Capital charges default it to REGULAR instead.
    private static final Integer CHARGE_PAYMENT_MODE_REGULAR = 0;

    private WorkingCapitalLoanProductHelper productHelper;
    private WorkingCapitalLoanHelper loanHelper;
    private FeignChargesHelper chargesHelper;

    private final List<Long> createdProductIds = new ArrayList<>();
    private final List<Long> createdChargeIds = new ArrayList<>();

    @BeforeEach
    public void setup() {
        this.productHelper = new WorkingCapitalLoanProductHelper();
        this.loanHelper = new WorkingCapitalLoanHelper();
        this.chargesHelper = new FeignChargesHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @AfterEach
    public void cleanup() {
        // Products first: a charge attached to a product cannot be deleted.
        createdProductIds.forEach(productId -> {
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final RuntimeException ignored) {
                // best effort, the assertions already ran
            }
        });
        createdProductIds.clear();
        createdChargeIds.forEach(chargeId -> {
            try {
                chargesHelper.deleteCharge(chargeId);
            } catch (final RuntimeException ignored) {
                // best effort, the assertions already ran
            }
        });
        createdChargeIds.clear();
    }

    @Test
    public void createProductWithChargesPersistsAndReturnsThem() {
        final Long chargeId = createWorkingCapitalFee();

        final Long productId = createProduct(new WorkingCapitalLoanProductTestBuilder().withChargeIds(List.of(chargeId)));

        final GetWorkingCapitalLoanProductsProductIdResponse product = productHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertNotNull(product.getCharges(), "the product must expose the charges it offers");
        assertEquals(1, product.getCharges().size());
        assertEquals(chargeId, product.getCharges().get(0).getId());
    }

    @Test
    public void createProductWithoutChargesLeavesTheProductWithoutAny() {
        final Long productId = createProduct(new WorkingCapitalLoanProductTestBuilder());

        final GetWorkingCapitalLoanProductsProductIdResponse product = productHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertTrue(product.getCharges() == null || product.getCharges().isEmpty(),
                "charges is optional, omitting it must leave the product without charges");
    }

    @Test
    public void createProductWithSeveralChargesKeepsAllOfThem() {
        final Long feeId = createWorkingCapitalFee();
        final Long penaltyId = createWorkingCapitalPenalty();

        final Long productId = createProduct(new WorkingCapitalLoanProductTestBuilder().withChargeIds(List.of(feeId, penaltyId)));

        final GetWorkingCapitalLoanProductsProductIdResponse product = productHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertNotNull(product.getCharges());
        final List<Long> attachedIds = product.getCharges().stream().map(ChargeData::getId).toList();
        assertEquals(2, attachedIds.size());
        assertTrue(attachedIds.containsAll(List.of(feeId, penaltyId)), "both charges must be attached, got " + attachedIds);
    }

    @Test
    public void updateProductChargesReplacesTheWholeList() {
        final Long firstChargeId = createWorkingCapitalFee();
        final Long secondChargeId = createWorkingCapitalFee();

        final String name = uniqueName();
        final String shortName = uniqueShortName();
        final Long productId = createProduct(new WorkingCapitalLoanProductTestBuilder().withName(name).withShortName(shortName)
                .withChargeIds(List.of(firstChargeId)).build());

        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new WorkingCapitalLoanProductTestBuilder().withName(name)
                .withShortName(shortName).withChargeIds(List.of(secondChargeId)).buildUpdateRequest();
        productHelper.updateWorkingCapitalLoanProductById(productId, updateRequest);

        final GetWorkingCapitalLoanProductsProductIdResponse product = productHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertNotNull(product.getCharges());
        assertEquals(1, product.getCharges().size(), "the update replaces the list rather than appending to it");
        assertEquals(secondChargeId, product.getCharges().get(0).getId());
    }

    @Test
    public void updateProductWithEmptyChargesDetachesAllOfThem() {
        final Long chargeId = createWorkingCapitalFee();

        final String name = uniqueName();
        final String shortName = uniqueShortName();
        final Long productId = createProduct(new WorkingCapitalLoanProductTestBuilder().withName(name).withShortName(shortName)
                .withChargeIds(List.of(chargeId)).build());

        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new WorkingCapitalLoanProductTestBuilder().withName(name)
                .withShortName(shortName).withChargeIds(List.of()).buildUpdateRequest();
        productHelper.updateWorkingCapitalLoanProductById(productId, updateRequest);

        final GetWorkingCapitalLoanProductsProductIdResponse product = productHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertTrue(product.getCharges() == null || product.getCharges().isEmpty(), "an empty charges array detaches every charge");
    }

    @Test
    public void updateWithoutChargesParameterKeepsTheExistingCharges() {
        final Long chargeId = createWorkingCapitalFee();

        final String name = uniqueName();
        final String shortName = uniqueShortName();
        final Long productId = createProduct(new WorkingCapitalLoanProductTestBuilder().withName(name).withShortName(shortName)
                .withChargeIds(List.of(chargeId)).build());

        // The builder omits the parameter entirely when no charge ids are set.
        final PutWorkingCapitalLoanProductsProductIdRequest updateRequest = new WorkingCapitalLoanProductTestBuilder()
                .withName(name + " renamed").withShortName(shortName).buildUpdateRequest();
        productHelper.updateWorkingCapitalLoanProductById(productId, updateRequest);

        final GetWorkingCapitalLoanProductsProductIdResponse product = productHelper.retrieveWorkingCapitalLoanProductById(productId);
        assertNotNull(product.getCharges(), "omitting charges must not detach them");
        assertEquals(1, product.getCharges().size());
        assertEquals(chargeId, product.getCharges().get(0).getId());
    }

    @Test
    public void attachingATermLoanChargeIsRejected() {
        final Long termLoanChargeId = createTermLoanFee();

        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName())
                .withShortName(uniqueShortName()).withChargeIds(List.of(termLoanChargeId)).build();

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> productHelper.createWorkingCapitalLoanProduct(request));
        assertTrue(exception.getResponseBody().contains(WCLP_CHARGE_NOT_APPLICABLE),
                "only charges defined for Working Capital loans may be attached, got: " + exception.getResponseBody());
    }

    @Test
    public void attachingAChargeInAnotherCurrencyIsRejected() {
        final Long eurChargeId = createWorkingCapitalFeeInCurrency("EUR");

        final PostWorkingCapitalLoanProductsRequest request = new WorkingCapitalLoanProductTestBuilder().withName(uniqueName())
                .withShortName(uniqueShortName()).withCurrencyCode("USD").withChargeIds(List.of(eurChargeId)).build();

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> productHelper.createWorkingCapitalLoanProduct(request));
        assertTrue(exception.getResponseBody().contains(WCLP_CHARGE_INVALID_CURRENCY),
                "charge and product must share the currency, got: " + exception.getResponseBody());
    }

    @Test
    public void deletingAChargeAttachedToAProductIsRejected() {
        final Long chargeId = createWorkingCapitalFee();
        createProduct(new WorkingCapitalLoanProductTestBuilder().withChargeIds(List.of(chargeId)));

        assertThrows(CallFailedRuntimeException.class, () -> chargesHelper.deleteCharge(chargeId),
                "a charge offered by a Working Capital product must not be deletable");
    }

    @Test
    public void productTemplateOffersWorkingCapitalChargesOnly() {
        final Long wcFeeId = createWorkingCapitalFee();
        final Long wcPenaltyId = createWorkingCapitalPenalty();
        final Long termLoanChargeId = createTermLoanFee();

        final GetWorkingCapitalLoanProductsTemplateResponse template = productHelper.retrieveTemplate();

        assertNotNull(template.getChargeOptions(), "the product template must offer the Working Capital fees");
        final List<Long> feeOptionIds = template.getChargeOptions().stream().map(ChargeData::getId).toList();
        assertTrue(feeOptionIds.contains(wcFeeId), "chargeOptions must contain the Working Capital fee");
        assertFalse(feeOptionIds.contains(termLoanChargeId), "chargeOptions must not leak term loan charges");
        assertFalse(feeOptionIds.contains(wcPenaltyId), "chargeOptions carries fees, penalties go to penaltyOptions");

        assertNotNull(template.getPenaltyOptions(), "the product template must offer the Working Capital penalties");
        final List<Long> penaltyOptionIds = template.getPenaltyOptions().stream().map(ChargeData::getId).toList();
        assertTrue(penaltyOptionIds.contains(wcPenaltyId), "penaltyOptions must contain the Working Capital penalty");
        assertFalse(penaltyOptionIds.contains(wcFeeId), "penaltyOptions must not contain fees");
    }

    @Test
    public void accountTemplateCarriesTheChargesOfTheSelectedProduct() {
        final Long chargeId = createWorkingCapitalFee();
        final Long productId = createProduct(new WorkingCapitalLoanProductTestBuilder().withChargeIds(List.of(chargeId)));

        final GetWorkingCapitalLoansTemplateResponse template = loanHelper.retrieveTemplateRaw(Map.of("productId", productId));

        assertNotNull(template.getLoanData(), "the account template must carry the loan data");
        assertNotNull(template.getLoanData().getProduct(), "selecting a product must expand it in the account template");
        final List<ChargeData> charges = template.getLoanData().getProduct().getCharges();
        assertNotNull(charges, "the account template must expose the charges the product offers");
        assertEquals(1, charges.size());
        assertEquals(chargeId, charges.get(0).getId());
    }

    private Long createProduct(final WorkingCapitalLoanProductTestBuilder builder) {
        final PostWorkingCapitalLoanProductsRequest request = builder.withName(uniqueName()).withShortName(uniqueShortName()).build();
        return createProduct(request);
    }

    private Long createProduct(final PostWorkingCapitalLoanProductsRequest request) {
        final Long productId = productHelper.createWorkingCapitalLoanProduct(request).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createWorkingCapitalFee() {
        return trackCharge(
                chargesHelper.createCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 15.0)).getResourceId());
    }

    private Long createWorkingCapitalPenalty() {
        return trackCharge(
                chargesHelper.createCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(true, 25.0)).getResourceId());
    }

    private Long createWorkingCapitalFeeInCurrency(final String currencyCode) {
        final ChargeRequest request = WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 15.0).currencyCode(currencyCode);
        return trackCharge(chargesHelper.createCharge(request).getResourceId());
    }

    private Long createTermLoanFee() {
        final ChargeRequest request = new ChargeRequest().chargeAppliesTo(CHARGE_APPLIES_TO_LOAN)
                .chargeTimeType(CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE).chargeCalculationType(CHARGE_CALCULATION_TYPE_FLAT)
                .name(Utils.uniqueRandomStringGenerator("TERM_LOAN_CHARGE_", 8)).amount(15.0).active(true).currencyCode("USD")
                .chargePaymentMode(CHARGE_PAYMENT_MODE_REGULAR).penalty(false).locale("en");
        return trackCharge(chargesHelper.createCharge(request).getResourceId());
    }

    private Long trackCharge(final Long chargeId) {
        createdChargeIds.add(chargeId);
        return chargeId;
    }

    private static String uniqueName() {
        return "WCP Charges Product " + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String uniqueShortName() {
        return Utils.uniqueRandomStringGenerator("", 4);
    }
}
