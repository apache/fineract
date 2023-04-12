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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@SuppressWarnings("rawtypes")
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanWithdrawnByApplicantIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanWithdrawnByApplicant() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        this.loanTransactionHelper.withdrawLoanApplicationByClient("03 April 2012", loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsNotActive(loanStatusHashMap);

    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal("5000").withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withCollaterals(collaterals).withSubmittedOnDate("02 April 2012")
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }
}
