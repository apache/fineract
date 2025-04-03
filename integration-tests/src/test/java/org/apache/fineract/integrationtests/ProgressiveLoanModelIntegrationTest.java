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

import org.apache.fineract.client.models.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Test;

public class ProgressiveLoanModelIntegrationTest extends BaseLoanIntegrationTest {

    private final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    private final Long loanProductId = loanProductHelper.createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true))
            .getResourceId();

    @Test
    public void testModelReturnsNullAndThenSaveThenNotNull() {
        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 1000.0, 96.32, 6, null);
            loanTransactionHelper.disburseLoan(loanId, "1 January 2024", 1000.0);

            // Model not saved, fetching it. It should return null
            ProgressiveLoanInterestScheduleModel progressiveLoanInterestScheduleModelResponse1 = ok(
                    fineractClient().progressiveLoanApi.fetchModel(loanId));

            assertThat(progressiveLoanInterestScheduleModelResponse1).isNull();

            // Forcing Model recalculation and save to database. It should return the actual model.
            ProgressiveLoanInterestScheduleModel ok = ok(fineractClient().progressiveLoanApi.updateModel(loanId));
            assertThat(ok).isNotNull();

            // Model saved in previous step. API should return the previous model.
            ProgressiveLoanInterestScheduleModel progressiveLoanInterestScheduleModelResponse2 = ok(
                    fineractClient().progressiveLoanApi.fetchModel(loanId));
            assertThat(progressiveLoanInterestScheduleModelResponse2).isNotNull();
        });
    }

}
