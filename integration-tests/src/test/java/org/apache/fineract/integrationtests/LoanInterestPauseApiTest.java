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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.InterestPauseRequestDto;
import org.apache.fineract.client.models.InterestPauseResponseDto;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class LoanInterestPauseApiTest extends FeignLoanTestBase {

    private static final Long NON_EXIST_LOAN_ID = 99999L;
    private static final String NON_EXIST_EXTERNAL_ID = "7c4fb86f-a778-4d02-b7a8-ec3ec98941fa";
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    private static final String TEXT_DATE_FORMAT = "d MMMM yyyy";
    private static final String LOAN_START_DATE = "01 January 2023";
    private static final String LOAN_PRINCIPAL_AMOUNT = "10000.00";
    private static final int NUMBER_OF_REPAYMENTS = 12;
    private static final double INTEREST_RATE_PER_PERIOD = 18.0;

    private Long clientId;
    private Long loanProductId;
    private Long loanId;
    private String externalId;
    private Long zeroInterestLoanId;

    @BeforeEach
    public void initialize() {
        clientId = createClient();
        externalId = UUID.randomUUID().toString();
        loanProductId = createLoanProduct(create4IProgressive().numberOfRepayments(NUMBER_OF_REPAYMENTS)
                .interestRatePerPeriod(INTEREST_RATE_PER_PERIOD).principal(Double.parseDouble(LOAN_PRINCIPAL_AMOUNT)));
        loanId = createLoan();

        Assertions.assertNotNull(loanProductId, "Loan Product ID should not be null after creation");
        Assertions.assertNotNull(loanId, "Loan ID should not be null after creation");
        Assertions.assertNotNull(externalId, "External Loan ID should not be null after creation");
    }

    @Test
    public void testCreateInterestPauseByLoanId_validRequest_shouldSucceed() {
        CommandProcessingResult response = loanHelper.createInterestPause(loanId, pause("2023-01-01", "2023-01-12"));

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
    }

    @Test
    public void testCreateInterestPauseByLoanId_endDateBeforeStartDate_shouldFail() {
        assertPauseFails(403, "interest.pause.end.date.before.start.date",
                () -> loanHelper.createInterestPause(loanId, pause("2024-12-05", "2023-01-12")));
    }

    @Test
    public void testCreateInterestPauseByLoanId_startDateBeforeLoanStart_shouldFail() {
        assertPauseFails(403, "interest.pause.start.date.before.loan.start.date",
                () -> loanHelper.createInterestPause(loanId, pause("2022-12-01", "2023-01-12")));
    }

    @Test
    public void testCreateInterestPauseByLoanId_endDateAfterLoanMaturity_shouldFail() {
        assertPauseFails(403, "interest.pause.end.date.after.loan.maturity.date",
                () -> loanHelper.createInterestPause(loanId, pause("2024-12-01", "2025-12-05")));
    }

    @Test
    public void testRetrieveInterestPausesByLoanId_noPauses_shouldReturnEmpty() {
        Assertions.assertTrue(loanHelper.retrieveInterestPauses(NON_EXIST_LOAN_ID).isEmpty());
    }

    @Test
    public void testRetrieveInterestPausesByLoanId_shouldReturnData() {
        loanHelper.createInterestPause(loanId, pause("2023-01-01", "2023-01-12"));

        assertContainsPause(loanHelper.retrieveInterestPauses(loanId), "2023-01-01", "2023-01-12");
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_validRequest_shouldSucceed() {
        CommandProcessingResult response = loanHelper.createInterestPauseByExternalId(externalId, pause("2023-01-01", "2023-01-12"));

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_endDateBeforeStartDate_shouldFail() {
        assertPauseFails(403, "interest.pause.end.date.before.start.date",
                () -> loanHelper.createInterestPauseByExternalId(externalId, pause("2023-01-01", "2022-01-12")));
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_startDateBeforeLoanStart_shouldFail() {
        assertPauseFails(403, "interest.pause.start.date.before.loan.start.date",
                () -> loanHelper.createInterestPauseByExternalId(externalId, pause("2022-12-01", "2024-12-05")));
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_endDateAfterLoanMaturity_shouldFail() {
        assertPauseFails(403, "interest.pause.end.date.after.loan.maturity.date",
                () -> loanHelper.createInterestPauseByExternalId(externalId, pause("2024-12-01", "2025-12-05")));
    }

    @Test
    public void testRetrieveInterestPausesByExternalLoanId_noPauses_shouldReturnEmpty() {
        Assertions.assertTrue(loanHelper.retrieveInterestPausesByExternalId(NON_EXIST_EXTERNAL_ID).isEmpty());
    }

    @Test
    public void testRetrieveInterestPausesByExternalLoanId_shouldReturnData() {
        loanHelper.createInterestPauseByExternalId(externalId, pause("2023-01-01", "2023-01-12"));

        assertContainsPause(loanHelper.retrieveInterestPausesByExternalId(externalId), "2023-01-01", "2023-01-12");
    }

    @Test
    public void testUpdateInterestPauseByLoanId_overlapping_shouldFail() {
        Long variationId = createPauseByLoanId("2023-01-01", "2023-01-03");
        loanHelper.createInterestPause(loanId, pause("2023-01-08", "2023-01-10"));

        assertPauseFails(403, "interest.pause.overlapping",
                () -> loanHelper.updateInterestPause(loanId, variationId, pause("2023-01-01", "2023-01-12")));
    }

    @Test
    public void testUpdateInterestPauseByLoanId_overlapping_shouldNotFail() {
        Long variationId = createPauseByLoanId("2023-01-01", "2023-01-03");
        loanHelper.createInterestPause(loanId, pause("2023-01-08", "2023-01-10"));

        CommandProcessingResult updateResponse = loanHelper.updateInterestPause(loanId, variationId, pause("2023-01-01", "2023-01-07"));

        Assertions.assertNotNull(updateResponse.getResourceId());
        Assertions.assertEquals(variationId, updateResponse.getResourceId());
    }

    @Test
    public void testUpdateInterestPauseByLoanId_overlapping_shouldFail2() {
        Long variationId = createPauseByLoanId("2023-01-01", "2023-01-06");
        loanHelper.createInterestPause(loanId, pause("2023-01-07", "2023-01-12"));

        assertPauseFails(403, "interest.pause.overlapping",
                () -> loanHelper.updateInterestPause(loanId, variationId, pause("2023-01-02", "2023-01-13")));
    }

    @Test
    public void testUpdateInterestPauseByLoanId_overlapping_shouldFail3() {
        Long variationId = createPauseByLoanId("2023-01-03", "2023-01-06");
        loanHelper.createInterestPause(loanId, pause("2023-01-07", "2023-01-12"));

        assertPauseFails(403, "interest.pause.overlapping",
                () -> loanHelper.updateInterestPause(loanId, variationId, pause("2023-01-01", "2023-01-11")));
    }

    @Test
    public void testUpdateInterestPauseByLoanId_validRequest_shouldSucceed() {
        Long variationId = createPauseByLoanId("2023-01-01", "2023-01-02");

        CommandProcessingResult updateResponse = loanHelper.updateInterestPause(loanId, variationId, pause("2023-01-03", "2023-01-04"));

        Assertions.assertNotNull(updateResponse.getResourceId());
        Assertions.assertEquals(variationId, updateResponse.getResourceId());
    }

    @Test
    public void testUpdateInterestPauseByLoanId_endDateBeforeStartDate_shouldFail() {
        Long variationId = createPauseByLoanId("2023-01-01", "2023-01-12");

        assertPauseFails(403, "interest.pause.end.date.before.start.date",
                () -> loanHelper.updateInterestPause(loanId, variationId, pause("2023-03-01", "2023-01-12")));
    }

    @Test
    public void testUpdateInterestPauseByLoanId_startDateBeforeLoanStart_shouldFail() {
        Long variationId = createPauseByLoanId("2023-01-01", "2023-01-12");

        assertPauseFails(403, "interest.pause.start.date.before.loan.start.date",
                () -> loanHelper.updateInterestPause(loanId, variationId, pause("2022-12-01", "2023-01-12")));
    }

    @Test
    public void testDeleteInterestPauseByLoanId_validRequest_shouldSucceed() {
        Long variationId = createPauseByLoanId("2023-01-01", "2023-01-12");

        loanHelper.deleteInterestPause(loanId, variationId);

        assertDoesNotContainPause(loanHelper.retrieveInterestPauses(loanId), variationId);
    }

    @Test
    public void testDeleteInterestPauseByLoanId_nonExistentVariation_shouldFail() {
        assertPauseFails(403, "error.msg.variation.not.found", () -> loanHelper.deleteInterestPause(loanId, NON_EXIST_LOAN_ID));
    }

    @Test
    public void testDeleteInterestPauseByLoanId_invalidLoanId_shouldFail() {
        Long variationId = createPauseByLoanId("2023-01-01", "2023-01-12");

        assertPauseFails(404, "error.msg.loan.id.invalid", () -> loanHelper.deleteInterestPause(NON_EXIST_LOAN_ID, variationId));
    }

    @Test
    public void testUpdateInterestPauseByExternalId_validRequest_shouldSucceed() {
        Long variationId = createPauseByExternalId("2023-01-01", "2023-01-02");

        CommandProcessingResult updateResponse = loanHelper.updateInterestPauseByExternalId(externalId, variationId,
                pause("2023-01-03", "2023-01-04"));

        Assertions.assertNotNull(updateResponse.getResourceId());
        Assertions.assertEquals(variationId, updateResponse.getResourceId());
    }

    @Test
    public void testUpdateInterestPauseByExternalId_overlapping_shouldFail() {
        Long variationId = createPauseByExternalId("2023-01-01", "2023-01-06");
        loanHelper.createInterestPauseByExternalId(externalId, pause("2023-01-07", "2023-01-12"));

        assertPauseFails(403, "interest.pause.overlapping",
                () -> loanHelper.updateInterestPauseByExternalId(externalId, variationId, pause("2023-01-01", "2023-01-12")));
    }

    @Test
    public void testUpdateInterestPauseByExternalId_overlapping_shouldFail2() {
        Long variationId = createPauseByExternalId("2023-01-01", "2023-01-06");
        loanHelper.createInterestPauseByExternalId(externalId, pause("2023-01-07", "2023-01-12"));

        assertPauseFails(403, "interest.pause.overlapping",
                () -> loanHelper.updateInterestPauseByExternalId(externalId, variationId, pause("2023-01-02", "2023-01-13")));
    }

    @Test
    public void testUpdateInterestPauseByExternalId_overlapping_shouldFail3() {
        Long variationId = createPauseByExternalId("2023-01-03", "2023-01-06");
        loanHelper.createInterestPauseByExternalId(externalId, pause("2023-01-07", "2023-01-12"));

        assertPauseFails(403, "interest.pause.overlapping",
                () -> loanHelper.updateInterestPauseByExternalId(externalId, variationId, pause("2023-01-01", "2023-01-11")));
    }

    @Test
    public void testUpdateInterestPauseByExternalId_endDateBeforeStartDate_shouldFail() {
        Long variationId = createPauseByExternalId("2023-01-03", "2023-01-12");

        assertPauseFails(403, "interest.pause.end.date.before.start.date",
                () -> loanHelper.updateInterestPauseByExternalId(externalId, variationId, pause("2023-03-01", "2023-01-12")));
    }

    @Test
    public void testUpdateInterestPauseByExternalId_startDateBeforeLoanStart_shouldFail() {
        Long variationId = createPauseByExternalId("2023-01-03", "2023-01-12");

        assertPauseFails(403, "interest.pause.start.date.before.loan.start.date",
                () -> loanHelper.updateInterestPauseByExternalId(externalId, variationId, pause("2022-12-01", "2023-01-12")));
    }

    @Test
    public void testDeleteInterestPauseByExternalId_validRequest_shouldSucceed() {
        Long variationId = createPauseByExternalId("2023-01-03", "2023-01-12");

        loanHelper.deleteInterestPauseByExternalId(externalId, variationId);

        assertDoesNotContainPause(loanHelper.retrieveInterestPausesByExternalId(externalId), variationId);
    }

    @Test
    public void testDeleteInterestPauseByExternalId_nonExistentVariation_shouldFail() {
        assertPauseFails(403, "error.msg.variation.not.found",
                () -> loanHelper.deleteInterestPauseByExternalId(externalId, NON_EXIST_LOAN_ID));
    }

    @Test
    public void testDeleteInterestPauseByExternalId_invalidExternalId_shouldFail() {
        Long variationId = createPauseByExternalId("2023-01-03", "2023-01-12");

        assertPauseFails(404, "error.msg.loan.external.id.invalid",
                () -> loanHelper.deleteInterestPauseByExternalId(NON_EXIST_EXTERNAL_ID, variationId));
    }

    @Test
    public void testInterestPauseOnZeroInterestRate() {
        runAt("1 September 2019", () -> {
            Long loanProductId = createLoanProduct(create4IProgressive());
            zeroInterestLoanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 September 2019", 1200.0, 0.0, 4, null);
            disburseLoan(zeroInterestLoanId, BigDecimal.valueOf(1200.0), "1 September 2019");
        });
        runAt("1 October 2019", () -> {
            makeLoanRepayment(zeroInterestLoanId, "Repayment", "1 October 2019", 300.0);
        });
        runAt("1 November 2019", () -> {
            makeLoanRepayment(zeroInterestLoanId, "Repayment", "1 November 2019", 300.0);
        });
        runAt("1 December 2019", () -> {
            makeLoanRepayment(zeroInterestLoanId, "Repayment", "1 December 2019", 300.0);
            verifyTransactions(zeroInterestLoanId, //
                    transaction(1200.0, "Disbursement", "01 September 2019", 1200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0), //
                    transaction(300.0, "Repayment", "01 October 2019", 900.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(300.0, "Repayment", "01 November 2019", 600.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(300.0, "Repayment", "01 December 2019", 300.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );
            assertPauseFails(403, "", () -> loanHelper.createInterestPause(zeroInterestLoanId,
                    pause("1 October 2019", "15 December 2019", TEXT_DATE_FORMAT)));
            verifyTransactions(zeroInterestLoanId, //
                    transaction(1200.0, "Disbursement", "01 September 2019", 1200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0), //
                    transaction(300.0, "Repayment", "01 October 2019", 900.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(300.0, "Repayment", "01 November 2019", 600.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(300.0, "Repayment", "01 December 2019", 300.0, 300.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );
        });
    }

    @Test
    public void testInterestPauseOnZeroInterestRateRightAfterDisbursement() {
        runAt("1 September 2019", () -> {
            Long loanProductId = createLoanProduct(create4IProgressive());
            zeroInterestLoanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 September 2019", 1200.0, 0.0, 4, null);
            disburseLoan(zeroInterestLoanId, BigDecimal.valueOf(1200.0), "1 September 2019");
            verifyRepaymentSchedule(zeroInterestLoanId, //
                    installment(1200.0, null, "01 September 2019"), //
                    installment(300.0, 0.0, 300.0, false, "01 October 2019"), //
                    installment(300.0, 0.0, 300.0, false, "01 November 2019"), //
                    installment(300.0, 0.0, 300.0, false, "01 December 2019"), //
                    installment(300.0, 0.0, 300.0, false, "01 January 2020") //
            );
            assertPauseFails(403, "", () -> loanHelper.createInterestPause(zeroInterestLoanId,
                    pause("1 October 2019", "15 December 2019", TEXT_DATE_FORMAT)));
            verifyRepaymentSchedule(zeroInterestLoanId, //
                    installment(1200.0, null, "01 September 2019"), //
                    installment(300.0, 0.0, 300.0, false, "01 October 2019"), //
                    installment(300.0, 0.0, 300.0, false, "01 November 2019"), //
                    installment(300.0, 0.0, 300.0, false, "01 December 2019"), //
                    installment(300.0, 0.0, 300.0, false, "01 January 2020") //
            );
        });
    }

    private Long createLoan() {
        PostLoansRequest request = applyLP2ProgressiveLoanRequest(clientId, loanProductId, LOAN_START_DATE,
                Double.parseDouble(LOAN_PRINCIPAL_AMOUNT), INTEREST_RATE_PER_PERIOD, NUMBER_OF_REPAYMENTS,
                r -> r.repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)
                        .loanTermFrequency(NUMBER_OF_REPAYMENTS).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)
                        .externalId(externalId));
        Long newLoanId = applyForLoan(request);
        approveLoan(newLoanId, approveLoanRequest(Double.parseDouble(LOAN_PRINCIPAL_AMOUNT), LOAN_START_DATE));
        disburseLoan(newLoanId, new BigDecimal(LOAN_PRINCIPAL_AMOUNT), LOAN_START_DATE);
        return newLoanId;
    }

    private Long createPauseByLoanId(String startDate, String endDate) {
        CommandProcessingResult response = loanHelper.createInterestPause(loanId, pause(startDate, endDate));
        Assertions.assertNotNull(response.getResourceId());
        return response.getResourceId();
    }

    private Long createPauseByExternalId(String startDate, String endDate) {
        CommandProcessingResult response = loanHelper.createInterestPauseByExternalId(externalId, pause(startDate, endDate));
        Assertions.assertNotNull(response.getResourceId());
        return response.getResourceId();
    }

    private static InterestPauseRequestDto pause(String startDate, String endDate) {
        return pause(startDate, endDate, ISO_DATE_FORMAT);
    }

    private static InterestPauseRequestDto pause(String startDate, String endDate, String dateFormat) {
        return new InterestPauseRequestDto().startDate(startDate).endDate(endDate).dateFormat(dateFormat).locale("en");
    }

    private static void assertPauseFails(int expectedStatus, String expectedError, Executable call) {
        CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class, call);
        Assertions.assertEquals(expectedStatus, exception.getStatus());
        Assertions.assertTrue(exception.getMessage().contains(expectedError),
                "Expected error '" + expectedError + "' but was: " + exception.getMessage());
    }

    private static void assertContainsPause(List<InterestPauseResponseDto> pauses, String startDate, String endDate) {
        Assertions
                .assertTrue(
                        pauses.stream()
                                .anyMatch(p -> LocalDate.parse(startDate).equals(p.getStartDate())
                                        && LocalDate.parse(endDate).equals(p.getEndDate())),
                        "Expected a pause from " + startDate + " to " + endDate + " but found: " + pauses);
    }

    private static void assertDoesNotContainPause(List<InterestPauseResponseDto> pauses, Long variationId) {
        Assertions.assertTrue(pauses.stream().noneMatch(p -> variationId.equals(p.getId())),
                "Response should not contain the deleted variation ID " + variationId);
    }
}
