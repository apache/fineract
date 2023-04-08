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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanApplicationScheduleMonthlyTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanApplicationScheduleMonthlyTest.class);
    public static final Integer TOTAL_REPAYMENTS = 14;
    public static final String NUMBER_OF_REPAYMENTS = String.valueOf(TOTAL_REPAYMENTS);
    public static final String DISBURSEMENT_DATE = "30 December 2022";
    public static final String CLIENT_ACTIVATION_DATE = "13 October 2022";
    public static final String DUE_DATE = "dueDate";
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

    @SuppressWarnings({ "unchecked" })
    @Test
    public void validateSeedDate31() {
        final Integer clientId = createClient(CLIENT_ACTIVATION_DATE);

        String firstRepaymentDate = "31 January 2023";
        Integer loanProductId = createLoanProductEntity();

        Integer loanId = applyForLoanApplication(clientId, loanProductId, firstRepaymentDate);

        final ArrayList<HashMap> repaymentPeriods = (ArrayList<HashMap>) this.loanTransactionHelper
                .getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanId);

        assertEquals(new ArrayList<>(Arrays.asList(2023, 1, 31)), repaymentPeriods.get(1).get(DUE_DATE),
                "Checking for Due Date for 1st Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 2, 28)), repaymentPeriods.get(2).get(DUE_DATE),
                "Checking for Due Date for 2nd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 3, 31)), repaymentPeriods.get(3).get(DUE_DATE),
                "Checking for Due Date for 3rd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 4, 30)), repaymentPeriods.get(4).get(DUE_DATE),
                "Checking for Due Date for 4th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 5, 31)), repaymentPeriods.get(5).get(DUE_DATE),
                "Checking for Due Date for 5th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 6, 30)), repaymentPeriods.get(6).get(DUE_DATE),
                "Checking for Due Date for 6th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 7, 31)), repaymentPeriods.get(7).get(DUE_DATE),
                "Checking for Due Date for 7th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 8, 31)), repaymentPeriods.get(8).get(DUE_DATE),
                "Checking for Due Date for 8th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 9, 30)), repaymentPeriods.get(9).get(DUE_DATE),
                "Checking for Due Date for 9th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 10, 31)), repaymentPeriods.get(10).get(DUE_DATE),
                "Checking for Due Date for 10th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 11, 30)), repaymentPeriods.get(11).get(DUE_DATE),
                "Checking for Due Date for 11th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 12, 31)), repaymentPeriods.get(12).get(DUE_DATE),
                "Checking for Due Date for 12th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2024, 1, 31)), repaymentPeriods.get(13).get(DUE_DATE),
                "Checking for Due Date for 13th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2024, 2, 29)), repaymentPeriods.get(14).get(DUE_DATE),
                "Checking for Due Date for 14th Month");
    }

    private Integer createClient(String activationDate) {
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, activationDate);
        return clientId;
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void validateSeedDate30() {
        final Integer clientId = createClient(CLIENT_ACTIVATION_DATE);

        String firstRepaymentDate = "30 January 2023";
        Integer loanProductId = createLoanProductEntity();

        Integer loanId = applyForLoanApplication(clientId, loanProductId, firstRepaymentDate);

        final ArrayList<HashMap> repaymentPeriods = (ArrayList<HashMap>) this.loanTransactionHelper
                .getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanId);

        assertEquals(new ArrayList<>(Arrays.asList(2023, 1, 30)), repaymentPeriods.get(1).get(DUE_DATE),
                "Checking for Due Date for 1st Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 2, 28)), repaymentPeriods.get(2).get(DUE_DATE),
                "Checking for Due Date for 2nd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 3, 30)), repaymentPeriods.get(3).get(DUE_DATE),
                "Checking for Due Date for 3rd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 4, 30)), repaymentPeriods.get(4).get(DUE_DATE),
                "Checking for Due Date for 4th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 5, 30)), repaymentPeriods.get(5).get(DUE_DATE),
                "Checking for Due Date for 5th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 6, 30)), repaymentPeriods.get(6).get(DUE_DATE),
                "Checking for Due Date for 6th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 7, 30)), repaymentPeriods.get(7).get(DUE_DATE),
                "Checking for Due Date for 7th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 8, 30)), repaymentPeriods.get(8).get(DUE_DATE),
                "Checking for Due Date for 8th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 9, 30)), repaymentPeriods.get(9).get(DUE_DATE),
                "Checking for Due Date for 9th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 10, 30)), repaymentPeriods.get(10).get(DUE_DATE),
                "Checking for Due Date for 10th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 11, 30)), repaymentPeriods.get(11).get(DUE_DATE),
                "Checking for Due Date for 11th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 12, 30)), repaymentPeriods.get(12).get(DUE_DATE),
                "Checking for Due Date for 12th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2024, 1, 30)), repaymentPeriods.get(13).get(DUE_DATE),
                "Checking for Due Date for 13th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2024, 2, 29)), repaymentPeriods.get(14).get(DUE_DATE),
                "Checking for Due Date for 14th Month");
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void validateSeedDate28() {
        final Integer clientId = createClient(CLIENT_ACTIVATION_DATE);

        String firstRepaymentDate = "28 January 2023";
        Integer loanProductId = createLoanProductEntity();

        Integer loanId = applyForLoanApplication(clientId, loanProductId, firstRepaymentDate);

        final ArrayList<HashMap> repaymentPeriods = (ArrayList<HashMap>) this.loanTransactionHelper
                .getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanId);

        assertEquals(new ArrayList<>(Arrays.asList(2023, 1, 28)), repaymentPeriods.get(1).get(DUE_DATE),
                "Checking for Due Date for 1st Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 2, 28)), repaymentPeriods.get(2).get(DUE_DATE),
                "Checking for Due Date for 2nd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 3, 28)), repaymentPeriods.get(3).get(DUE_DATE),
                "Checking for Due Date for 3rd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 4, 28)), repaymentPeriods.get(4).get(DUE_DATE),
                "Checking for Due Date for 4th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 5, 28)), repaymentPeriods.get(5).get(DUE_DATE),
                "Checking for Due Date for 5th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 6, 28)), repaymentPeriods.get(6).get(DUE_DATE),
                "Checking for Due Date for 6th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 7, 28)), repaymentPeriods.get(7).get(DUE_DATE),
                "Checking for Due Date for 7th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 8, 28)), repaymentPeriods.get(8).get(DUE_DATE),
                "Checking for Due Date for 8th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 9, 28)), repaymentPeriods.get(9).get(DUE_DATE),
                "Checking for Due Date for 9th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 10, 28)), repaymentPeriods.get(10).get(DUE_DATE),
                "Checking for Due Date for 10th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 11, 28)), repaymentPeriods.get(11).get(DUE_DATE),
                "Checking for Due Date for 11th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 12, 28)), repaymentPeriods.get(12).get(DUE_DATE),
                "Checking for Due Date for 12th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2024, 1, 28)), repaymentPeriods.get(13).get(DUE_DATE),
                "Checking for Due Date for 13th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2024, 2, 28)), repaymentPeriods.get(14).get(DUE_DATE),
                "Checking for Due Date for 14th Month");
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void validateSeedDate25() {
        final Integer clientId = createClient(CLIENT_ACTIVATION_DATE);

        String firstRepaymentDate = "25 January 2023";
        Integer loanProductId = createLoanProductEntity();

        Integer loanId = applyForLoanApplication(clientId, loanProductId, firstRepaymentDate);

        final ArrayList<HashMap> repaymentPeriods = (ArrayList<HashMap>) this.loanTransactionHelper
                .getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanId);

        assertEquals(new ArrayList<>(Arrays.asList(2023, 1, 25)), repaymentPeriods.get(1).get(DUE_DATE),
                "Checking for Due Date for 1st Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 2, 25)), repaymentPeriods.get(2).get(DUE_DATE),
                "Checking for Due Date for 2nd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 3, 25)), repaymentPeriods.get(3).get(DUE_DATE),
                "Checking for Due Date for 3rd Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 4, 25)), repaymentPeriods.get(4).get(DUE_DATE),
                "Checking for Due Date for 4th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 5, 25)), repaymentPeriods.get(5).get(DUE_DATE),
                "Checking for Due Date for 5th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 6, 25)), repaymentPeriods.get(6).get(DUE_DATE),
                "Checking for Due Date for 6th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 7, 25)), repaymentPeriods.get(7).get(DUE_DATE),
                "Checking for Due Date for 7th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 8, 25)), repaymentPeriods.get(8).get(DUE_DATE),
                "Checking for Due Date for 8th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 9, 25)), repaymentPeriods.get(9).get(DUE_DATE),
                "Checking for Due Date for 9th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 10, 25)), repaymentPeriods.get(10).get(DUE_DATE),
                "Checking for Due Date for 10th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 11, 25)), repaymentPeriods.get(11).get(DUE_DATE),
                "Checking for Due Date for 11th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2023, 12, 25)), repaymentPeriods.get(12).get(DUE_DATE),
                "Checking for Due Date for 12th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2024, 1, 25)), repaymentPeriods.get(13).get(DUE_DATE),
                "Checking for Due Date for 13th Month");

        assertEquals(new ArrayList<>(Arrays.asList(2024, 2, 25)), repaymentPeriods.get(14).get(DUE_DATE),
                "Checking for Due Date for 14th Month");
    }

    /**
     * create a new loan product
     **/
    private Integer createLoanProductEntity() {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("10000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments(NUMBER_OF_REPAYMENTS).withRepaymentTypeAsMonth().withInterestRateFrequencyTypeAsMonths()
                .build(null);

        Integer loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanProductId;
    }

    /**
     * Apply for a Loan
     */
    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String firstRepaymentDate) {
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal("10000").withLoanTermFrequency(NUMBER_OF_REPAYMENTS)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(NUMBER_OF_REPAYMENTS).withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate(DISBURSEMENT_DATE)
                .withSubmittedOnDate(DISBURSEMENT_DATE).withFirstRepaymentDate(firstRepaymentDate)
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }
}
