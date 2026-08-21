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
package org.apache.fineract.test.helper;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsTemplateResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.WorkingCapitalLoanProductChargeToGLAccountMapper;
import org.apache.fineract.client.models.WorkingCapitalPostChargeOffReasonToExpenseAccountMappings;
import org.apache.fineract.client.models.WorkingCapitalPostWriteOffReasonToExpenseAccountMappings;
import org.apache.fineract.test.data.ChargeCalculationType;
import org.apache.fineract.test.data.ChargePaymentMode;
import org.apache.fineract.test.data.ChargeTimeType;
import org.apache.fineract.test.data.GLAType;
import org.apache.fineract.test.data.GLAUsage;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.factory.GLAccountRequestFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public final class WorkingCapitalLoanProductAdvancedAccountingTestHelper {

    private WorkingCapitalLoanProductAdvancedAccountingTestHelper() {}

    public static void assertTemplateHasOptions(final GetWorkingCapitalLoanProductsTemplateResponse template) {
        final JsonNode root = toTree(new JsonMapper(), template);
        assertThat(root.path("paymentTypeOptions").isArray()).as("paymentTypeOptions must be present in template").isTrue();
        assertThat(root.path("chargeOffReasonOptions").isArray()).as("chargeOffReasonOptions must be present in template").isTrue();
        assertThat(root.path("writeOffReasonOptions").isArray()).as("writeOffReasonOptions must be present in template").isTrue();
        assertThat(root.path("paymentTypeOptions").isEmpty()).as("paymentTypeOptions must not be empty").isFalse();
        assertThat(root.path("chargeOffReasonOptions").isEmpty()).as("chargeOffReasonOptions must not be empty").isFalse();
        assertThat(root.path("writeOffReasonOptions").isEmpty()).as("writeOffReasonOptions must not be empty").isFalse();
    }

    public static void assertProductHasAdvancedMappings(final ObjectMapper objectMapper,
            final GetWorkingCapitalLoanProductsProductIdResponse product) {
        final JsonNode root = toTree(objectMapper, product);
        assertNonEmptyArray(root, "paymentChannelToFundSourceMappings");
        assertNonEmptyArray(root, "feeToIncomeAccountMappings");
        assertNonEmptyArray(root, "penaltyToIncomeAccountMappings");
        assertNonEmptyArray(root, "chargeOffReasonToExpenseAccountMappings");
        assertNonEmptyArray(root, "writeOffReasonsToExpenseMappings");
    }

    public static AdvancedAccountingExpectation prepareAdvancedMappings(final PostWorkingCapitalLoanProductsRequest request,
            final PaymentTypeResolver paymentTypeResolver, final FineractFeignClient fineractFeignClient) {
        return prepareAdvancedMappingsInternal(request::setFundSourceAccountId, request::setIncomeFromFeeAccountId,
                request::setIncomeFromPenaltyAccountId, request::setWriteOffAccountId, request::setPaymentChannelToFundSourceMappings,
                request::setFeeToIncomeAccountMappings, request::setPenaltyToIncomeAccountMappings,
                request::setChargeOffReasonToExpenseAccountMappings, request::setWriteOffReasonsToExpenseMappings, paymentTypeResolver,
                fineractFeignClient);
    }

    public static AdvancedAccountingExpectation prepareAdvancedMappings(final PutWorkingCapitalLoanProductsProductIdRequest request,
            final PaymentTypeResolver paymentTypeResolver, final FineractFeignClient fineractFeignClient) {
        return prepareAdvancedMappingsInternal(request::setFundSourceAccountId, request::setIncomeFromFeeAccountId,
                request::setIncomeFromPenaltyAccountId, request::setWriteOffAccountId, request::setPaymentChannelToFundSourceMappings,
                request::setFeeToIncomeAccountMappings, request::setPenaltyToIncomeAccountMappings,
                request::setChargeOffReasonToExpenseAccountMappings, request::setWriteOffReasonsToExpenseMappings, paymentTypeResolver,
                fineractFeignClient);
    }

    private static AdvancedAccountingExpectation prepareAdvancedMappingsInternal(final Consumer<Long> setFundSourceAccountId,
            final Consumer<Long> setFeeIncomeAccountId, final Consumer<Long> setPenaltyIncomeAccountId,
            final Consumer<Long> setWriteOffAccountId,
            final Consumer<List<WorkingCapitalLoanPaymentChannelToFundSourceMappings>> setPaymentChannelMappings,
            final Consumer<List<WorkingCapitalLoanProductChargeToGLAccountMapper>> setFeeMappings,
            final Consumer<List<WorkingCapitalLoanProductChargeToGLAccountMapper>> setPenaltyMappings,
            final Consumer<List<WorkingCapitalPostChargeOffReasonToExpenseAccountMappings>> setChargeOffMappings,
            final Consumer<List<WorkingCapitalPostWriteOffReasonToExpenseAccountMappings>> setWriteOffMappings,
            final PaymentTypeResolver paymentTypeResolver, final FineractFeignClient fineractFeignClient) {
        final Long fundSourceAccountId = resolveOrCreateGLAccount(fineractFeignClient, "WC E2E Mapping Fund Source", "WCE2EFS1",
                GLAType.ASSET.value);
        final Long feeIncomeAccountId = resolveOrCreateGLAccount(fineractFeignClient, "WC E2E Mapping Fee Income", "WCE2EFI1",
                GLAType.INCOME.value);
        final Long penaltyIncomeAccountId = resolveOrCreateGLAccount(fineractFeignClient, "WC E2E Mapping Penalty Income", "WCE2EPI1",
                GLAType.INCOME.value);
        final Long writeOffExpenseAccountId = resolveOrCreateGLAccount(fineractFeignClient, "WC E2E Mapping Write Off Expense", "WCE2EWE1",
                GLAType.EXPENSE.value);

        setFundSourceAccountId.accept(fundSourceAccountId);
        setFeeIncomeAccountId.accept(feeIncomeAccountId);
        setPenaltyIncomeAccountId.accept(penaltyIncomeAccountId);
        setWriteOffAccountId.accept(writeOffExpenseAccountId);

        final Long paymentTypeId = paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER);
        final Long feeChargeId = createChargeForAdvancedMappings(fineractFeignClient, false, 2.0);
        final Long penaltyChargeId = createChargeForAdvancedMappings(fineractFeignClient, true, 1.0);
        final Long chargeOffReasonId = resolveFirstCodeValueId(fineractFeignClient, "ChargeOffReasons");
        final Long writeOffReasonId = resolveFirstCodeValueId(fineractFeignClient, "WriteOffReasons");

        setPaymentChannelMappings.accept(List.of(new WorkingCapitalLoanPaymentChannelToFundSourceMappings().paymentTypeId(paymentTypeId)
                .fundSourceAccountId(fundSourceAccountId)));
        setFeeMappings.accept(
                List.of(new WorkingCapitalLoanProductChargeToGLAccountMapper().chargeId(feeChargeId).incomeAccountId(feeIncomeAccountId)));
        setPenaltyMappings.accept(List.of(
                new WorkingCapitalLoanProductChargeToGLAccountMapper().chargeId(penaltyChargeId).incomeAccountId(penaltyIncomeAccountId)));
        setChargeOffMappings.accept(List.of(new WorkingCapitalPostChargeOffReasonToExpenseAccountMappings()
                .chargeOffReasonCodeValueId(chargeOffReasonId).expenseAccountId(writeOffExpenseAccountId)));
        setWriteOffMappings.accept(List.of(new WorkingCapitalPostWriteOffReasonToExpenseAccountMappings()
                .writeOffReasonCodeValueId(writeOffReasonId).expenseAccountId(writeOffExpenseAccountId)));

        return buildExpectation(paymentTypeId, fundSourceAccountId, feeChargeId, feeIncomeAccountId, penaltyChargeId,
                penaltyIncomeAccountId, chargeOffReasonId, writeOffExpenseAccountId, writeOffReasonId, writeOffExpenseAccountId,
                fineractFeignClient);
    }

    private static AdvancedAccountingExpectation buildExpectation(final Long paymentTypeId, final Long fundSourceAccountId,
            final Long feeChargeId, final Long feeIncomeAccountId, final Long penaltyChargeId, final Long penaltyIncomeAccountId,
            final Long chargeOffReasonId, final Long chargeOffExpenseAccountId, final Long writeOffReasonId,
            final Long writeOffExpenseAccountId, final FineractFeignClient fineractFeignClient) {
        final List<GetGLAccountsResponse> glAccounts = ok(
                () -> fineractFeignClient.generalLedgerAccount().retrieveAllAccountsUniversal(Map.of()));
        final Map<Long, String> accountNameById = new HashMap<>();
        for (GetGLAccountsResponse account : glAccounts) {
            if (account != null && account.getId() != null && account.getName() != null) {
                accountNameById.putIfAbsent(account.getId(), account.getName());
            }
        }
        final String paymentTypeName = DefaultPaymentType.MONEY_TRANSFER.getName();
        final String fundSourceAccountName = accountNameById.get(fundSourceAccountId);
        final String feeIncomeAccountName = accountNameById.get(feeIncomeAccountId);
        final String penaltyIncomeAccountName = accountNameById.get(penaltyIncomeAccountId);
        final String chargeOffExpenseAccountName = accountNameById.get(chargeOffExpenseAccountId);
        final String writeOffExpenseAccountName = accountNameById.get(writeOffExpenseAccountId);

        return new AdvancedAccountingExpectation(paymentTypeId, paymentTypeName, fundSourceAccountId, fundSourceAccountName, feeChargeId,
                feeIncomeAccountId, feeIncomeAccountName, penaltyChargeId, penaltyIncomeAccountId, penaltyIncomeAccountName,
                chargeOffReasonId, chargeOffExpenseAccountId, chargeOffExpenseAccountName, writeOffReasonId, writeOffExpenseAccountId,
                writeOffExpenseAccountName);
    }

    public static void assertProductHasExpectedAdvancedMappings(final ObjectMapper objectMapper,
            final GetWorkingCapitalLoanProductsProductIdResponse product, final AdvancedAccountingExpectation expected) {
        final JsonNode root = toTree(objectMapper, product);
        assertProductHasAdvancedMappings(objectMapper, product);

        assertThat(root.path("paymentChannelToFundSourceMappings").path(0).path("paymentType").path("id").asLong())
                .as("paymentType.id mismatch").isEqualTo(expected.paymentTypeId());
        assertThat(root.path("paymentChannelToFundSourceMappings").path(0).path("paymentType").path("name").asString())
                .as("paymentType.name mismatch").isEqualTo(expected.paymentTypeName());
        assertThat(root.path("paymentChannelToFundSourceMappings").path(0).path("fundSourceAccount").path("id").asLong())
                .as("fundSourceAccount.id mismatch").isEqualTo(expected.fundSourceAccountId());
        assertThat(root.path("paymentChannelToFundSourceMappings").path(0).path("fundSourceAccount").path("name").asString())
                .as("fundSourceAccount.name mismatch").isEqualTo(expected.fundSourceAccountName());

        assertThat(root.path("feeToIncomeAccountMappings").path(0).path("charge").path("id").asLong()).as("fee charge.id mismatch")
                .isEqualTo(expected.feeChargeId());
        assertThat(root.path("feeToIncomeAccountMappings").path(0).path("incomeAccount").path("id").asLong())
                .as("fee incomeAccount.id mismatch").isEqualTo(expected.feeIncomeAccountId());
        assertThat(root.path("feeToIncomeAccountMappings").path(0).path("incomeAccount").path("name").asString())
                .as("fee incomeAccount.name mismatch").isEqualTo(expected.feeIncomeAccountName());

        assertThat(root.path("penaltyToIncomeAccountMappings").path(0).path("charge").path("id").asLong()).as("penalty charge.id mismatch")
                .isEqualTo(expected.penaltyChargeId());
        assertThat(root.path("penaltyToIncomeAccountMappings").path(0).path("incomeAccount").path("id").asLong())
                .as("penalty incomeAccount.id mismatch").isEqualTo(expected.penaltyIncomeAccountId());
        assertThat(root.path("penaltyToIncomeAccountMappings").path(0).path("incomeAccount").path("name").asString())
                .as("penalty incomeAccount.name mismatch").isEqualTo(expected.penaltyIncomeAccountName());

        assertThat(root.path("chargeOffReasonToExpenseAccountMappings").path(0).path("reasonCodeValue").path("id").asLong())
                .as("chargeOff reason id mismatch").isEqualTo(expected.chargeOffReasonId());
        assertThat(root.path("chargeOffReasonToExpenseAccountMappings").path(0).path("expenseAccount").path("id").asLong())
                .as("chargeOff expenseAccount.id mismatch").isEqualTo(expected.chargeOffExpenseAccountId());
        assertThat(root.path("chargeOffReasonToExpenseAccountMappings").path(0).path("expenseAccount").path("name").asString())
                .as("chargeOff expenseAccount.name mismatch").isEqualTo(expected.chargeOffExpenseAccountName());

        assertThat(root.path("writeOffReasonsToExpenseMappings").path(0).path("reasonCodeValue").path("id").asLong())
                .as("writeOff reason id mismatch").isEqualTo(expected.writeOffReasonId());
        assertThat(root.path("writeOffReasonsToExpenseMappings").path(0).path("expenseAccount").path("id").asLong())
                .as("writeOff expenseAccount.id mismatch").isEqualTo(expected.writeOffExpenseAccountId());
        assertThat(root.path("writeOffReasonsToExpenseMappings").path(0).path("expenseAccount").path("name").asString())
                .as("writeOff expenseAccount.name mismatch").isEqualTo(expected.writeOffExpenseAccountName());
    }

    private static Long createChargeForAdvancedMappings(final FineractFeignClient fineractFeignClient, final boolean penalty,
            final double amount) {
        final ChargeRequest request = new ChargeRequest().active(true).name(Utils.randomStringGenerator("WCLP_ADV_CHARGE_", 8))
                .chargeAppliesTo(1).chargeCalculationType(ChargeCalculationType.FLAT.value)
                .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.value).chargePaymentMode(ChargePaymentMode.REGULAR.value).penalty(penalty)
                .amount(amount).currencyCode("USD").locale("en");
        final PostChargesResponse response = ok(() -> fineractFeignClient.charges().createCharge(request));
        return response.getResourceId();
    }

    private static Long resolveOrCreateGLAccount(final FineractFeignClient fineractFeignClient, final String name, final String glCode,
            final Integer type) {
        final Function<List<GetGLAccountsResponse>, GetGLAccountsResponse> byNameOrCode = accounts -> accounts.stream()
                .filter(a -> name.equals(a.getName()) || glCode.equals(a.getGlCode())).findFirst().orElse(null);
        List<GetGLAccountsResponse> accounts = ok(() -> fineractFeignClient.generalLedgerAccount().retrieveAllAccountsUniversal(Map.of()));
        GetGLAccountsResponse existing = byNameOrCode.apply(accounts);
        if (existing != null) {
            return existing.getId();
        }

        final PostGLAccountsRequest request = GLAccountRequestFactory.defaultGLAccountRequest(name, glCode, type, GLAUsage.DETAIL.value,
                true);
        executeVoid(() -> fineractFeignClient.generalLedgerAccount().createGLAccount(request, Map.of()));

        accounts = ok(() -> fineractFeignClient.generalLedgerAccount().retrieveAllAccountsUniversal(Map.of()));
        existing = byNameOrCode.apply(accounts);
        if (existing != null) {
            return existing.getId();
        }
        throw new IllegalStateException("Unable to resolve or create GL account: " + name + " / " + glCode);
    }

    private static Long resolveFirstCodeValueId(final FineractFeignClient fineractFeignClient, final String codeName) {
        final List<GetCodeValuesDataResponse> codeValues = ok(
                () -> fineractFeignClient.codeValues().retrieveAllCodeValuesByCodeName(codeName, Map.of()));
        if (codeValues == null || codeValues.isEmpty()) {
            throw new IllegalStateException("No code values found for code: " + codeName);
        }
        return codeValues.getFirst().getId();
    }

    private static void assertNonEmptyArray(final JsonNode root, final String field) {
        final JsonNode node = root.path(field);
        assertThat(node.isArray()).as(field + " must be present").isTrue();
        assertThat(node.isEmpty()).as(field + " must not be empty").isFalse();
    }

    private static JsonNode toTree(final ObjectMapper objectMapper, final Object value) {
        return objectMapper.valueToTree(value);
    }

    public record AdvancedAccountingExpectation(Long paymentTypeId, String paymentTypeName, Long fundSourceAccountId,
            String fundSourceAccountName, Long feeChargeId, Long feeIncomeAccountId, String feeIncomeAccountName, Long penaltyChargeId,
            Long penaltyIncomeAccountId, String penaltyIncomeAccountName, Long chargeOffReasonId, Long chargeOffExpenseAccountId,
            String chargeOffExpenseAccountName, Long writeOffReasonId, Long writeOffExpenseAccountId, String writeOffExpenseAccountName) {
    }

}
