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
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.test.data.job.DefaultJob;
import org.apache.fineract.test.data.job.JobResolver;
import org.apache.fineract.test.stepdef.AbstractStepDef;

@RequiredArgsConstructor
public class OrderingParametersStepDef extends AbstractStepDef {

    private final FineractFeignClient fineractClient;
    private final JobResolver jobResolver;

    @Then("Listing {string} ordered by {string} with sort order {string} is accepted")
    public void listingIsAccepted(final String list, final String orderBy, final String sortOrder) {
        ok(() -> list(list, blankToNull(orderBy), blankToNull(sortOrder)));
    }

    @Then("Listing {string} ordered by {string} with sort order {string} is rejected")
    public void listingIsRejected(final String list, final String orderBy, final String sortOrder) {
        final CallFailedRuntimeException exception = fail(() -> list(list, blankToNull(orderBy), blankToNull(sortOrder)));
        assertThat(exception.getStatus()).isBetween(400, 499);
    }

    private Object list(final String list, final String orderBy, final String sortOrder) {
        switch (list) {
            case "journal entries": {
                final Map<String, Object> params = new HashMap<>();
                if (orderBy != null) {
                    params.put("orderBy", orderBy);
                }
                if (sortOrder != null) {
                    params.put("sortOrder", sortOrder);
                }
                return fineractClient.journalEntries().retrieveAllJournalEntries(params);
            }
            case "scheduler job run history": {
                final Long jobId = jobResolver.resolve(DefaultJob.ADD_ACCRUAL_TRANSACTIONS);
                return fineractClient.schedulerJob().retrieveHistory(jobId, 0, 10, orderBy, sortOrder);
            }
            case "loans":
                return fineractClient.loans().retrieveAllLoans(null, 0, 10, orderBy, sortOrder, null, null, null, null);
            default:
                throw new IllegalArgumentException("Unknown list: " + list);
        }
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
