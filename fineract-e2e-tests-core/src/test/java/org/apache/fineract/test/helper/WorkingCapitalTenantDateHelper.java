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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.test.support.TestContext;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.stereotype.Component;

/**
 * Resolves the date a Working Capital loan action is stamped with while {@code enable-business-date} is switched off.
 * The business date API cannot be used for that: it returns the persisted business-date row even when the configuration
 * is disabled. The read-only charge-off template exposes a date resolved through
 * {@code DateUtils.getBusinessLocalDate}, the same resolver the stamps use, and therefore returns the tenant date while
 * the configuration is disabled.
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalTenantDateHelper {

    private static final String CHARGE_OFF_TEMPLATE = "chargeOff";

    private final FineractFeignClient fineractClient;
    private final BusinessDateHelper businessDateHelper;

    /**
     * Captures the server's effective date immediately before an action that should be stamped with the tenant date.
     */
    public void captureCurrentTenantDateBeforeAction(final Long loanId) {
        TestContext.INSTANCE.set(TestContextKey.WORKING_CAPITAL_CURRENT_TENANT_DATE_BEFORE_ACTION, getEffectiveDateFromServer(loanId));
    }

    /**
     * Accepts the server date captured immediately before or after the action. Usually they are identical; accepting
     * both makes the assertion deterministic when the action crosses midnight in the tenant timezone.
     *
     * <p>
     * The probe and the stamp share one resolver, so on their own they cannot tell a correct fallback from a regression
     * that returns the stored business-date row for both. The stored row is therefore required to differ from the
     * tenant date: the scenarios park it on a date in the past for exactly this reason.
     */
    public void assertStampedOnCurrentTenantDate(final LocalDate actual, final Long loanId, final String description) {
        final LocalDate tenantDateBeforeAction = TestContext.INSTANCE.get(TestContextKey.WORKING_CAPITAL_CURRENT_TENANT_DATE_BEFORE_ACTION);
        final LocalDate tenantDateAfterAction = getEffectiveDateFromServer(loanId);
        final LocalDate storedBusinessDate = businessDateHelper.getBusinessLocalDate();

        assertThat(tenantDateBeforeAction).as("Tenant date must be captured before the Working Capital action").isNotNull();
        assertThat(storedBusinessDate)
                .as("scenario precondition: the stored business date must differ from the tenant date, otherwise the fallback "
                        + "cannot be told apart from the stored row")
                .isNotIn(tenantDateBeforeAction, tenantDateAfterAction);
        assertThat(actual).as(description).isNotNull().isIn(tenantDateBeforeAction, tenantDateAfterAction);
    }

    private LocalDate getEffectiveDateFromServer(final Long loanId) {
        return ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanActionTemplate(loanId, CHARGE_OFF_TEMPLATE))
                .getChargeOffDate();
    }
}
