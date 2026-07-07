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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetFixedDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetFixedDepositAccountsTemplateResponse;
import org.apache.fineract.client.models.GetFixedDepositProductsProductIdResponse;
import org.apache.fineract.client.models.GetRecurringDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetRecurringDepositAccountsTemplateResponse;
import org.apache.fineract.client.models.GetRecurringDepositProductsProductIdResponse;
import org.apache.fineract.client.models.PostFixedDepositAccountsRequest;
import org.apache.fineract.client.models.PostFixedDepositProductsChartSlabs;
import org.apache.fineract.client.models.PostFixedDepositProductsCharts;
import org.apache.fineract.client.models.PostFixedDepositProductsRequest;
import org.apache.fineract.client.models.PostRecurringDepositAccountsRequest;
import org.apache.fineract.client.models.PostRecurringDepositProductsChartSlabs;
import org.apache.fineract.client.models.PostRecurringDepositProductsCharts;
import org.apache.fineract.client.models.PostRecurringDepositProductsRequest;
import org.apache.fineract.client.models.PutFixedDepositProductsProductIdRequest;
import org.apache.fineract.client.models.PutRecurringDepositProductsRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSchedulerHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

public class FeignDepositProductArchivingTest extends FeignIntegrationTest {

    private static final String INACTIVE_STATUS = "loanProduct.inActive";
    private static final String MATURED_STATUS = "savingsAccountStatusType.matured";
    private static final String BEFORE_START_DATE_ERROR = "error.msg.deposit.account.application.submitted.on.date.cannot.be.before.the.deposit.product.start.date";
    private static final String AFTER_CLOSE_DATE_ERROR = "error.msg.deposit.account.application.submitted.on.date.cannot.be.after.the.deposit.product.close.date";

    @Test
    public void testFixedDepositProductCanBeArchived() throws JsonProcessingException {
        final LocalDate businessDate = businessDate();
        final Long productId = createFixedDepositProduct(businessDate.minusMonths(3));

        final GetFixedDepositAccountsTemplateResponse activeTemplate = ok(
                () -> fineractClient().fixedDepositAccount().retrieveTemplateFixedDepositAccount(null, null, null, false));
        assertThat(activeTemplate.getProductOptions().stream().anyMatch(product -> productId.equals(product.getId()))).isTrue();

        final Long clientId = new FeignClientHelper(fineractClient()).createClient(format(businessDate));
        final Long existingAccountId = ok(() -> fineractClient().fixedDepositAccount()
                .submitApplicationFixedDepositAccount(fixedDepositAccountRequest(clientId, productId, businessDate))).getResourceId();

        final LocalDate startDate = businessDate.minusDays(2);
        final LocalDate closeDate = businessDate.minusDays(1);
        ok(() -> fineractClient().fixedDepositProduct().updateFixedDepositProduct(productId, new PutFixedDepositProductsProductIdRequest()
                .startDate(format(startDate)).closeDate(format(closeDate)).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE)));

        final GetFixedDepositProductsProductIdResponse product = ok(
                () -> fineractClient().fixedDepositProduct().retrieveOneFixedDepositProduct(productId));
        assertThat(product.getStartDate()).isEqualTo(startDate);
        assertThat(product.getCloseDate()).isEqualTo(closeDate);
        assertThat(product.getStatus()).isEqualTo(INACTIVE_STATUS);

        final GetFixedDepositAccountsTemplateResponse archivedTemplate = ok(
                () -> fineractClient().fixedDepositAccount().retrieveTemplateFixedDepositAccount(null, null, null, false));
        assertThat(archivedTemplate.getProductOptions().stream().anyMatch(option -> productId.equals(option.getId()))).isFalse();

        final GetFixedDepositAccountsAccountIdResponse existingAccount = ok(
                () -> fineractClient().fixedDepositAccount().retrieveOneFixedDepositAccount(existingAccountId, false, "all"));
        assertThat(existingAccount.getId()).isEqualTo(existingAccountId);

