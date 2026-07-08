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
package org.apache.fineract.integrationtests.investor.externalassetowner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.UUID;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalAssetOwnerHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({ LoanTestLifecycleExtension.class })
public class ExternalAssetOwnerTransferUndisbursedLoanTest extends FeignLoanTestBase {

    private final FeignExternalAssetOwnerHelper externalAssetOwnerHelper = new FeignExternalAssetOwnerHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    private Long loan1Id;
    private Long loan2Id;

    @BeforeAll
    public static void setup() {
        new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS",
                "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER");
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER,
                new PutGlobalConfigurationsRequest().stringValue("APPROVED,ACTIVE,TRANSFER_IN_PROGRESS,TRANSFER_ON_HOLD"));
    }

    @AfterAll
    public static void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER,
                new PutGlobalConfigurationsRequest().stringValue("ACTIVE,TRANSFER_IN_PROGRESS,TRANSFER_ON_HOLD"));
    }

    @Test
    public void testExternalAssetOwnerTransferForUndisbursedLoan() {
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);

        Account transferAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("TRANSFER_", 5));
        externalAssetOwnerHelper.setProperFinancialActivity(transferAccount);

        try {
            runAt("01 January 2024", () -> {
                Long clientId = createClient();

                PostLoanProductsRequest loanProductRequest = createOnePeriod30DaysPeriodicAccrualProduct(12.0)
                        .name(Utils.uniqueRandomStringGenerator("UNDISBURSED_TEST_", 4))
                        .shortName(Utils.uniqueRandomStringGenerator("UT", 2));

                Long loanProductId = createLoanProduct(loanProductRequest);

                Long loanId = applyForLoan(applyLoanRequest(clientId, loanProductId, "01 January 2024", 10000.0, 4));

                approveLoan(loanId, approveLoanRequest(10000.0, "01 January 2024"));

                GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                assertNotNull(loanDetails);
                assertEquals("loanStatusType.approved", loanDetails.getStatus().getCode());

                String transferExternalId = UUID.randomUUID().toString();
                String ownerExternalId = UUID.randomUUID().toString();

                PostInitiateTransferResponse transferResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanId, "sale",
                        new ExternalAssetOwnerRequest().settlementDate("01 January 2024").dateFormat("dd MMMM yyyy").locale("en")
                                .transferExternalId(transferExternalId).ownerExternalId(ownerExternalId).purchasePriceRatio("1.0"));

                assertNotNull(transferResponse);
                assertEquals(transferExternalId, transferResponse.getResourceExternalId());

                GetLoansLoanIdResponse loanAfterTransfer = getLoanDetails(loanId);
                assertNotNull(loanAfterTransfer, "Loan details should not be null");

                if (loanAfterTransfer.getSummary() == null) {
                    assertEquals("loanStatusType.approved", loanAfterTransfer.getStatus().getCode(),
                            "Loan should remain in approved status");

                    assertEquals(0, BigDecimal.valueOf(10000.0).compareTo(loanAfterTransfer.getApprovedPrincipal()),
                            "Approved principal should be 10000");

                    return;
                }

                fail("Unexpected: Loan summary should be null for undisbursed loans");
            });
        } finally {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        }
    }

    @Test
    public void testExternalAssetOwnerTransferForBackdatedUndisbursedLoan() {
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);

        Account transferAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("TRANSFER_", 5));
        externalAssetOwnerHelper.setProperFinancialActivity(transferAccount);

        try {
            runAt("01 March 2024", () -> {
                Long clientId = createClient();

                PostLoanProductsRequest loanProductRequest = createOnePeriod30DaysPeriodicAccrualProduct(12.0)
                        .name(Utils.uniqueRandomStringGenerator("BACKDATED_UNDISBURSED_", 4))
                        .shortName(Utils.uniqueRandomStringGenerator("BU", 2));

                Long loanProductId = createLoanProduct(loanProductRequest);

                Long loanId = applyForLoan(applyLoanRequest(clientId, loanProductId, "01 December 2023", 15000.0, 4));

                approveLoan(loanId, approveLoanRequest(15000.0, "01 December 2023"));

                String transferExternalId = UUID.randomUUID().toString();
                String ownerExternalId = UUID.randomUUID().toString();

                PostInitiateTransferResponse transferResponse = externalAssetOwnerHelper.initiateTransferByLoanId(loanId, "sale",
                        new ExternalAssetOwnerRequest().settlementDate("01 March 2024").dateFormat("dd MMMM yyyy").locale("en")
                                .transferExternalId(transferExternalId).ownerExternalId(ownerExternalId).purchasePriceRatio("1.0"));

                assertNotNull(transferResponse);
                assertEquals(transferExternalId, transferResponse.getResourceExternalId());

                GetLoansLoanIdResponse loanAfterTransfer = getLoanDetails(loanId);
                assertNotNull(loanAfterTransfer, "Loan details should not be null");

                if (loanAfterTransfer.getSummary() == null) {
                    assertEquals("loanStatusType.approved", loanAfterTransfer.getStatus().getCode(),
                            "Loan should remain in approved status");

                    assertEquals(0, BigDecimal.valueOf(15000.0).compareTo(loanAfterTransfer.getApprovedPrincipal()),
                            "Approved principal should be 15000");

                    return;
                }

                fail("Unexpected: Loan summary should be null for undisbursed loans");
            });
        } finally {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        }
    }

    @Test
    public void testExternalAssetOwnerTransferComparison_DisbursedVsUndisbursed() {
        globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, true);

        Account transferAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("TRANSFER_", 5));
        externalAssetOwnerHelper.setProperFinancialActivity(transferAccount);

        try {
            runAt("01 January 2024", () -> {
                Long client1Id = createClient();
                Long client2Id = createClient();

                PostLoanProductsRequest loanProductRequest = createOnePeriod30DaysPeriodicAccrualProduct(12.0)
                        .name(Utils.uniqueRandomStringGenerator("COMPARISON_TEST_", 4))
                        .shortName(Utils.uniqueRandomStringGenerator("CT", 2));

                Long loanProductId = createLoanProduct(loanProductRequest);

                loan1Id = applyForLoan(applyLoanRequest(client1Id, loanProductId, "01 January 2024", 20000.0, 4));
                loan2Id = applyForLoan(applyLoanRequest(client2Id, loanProductId, "01 January 2024", 20000.0, 4));

                approveLoan(loan1Id, approveLoanRequest(20000.0, "01 January 2024"));
                approveLoan(loan2Id, approveLoanRequest(20000.0, "01 January 2024"));

                disburseLoan(loan1Id, BigDecimal.valueOf(20000.0), "01 January 2024");
            });

            runAt("31 January 2024", () -> {
                executeInlineCOB(loan1Id);
                executeInlineCOB(loan2Id);

                String transfer1ExternalId = UUID.randomUUID().toString();
                String transfer2ExternalId = UUID.randomUUID().toString();
                String ownerExternalId = UUID.randomUUID().toString();

                PostInitiateTransferResponse transfer1Response = externalAssetOwnerHelper.initiateTransferByLoanId(loan1Id, "sale",
                        new ExternalAssetOwnerRequest().settlementDate("31 January 2024").dateFormat("dd MMMM yyyy").locale("en")
                                .transferExternalId(transfer1ExternalId).ownerExternalId(ownerExternalId).purchasePriceRatio("1.0"));

                PostInitiateTransferResponse transfer2Response = externalAssetOwnerHelper.initiateTransferByLoanId(loan2Id, "sale",
                        new ExternalAssetOwnerRequest().settlementDate("31 January 2024").dateFormat("dd MMMM yyyy").locale("en")
                                .transferExternalId(transfer2ExternalId).ownerExternalId(ownerExternalId).purchasePriceRatio("1.0"));

                assertNotNull(transfer1Response);
                assertNotNull(transfer2Response);

                GetLoansLoanIdResponse disbursedLoan = getLoanDetails(loan1Id);
                GetLoansLoanIdResponse undisbursedLoan = getLoanDetails(loan2Id);

                assertNotNull(disbursedLoan, "Disbursed loan details should not be null");
                assertNotNull(undisbursedLoan, "Undisbursed loan details should not be null");

                assertNotNull(disbursedLoan.getSummary(), "Disbursed loan summary should not be null");

                if (undisbursedLoan.getSummary() == null) {
                    assertEquals("loanStatusType.active", disbursedLoan.getStatus().getCode(), "Disbursed loan should be active");
                    assertEquals("loanStatusType.approved", undisbursedLoan.getStatus().getCode(),
                            "Undisbursed loan should remain approved");

                    BigDecimal disbursedInterest = disbursedLoan.getSummary().getInterestOutstanding();
                    assertNotNull(disbursedInterest, "Disbursed loan interest outstanding should not be null");

                    return;
                }

                fail("Unexpected: Undisbursed loan should not have summary data");
            });
        } finally {
            globalConfigurationHelper.manageConfigurations(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID, false);
        }
    }
}
