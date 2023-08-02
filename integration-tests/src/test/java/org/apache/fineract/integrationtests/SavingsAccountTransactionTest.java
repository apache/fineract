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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "rawtypes" })
public class SavingsAccountTransactionTest {

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy";
    final String startDateString = "03 June 2023";
    final String depositDateString = "05 June 2023";
    final String withdrawDateString = "10 June 2023";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
    }

    @Test
    public void verifySavingsTransactionSubmittedOnDateAndTransactionDate() throws JsonProcessingException {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        try {
            enableBusinessDate(requestSpec, responseSpec, true);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, today);

            LocalDate depositDate = Utils.getDateAsLocalDate(depositDateString);
            LocalDate withdrawDate = Utils.getDateAsLocalDate(withdrawDateString);

            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDateString);
            Assertions.assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDateString);
            Assertions.assertNotNull(savingsId);

            performSavingsTransaction(savingsId, "100", depositDate, true);
            performSavingsTransaction(savingsId, "50", withdrawDate, false);
        } finally {
            enableBusinessDate(requestSpec, responseSpec, false);
        }

    }

    private void enableBusinessDate(RequestSpecification requestSpec, ResponseSpecification responseSpec, boolean enable) {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, enable);
    }

    private void performSavingsTransaction(Integer savingsId, String amount, LocalDate transactionDate, boolean isDeposit) {
        String transactionType = isDeposit ? "Deposit" : "Withdrawal";
        Integer transactionId = isDeposit
                ? (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, amount, depositDateString,
                        CommonConstants.RESPONSE_RESOURCE_ID)
                : (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, amount, withdrawDateString,
                        CommonConstants.RESPONSE_RESOURCE_ID);

        Assertions.assertNotNull(transactionId);

        HashMap transaction = savingsAccountHelper.getSavingsTransaction(savingsId, transactionId);
        Assertions.assertNotNull(transaction);

        assertEquals(transactionId, (Integer) transaction.get("id"), "Check Savings " + transactionType + " Transaction");
        LocalDate transactionDateFromResponse = extractLocalDate(transaction, "date");
        assertTrue(transactionDate.compareTo(transactionDateFromResponse) == 0,
                "Transaction Date check for Savings " + transactionType + " Transaction");
        LocalDate submittedOnDate = extractLocalDate(transaction, "submittedOnDate");
        assertTrue(LocalDate.now(ZoneId.systemDefault()).compareTo(submittedOnDate) == 0,
                "Submitted On Date check for Savings " + transactionType + " Transaction");
    }

    private LocalDate extractLocalDate(HashMap transactionMap, String fieldName) {
        List<Integer> dateStringList = (List<Integer>) transactionMap.get(fieldName);
        LocalDate extractedDate = dateStringList.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> LocalDate.of(list.get(0), list.get(1), list.get(2))));
        return extractedDate;
    }

    private Integer createSavingsAccountDailyPosting(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        GlobalConfigurationHelper.resetAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.verifyAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
    }

}
