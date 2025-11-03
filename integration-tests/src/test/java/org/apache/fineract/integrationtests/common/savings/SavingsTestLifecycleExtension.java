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
package org.apache.fineract.integrationtests.common.savings;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class SavingsTestLifecycleExtension implements AfterAllCallback {

    private SavingsAccountHelper savingsAccountHelper;
    private SchedulerJobHelper schedulerJobHelper;
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern(DATE_FORMAT).toFormatter();

    @Override
    public void afterAll(ExtensionContext context) {
        BusinessDateHelper.runAt(DateTimeFormatter.ofPattern(DATE_FORMAT).format(Utils.getLocalDateOfTenant()), () -> {
            RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
            requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
            requestSpec.header("Fineract-Platform-TenantId", "default");
            ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
            this.savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
            this.schedulerJobHelper = new SchedulerJobHelper(requestSpec);

            // Close open savings accounts
            List<Long> savingsIds = SavingsAccountHelper.getSavingsIdsByStatusId(300);
            savingsIds.forEach(savingsId -> {
                try {
                    this.savingsAccountHelper.postInterestForSavings(savingsId.intValue());
                    SavingsAccountData savingsAccountData = Calls
                            .ok(FineractClientHelper.getFineractClient().savingsAccounts.retrieveOne25(savingsId, false, null, "all"));
                    BigDecimal accountBalance = MathUtil.subtract(savingsAccountData.getSummary().getAvailableBalance(),
                            savingsAccountData.getMinRequiredBalance(), MathContext.DECIMAL64);
                    if (accountBalance.compareTo(BigDecimal.ZERO) > 0) {
                        savingsAccountHelper.closeSavingsAccount(savingsId,
                                new PostSavingsAccountsAccountIdRequest().locale("en").dateFormat(DATE_FORMAT)
                                        .closedOnDate(dateFormatter.format(Utils.getLocalDateOfTenant())).withdrawBalance(true));
                    } else if (accountBalance.compareTo(BigDecimal.ZERO) < 0) {
                        savingsAccountHelper.depositIntoSavingsAccount(savingsId,
                                new PostSavingsAccountTransactionsRequest().locale("en").dateFormat(DATE_FORMAT)
                                        .transactionDate(dateFormatter.format(Utils.getLocalDateOfTenant()))
                                        .transactionAmount(accountBalance.abs()).paymentTypeId(1));
                        savingsAccountHelper.closeSavingsAccount(savingsId, new PostSavingsAccountsAccountIdRequest().locale("en")
                                .dateFormat(DATE_FORMAT).closedOnDate(dateFormatter.format(Utils.getLocalDateOfTenant())));
                    }
                } catch (Exception e) {
                    log.warn("Unable to close savings account: {}, Reason: {}", savingsId, e.getMessage());
                }
            });
        });
    }
}
