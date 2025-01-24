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

import io.cucumber.java.en.And;
import java.io.IOException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.InterestPauseRequestDto;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.LoanInterestPauseApi;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class LoanInterestPauseStepDef extends AbstractStepDef {

    @Autowired
    private LoanInterestPauseApi loanInterestPauseApi;

    @And("Create interest pause period with start date {string} and end date {string}")
    public void createInterestPause(final String startDate, final String endDate) throws IOException {
        final Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assert loanResponse.body() != null;
        final long loanId = loanResponse.body().getLoanId();

        final InterestPauseRequestDto request = new InterestPauseRequestDto()//
                .startDate(startDate)//
                .endDate(endDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");//

        final Response<CommandProcessingResult> createResponse = loanInterestPauseApi.createInterestPause(loanId, request).execute();
        ErrorHelper.checkSuccessfulApiCall(createResponse);
    }
}
