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
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;




public class SavingsInterestPostingPeriodsTest {
    private static final Logger LOG = LoggerFactory.getLogger(SavingsInterestPostingIntegrationTest.class);
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private SavingsPostingInterestPeriodType savingsPostingInterestPeriodType;
    private SavingsHelper savingsHelper;

    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsHelper = new SavingsHelper(accountTransfersReadPlatformService);
    }

    @Test
    public void testDailyPostingPeriodRetrieval() {
        final Integer financialYearBeginningMonth = 1;
        final List<LocalDate> localDateList = new ArrayList<>();
final LocalDate startingDate = LocalDate.of(2021, 11, 1);
final LocalDate endingDate = LocalDate.of(2022, 1, 1);
final SavingsPostingInterestPeriodType savingsPostingInterestPeriodType1 = SavingsPostingInterestPeriodType.DAILY;
        System.out.println(savingsHelper.determineInterestPostingPeriods(startingDate, endingDate, savingsPostingInterestPeriodType1,
                financialYearBeginningMonth, localDateList));
    }

    @Test
    public void testMonthlyPostingPeriodRetrievalStartDateFirstOfMonth() {
        final Integer financialYearBeginningMonth = 1;
        final List<LocalDate> localDateList = new ArrayList<>();
        final LocalDate startingDate = LocalDate.of(2021, 11, 1);
        final LocalDate endingDate = LocalDate.of(2022, 1, 1);
        final SavingsPostingInterestPeriodType savingsPostingInterestPeriodType1 = SavingsPostingInterestPeriodType.MONTHLY;
        System.out.println(savingsHelper.determineInterestPostingPeriods(startingDate, endingDate, savingsPostingInterestPeriodType1,
                financialYearBeginningMonth, localDateList));
    }

    @Test
    public void testMonthlyPostingPeriodRetrievalStartDateMiddleOfMonth() {
        final Integer financialYearBeginningMonth = 1;
        final List<LocalDate> localDateList = new ArrayList<>();
        final LocalDate startingDate = LocalDate.of(2021, 11, 14);
        final LocalDate endingDate = LocalDate.of(2022, 5, 14);
        final SavingsPostingInterestPeriodType savingsPostingInterestPeriodType1 = SavingsPostingInterestPeriodType.MONTHLY;
        System.out.println(savingsHelper.determineInterestPostingPeriods(startingDate, endingDate, savingsPostingInterestPeriodType1,
                financialYearBeginningMonth, localDateList));
    }

    @Test
    public void testActivationDateMonthlyPostingPeriodRetrievalStartDateMiddleOfMonth() {
        final Integer financialYearBeginningMonth = 1;
        final List<LocalDate> localDateList = new ArrayList<>();
        final LocalDate startingDate = LocalDate.of(2021, 11, 14);
        final LocalDate endingDate = LocalDate.of(2022, 5, 14);
        final SavingsPostingInterestPeriodType savingsPostingInterestPeriodType1 = SavingsPostingInterestPeriodType.ACTIVATIONDATE;
        System.out.println(savingsHelper.determineInterestPostingPeriods(startingDate, endingDate, savingsPostingInterestPeriodType1,
                financialYearBeginningMonth, localDateList));
    }

}