        assertApplicationDateError(
                fail(() -> fineractClient().fixedDepositAccount()
                        .submitApplicationFixedDepositAccount(fixedDepositAccountRequest(clientId, productId, startDate.minusDays(1)))),
                BEFORE_START_DATE_ERROR);
        assertApplicationDateError(
                fail(() -> fineractClient().fixedDepositAccount()
                        .submitApplicationFixedDepositAccount(fixedDepositAccountRequest(clientId, productId, businessDate))),
                AFTER_CLOSE_DATE_ERROR);
    }

    @Test
    public void testRecurringDepositProductCanBeArchived() throws JsonProcessingException {
        final LocalDate businessDate = businessDate();
        final Long productId = createRecurringDepositProduct(businessDate.minusMonths(3));

        final GetRecurringDepositAccountsTemplateResponse activeTemplate = ok(
                () -> fineractClient().recurringDepositAccount().retrieveTemplateRecurringDepositAccount(null, null, null, false));
        assertThat(activeTemplate.getProductOptions().stream().anyMatch(product -> productId.equals(product.getId()))).isTrue();

        final Long clientId = new FeignClientHelper(fineractClient()).createClient(format(businessDate));
        final Long existingAccountId = ok(() -> fineractClient().recurringDepositAccount()
                .submitApplicationRecurringDepositAccount(recurringDepositAccountRequest(clientId, productId, businessDate)))
                .getResourceId();

        final LocalDate startDate = businessDate.minusDays(2);
        final LocalDate closeDate = businessDate.minusDays(1);
        ok(() -> fineractClient().recurringDepositProduct().updateRecurringDepositProduct(productId,
                new PutRecurringDepositProductsRequest().startDate(format(startDate)).closeDate(format(closeDate))
                        .dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE)));

        final GetRecurringDepositProductsProductIdResponse product = ok(
                () -> fineractClient().recurringDepositProduct().retrieveOneRecurringDepositProduct(productId));
        assertThat(product.getStartDate()).isEqualTo(startDate);
        assertThat(product.getCloseDate()).isEqualTo(closeDate);
        assertThat(product.getStatus()).isEqualTo(INACTIVE_STATUS);

        final GetRecurringDepositAccountsTemplateResponse archivedTemplate = ok(
                () -> fineractClient().recurringDepositAccount().retrieveTemplateRecurringDepositAccount(null, null, null, false));
        assertThat(archivedTemplate.getProductOptions().stream().anyMatch(option -> productId.equals(option.getId()))).isFalse();

        final GetRecurringDepositAccountsAccountIdResponse existingAccount = ok(
                () -> fineractClient().recurringDepositAccount().retrieveOneRecurringDepositAccount(existingAccountId, false, "all"));
        assertThat(existingAccount.getId()).isEqualTo(existingAccountId);

        assertApplicationDateError(fail(() -> fineractClient().recurringDepositAccount()
                .submitApplicationRecurringDepositAccount(recurringDepositAccountRequest(clientId, productId, startDate.minusDays(1)))),
                BEFORE_START_DATE_ERROR);
        assertApplicationDateError(
                fail(() -> fineractClient().recurringDepositAccount()
                        .submitApplicationRecurringDepositAccount(recurringDepositAccountRequest(clientId, productId, businessDate))),
                AFTER_CLOSE_DATE_ERROR);
    }

    @Test
    public void testArchivedDepositProductsRejectReinvestment() throws JsonProcessingException {
        final LocalDate businessDate = businessDate();
        final LocalDate applicationDate = businessDate.minusMonths(8);
        final Long fixedDepositProductId = createFixedDepositProduct(applicationDate.minusMonths(1));
        final Long recurringDepositProductId = createRecurringDepositProduct(applicationDate.minusMonths(1));
        final Long clientId = new FeignClientHelper(fineractClient()).createClient(format(applicationDate));

        final Long fixedDepositAccountId = ok(() -> fineractClient().fixedDepositAccount().submitApplicationFixedDepositAccount(
                fixedDepositAccountRequest(clientId, fixedDepositProductId, applicationDate).depositPeriod(6))).getResourceId();
        ok(() -> fineractClient().fixedDepositAccount().handleCommandsFixedDepositAccount(fixedDepositAccountId,
                datedCommandRequest("approvedOnDate", applicationDate), "approve"));
        ok(() -> fineractClient().fixedDepositAccount().handleCommandsFixedDepositAccount(fixedDepositAccountId,
                datedCommandRequest("activatedOnDate", applicationDate), "activate"));

        final Long recurringDepositAccountId = ok(() -> fineractClient().recurringDepositAccount().submitApplicationRecurringDepositAccount(
                recurringDepositAccountRequest(clientId, recurringDepositProductId, applicationDate).depositPeriod(6))).getResourceId();
        ok(() -> fineractClient().recurringDepositAccount().handleCommandsRecurringDepositAccount(recurringDepositAccountId,
                datedCommandRequest("approvedOnDate", applicationDate), "approve"));
        ok(() -> fineractClient().recurringDepositAccount().handleCommandsRecurringDepositAccount(recurringDepositAccountId,
                datedCommandRequest("activatedOnDate", applicationDate), "activate"));

        new FeignSchedulerHelper(fineractClient()).executeAndAwaitJob("Update Deposit Accounts Maturity details");

        final GetFixedDepositAccountsAccountIdResponse fixedDepositAccount = ok(
                () -> fineractClient().fixedDepositAccount().retrieveOneFixedDepositAccount(fixedDepositAccountId, false, "all"));
        assertThat(fixedDepositAccount.getStatus().getCode()).isEqualTo(MATURED_STATUS);
        final GetRecurringDepositAccountsAccountIdResponse recurringDepositAccount = ok(() -> fineractClient().recurringDepositAccount()
                .retrieveOneRecurringDepositAccount(recurringDepositAccountId, false, "all"));
        assertThat(recurringDepositAccount.getStatus().getCode()).isEqualTo(MATURED_STATUS);

        final LocalDate closeDate = businessDate.minusDays(1);
        ok(() -> fineractClient().fixedDepositProduct().updateFixedDepositProduct(fixedDepositProductId,
                new PutFixedDepositProductsProductIdRequest().startDate(format(applicationDate)).closeDate(format(closeDate))
                        .dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE)));
        ok(() -> fineractClient().recurringDepositProduct().updateRecurringDepositProduct(recurringDepositProductId,
                new PutRecurringDepositProductsRequest().startDate(format(applicationDate)).closeDate(format(closeDate))
                        .dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE)));

        assertApplicationDateError(fail(() -> fineractClient().fixedDepositAccount()
                .handleCommandsFixedDepositAccount(fixedDepositAccountId, reinvestmentClosureRequest(businessDate), "close")),
                AFTER_CLOSE_DATE_ERROR);
        assertApplicationDateError(fail(() -> fineractClient().recurringDepositAccount()
                .handleCommandsRecurringDepositAccount(recurringDepositAccountId, reinvestmentClosureRequest(businessDate), "close")),
                AFTER_CLOSE_DATE_ERROR);
    }

    private Long createFixedDepositProduct(final LocalDate chartStartDate) {
        final PostFixedDepositProductsChartSlabs chartSlab = new PostFixedDepositProductsChartSlabs().description("All terms").periodType(2)
                .fromPeriod(1).annualInterestRate(5.0);
        final PostFixedDepositProductsCharts chart = new PostFixedDepositProductsCharts().fromDate(format(chartStartDate))
                .dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE).chartSlabs(Set.of(chartSlab));
        final PostFixedDepositProductsRequest request = new PostFixedDepositProductsRequest().accountingRule(1).charts(Set.of(chart))
                .currencyCode("USD").depositAmount(100000L).description("Fixed deposit product for archiving test").digitsAfterDecimal(4)
                .inMultiplesOf(100).interestCalculationDaysInYearType(365).interestCalculationType(1).interestCompoundingPeriodType(4)
                .interestPostingPeriodType(4).locale(Utils.LOCALE).maxDepositTerm(10).maxDepositTermTypeId(3).minDepositTerm(6)
                .minDepositTermTypeId(2).name(Utils.uniqueRandomStringGenerator("FIXED_DEPOSIT_PRODUCT_", 6))
                .preClosurePenalApplicable(true).preClosurePenalInterest(2.0).preClosurePenalInterestOnTypeId(1)
                .shortName(Utils.uniqueRandomStringGenerator("", 4));

        return ok(() -> fineractClient().fixedDepositProduct().createFixedDepositProduct(request)).getResourceId();
    }

    private Long createRecurringDepositProduct(final LocalDate chartStartDate) {
        final PostRecurringDepositProductsChartSlabs chartSlab = new PostRecurringDepositProductsChartSlabs().description("All terms")
                .periodType(2).fromPeriod(1).annualInterestRate(5.0);
        final PostRecurringDepositProductsCharts chart = new PostRecurringDepositProductsCharts().fromDate(format(chartStartDate))
                .dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE).chartSlabs(Set.of(chartSlab));
        final PostRecurringDepositProductsRequest request = new PostRecurringDepositProductsRequest().accountingRule(1)
                .charts(Set.of(chart)).currencyCode("USD").depositAmount(100000L)
                .description("Recurring deposit product for archiving test").digitsAfterDecimal(4).inMultiplesOf(100)
                .interestCalculationDaysInYearType(365).interestCalculationType(1).interestCompoundingPeriodType(4)
                .interestPostingPeriodType(4).locale(Utils.LOCALE).maxDepositAmount(1000000L).maxDepositTerm(10).maxDepositTermTypeId(3)
                .minDepositAmount(100L).minDepositTerm(6).minDepositTermTypeId(2)
                .name(Utils.uniqueRandomStringGenerator("RECURRING_DEPOSIT_PRODUCT_", 6)).preClosurePenalApplicable(true)
                .preClosurePenalInterest(2.0).preClosurePenalInterestOnTypeId(1).shortName(Utils.uniqueRandomStringGenerator("", 4));

        return ok(() -> fineractClient().recurringDepositProduct().createRecurringDepositProduct(request)).getResourceId();
    }

    private PostFixedDepositAccountsRequest fixedDepositAccountRequest(final Long clientId, final Long productId,
            final LocalDate submittedOnDate) {
        return new PostFixedDepositAccountsRequest().clientId(clientId).productId(productId).locale(Utils.LOCALE)
                .dateFormat(Utils.DATE_FORMAT).submittedOnDate(format(submittedOnDate)).depositAmount(100000F).depositPeriod(14)
                .depositPeriodFrequencyId(2L);
    }

    private PostRecurringDepositAccountsRequest recurringDepositAccountRequest(final Long clientId, final Long productId,
            final LocalDate submittedOnDate) {
        return new PostRecurringDepositAccountsRequest().clientId(clientId).productId(productId).locale(Utils.LOCALE)
                .dateFormat(Utils.DATE_FORMAT).submittedOnDate(format(submittedOnDate)).depositAmount(2000F).depositPeriod(14)
                .depositPeriodFrequencyId(2).isCalendarInherited(false).recurringFrequency(1).recurringFrequencyType(2)
                .mandatoryRecommendedDepositAmount(2000L);
    }

    private Map<String, Object> datedCommandRequest(final String dateParameter, final LocalDate date) {
        return Map.of("locale", Utils.LOCALE, "dateFormat", Utils.DATE_FORMAT, dateParameter, format(date));
    }

    private Map<String, Object> reinvestmentClosureRequest(final LocalDate closedOnDate) {
        return Map.of("locale", Utils.LOCALE, "dateFormat", Utils.DATE_FORMAT, "closedOnDate", format(closedOnDate), "onAccountClosureId",
                300);
    }

    private void assertApplicationDateError(final CallFailedRuntimeException error, final String expectedErrorCode)
            throws JsonProcessingException {
        assertThat(error.getStatus()).isEqualTo(403);
        assertThat(error.getCause()).isInstanceOf(FeignException.class);

        final FeignException cause = (FeignException) error.getCause();
        final JsonNode errors = ObjectMapperFactory.getShared().readTree(cause.responseBodyAsString()).path("errors");
        assertThat(errors.get(0).path("userMessageGlobalisationCode").asText()).isEqualTo(expectedErrorCode);
    }

    private String format(final LocalDate date) {
        return Utils.dateFormatter.format(date);
    }

    private LocalDate businessDate() {
        return new FeignBusinessDateHelper(fineractClient()).getBusinessDate("BUSINESS_DATE").getDate();
    }
}
