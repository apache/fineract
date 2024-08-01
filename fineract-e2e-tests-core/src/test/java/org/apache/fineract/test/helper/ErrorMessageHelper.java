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
package org.apache.fineract.test.helper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanAccountLockResponse;
import org.apache.fineract.client.models.Header;
import retrofit2.Response;

public final class ErrorMessageHelper {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private ErrorMessageHelper() {}

    public static String requestFailed(Response response) throws IOException {
        return String.format("Request failed. Error:%n%s", response.errorBody() != null ? response.errorBody().string() : null);
    }

    public static String requestFailedWithCode(Response response) {
        return String.format("Response has error code: %2d", response.code());
    }

    public static String batchRequestFailedWithCode(BatchResponse response) {
        return String.format("Response has error code: %2d in request: %2d", response.getStatusCode(), response.getRequestId());
    }

    public static String chargeAppliesToIsInvalid(Enum chargeAppliesTo) {
        return String.format("%s is invalid input for charge applies to field", chargeAppliesTo);
    }

    public static String dateFailureErrorCodeMsg() {
        return "Loan has a wrong http status";
    }

    public static String disburseDateFailure(Integer loanId) {
        String loanIdStr = parseLoanIdToString(loanId);
        return String.format("The date on which a loan with identifier : %s is disbursed cannot be in the future.", loanIdStr);
    }

    public static String disburseMaxAmountFailure() {
        return "Loan disbursal amount can't be greater than maximum applied loan amount calculation. Total disbursed amount: [0-9]*  Maximum disbursal amount: [0-9]*";
    }

    public static String disbursePastDateFailure(Integer loanId, String actualDisbursementDate) {
        return String.format("The date on which a loan is disbursed cannot be before its approval date: %s", actualDisbursementDate);
    }

    public static String loanSubmitDateInFutureFailureMsg() {
        return "The date on which a loan is submitted cannot be in the future.";
    }

    public static String loanApproveDateInFutureFailureMsg() {
        return "The date on which a loan is approved cannot be in the future.";
    }

    public static String loanApproveMaxAmountFailureMsg() {
        return "Loan approved amount can't be greater than maximum applied loan amount calculation.";
    }

    public static String loanFraudFlagModificationMsg(String loanId) {
        return String.format("Loan Id: %s mark as fraud is not allowed as loan status is not active", loanId);

    }

    public static String transactionDateInFutureFailureMsg() {
        return "The transaction date cannot be in the future.";
    }

    public static String repaymentUndoFailureDueToChargeOff(Long loanId) {
        String loanIdStr = String.valueOf(loanId);
        return String.format("Loan transaction: %s adjustment is not allowed before or on the date when the loan got charged-off",
                loanIdStr);
    }

    public static String secondChargeOffFailure(Long loanId) {
        String loanIdStr = String.valueOf(loanId);
        return String.format("Loan: %s is already charged-off", loanIdStr);
    }

    public static String repaymentUndoFailureDueToChargeOffCodeMsg() {
        return "Undo not possible if the loan was charged-off";
    }

    public static String chargeOffUndoFailureCodeMsg() {
        return "Charge-Off Undo is not possible before the last transaction date";
    }

    public static String chargeOffUndoFailure(Long loanId) {
        String loanIdStr = String.valueOf(loanId);
        return String.format("Loan: %s charge-off cannot be executed. User transaction was found after the charge-off transaction date!",
                loanIdStr);
    }

    public static String notChargedOffFailure(Long loanId) {
        String loanIdStr = String.valueOf(loanId);
        return String.format("Loan: %s is not charged-off", loanIdStr);
    }

    public static String addChargeForChargeOffLoanCodeMsg() {
        return "Adding charge to a Charged-Off loan is not allowed.";
    }

    public static String addChargeForChargeOffLoanFailure(Long loanId) {
        String loanIdStr = String.valueOf(loanId);
        return String.format("Adding charge to Loan: %s is not allowed. Loan Account is Charged-off", loanIdStr);
    }

    public static String wrongAmountInRepaymentSchedule(int line, BigDecimal actual, BigDecimal expected) {
        String lineToStr = String.valueOf(line);
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule. Actual amount for line %s is: %s - But expected amount is: %s", lineToStr,
                actualToStr, expectedToStr);
    }

    public static String wrongAmountInRepaymentSchedulePrincipal(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Principal. Actual amount is: %s - But expected amount is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongAmountInRepaymentScheduleInterest(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Interest. Actual amount is: %s - But expected amount is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongAmountInRepaymentScheduleFees(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Fees. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInRepaymentSchedulePenalties(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Penalties. Actual amount is: %s - But expected amount is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongAmountInRepaymentScheduleDue(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Due. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInRepaymentSchedulePaid(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Paid. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInRepaymentScheduleInAdvance(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / In advance. Actual amount is: %s - But expected amount is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongAmountInRepaymentScheduleLate(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Late. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInRepaymentScheduleOutstanding(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Outstanding. Actual amount is: %s - But expected amount is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongAmountInRepaymentScheduleWaived(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Repayment schedule / Waived. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongDataInTransactionsTransactionType(String actual, String expected) {
        return String.format("Wrong data in Transactions / Transaction type. Actual value is: %s - But expected value is: %s", actual,
                expected);
    }

    public static String wrongDataInTransactionsTransactionDate(String actual, String expected) {
        return String.format("Wrong data in Transactions / Transaction date. Actual value is: %s - But expected value is: %s", actual,
                expected);
    }

    public static String transactionIsNotReversedError(Boolean actual, Boolean expected) {
        return String.format("The transaction should be reversed, but it is not. Actual value is: %s - But expected value is: %s", actual,
                expected);
    }

    public static String wrongAmountInTransactionsAmount(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Transactions / Amount. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInTransactionsPrincipal(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Transactions / Principal. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInTransactionsInterest(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Transactions / Interest. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInTransactionsFees(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Transactions / Fees. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInTransactionsPenalties(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Transactions / Penalties. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInTransactionsOverpayment(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Transactions / Overpayment. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInTransactionsBalance(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Transactions / Loan Balance. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongDataInChargesName(String actual, String expected) {
        return String.format("Wrong data in Charges / Name. Actual value is: %s - But expected value is: %s", actual, expected);
    }

    public static String wrongDataInChargesIsPenalty(String actual, String expected) {
        return String.format("Wrong data in Charges / isPenalty. Actual value is: %s - But expected value is: %s", actual, expected);
    }

    public static String wrongDataInChargesDueDate(String actual, String expected) {
        return String.format("Wrong data in Charges / Due Date. Actual value is: %s - But expected value is: %s", actual, expected);
    }

    public static String wrongDataInChargesAmountDue(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Charges / Due amount. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongDataInChargesAmountPaid(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Charges / Paid amount. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongDataInChargesAmountWaived(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Charges / Waived amount. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongDataInChargesAmountOutstanding(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Charges / Outstanding amount. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInTotalOutstanding(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Loan total outstanding. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongAmountInTotalOverdue(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong amount in Loan total overdue. Actual amount is: %s - But expected amount is: %s", actualToStr,
                expectedToStr);
    }

    public static String wrongLastPaymentAmount(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong last payment amount. Actual last payment amount is: %s - But expected last payment amount is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongDataInDelinquentLastRepaymentAmount(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format(
                "Wrong amount in Loan details delinquent.lastRepaymentAmount. Actual amount is: %s - But expected amount is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongDataInDelinquentLastRepaymentDate(String actual, String expected) {
        return String.format("Wrong amount in Loan details delinquent.lastRepaymentDate. Actual date is: %s - But expected date is: %s",
                actual, expected);
    }

    public static String wrongLoanStatus(Integer actual, Integer expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong Loan status ID. Actual ID is: %s - But expected ID is: %s", actualToStr, expectedToStr);
    }

    public static String wrongFraudFlag(Boolean actualFraudStatus, Boolean expectedFraudStatus) {
        return String.format("Wrong Loan fraud flag. Actual Fraud status is: %s - Expected Fraud status is: %s", actualFraudStatus,
                expectedFraudStatus);
    }

    public static String delinquencyRangeError(String actual, String expected) {
        return String.format("Wrong Delinquency range. Actual range is: %s - Expected range is: %s", actual, expected);
    }

    private static String parseLoanIdToString(Integer loanId) {
        return StringUtils.repeat("0", 9 - loanId.toString().length()) + loanId.toString();
    }

    public static String loanRepaymentOnClosedLoanFailureMsg() {
        return "Loan Repayment (or its types) or Waiver is not allowed. Loan Account is not active.";
    }

    public static String noTransactionMetCriteria(String transactionType, String date) {
        return String.format(
                "There are no transaction in Transactions met the following criteria: Transaction type = %s, Transaction date = %s",
                transactionType, date);
    }

    public static String missingMatchInJournalEntries(Map<String, String> entryPairs,
            List<GetJournalEntriesTransactionIdResponse> entryDataList) {
        String entryPairsStr = entryPairs.toString();
        String entryDataListStr = entryDataList.toString();
        return String.format("One or more entry pairs missing from Journal entries. Expected entry pairs: %s. Actual Journal entries: %s",
                entryPairsStr, entryDataListStr);
    }

    public static String wrongErrorCodeInFailedChargeAdjustment(Integer actual, Integer expected) {
        return String.format("Not the expected error code in error body: Actual error message is: %s. Expected error code is: %s",
                actual.toString(), expected.toString());
    }

    public static String wrongStatusCode(Integer actual, Integer expected) {
        return String.format("Not the expected http status code: Actual code is: %s. Expected code is: %s", actual.toString(),
                expected.toString());
    }

    public static String wrongErrorMessageInFailedChargeAdjustment(String actual, String expected) {
        return String.format("Not the expected error message in error body: Actual error code is: %s. Expected error message is: %s",
                actual, expected);
    }

    public static String wrongErrorMessage(String actual, String expected) {
        return String.format("Not the expected error message in error body: Actual error message is: %s. Expected error message is: %s",
                actual, expected);
    }

    public static String wrongValueInResponseHeader(String headerKey, String actual, String expected) {
        return String.format("Not the expected value in header '%s': Actual value is: %s. Expected value is: %s", headerKey, actual,
                expected);
    }

    public static String wrongNrOfTransactions(String transactionType, int actual, int expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);
        return String.format(
                "Not the expected number of '%s' transactions in Transactions tab: Actual number of transactions: %s. Expected  number of transactions: %s",
                transactionType, actualStr, expectedStr);
    }

    public static String wrongAmountInTransactionsResponse(Double actual, Double expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);
        return String.format("Wrong amount in Transactions response. Actual value is: %s - But expected value is: %s", actualStr,
                expectedStr);
    }

    public static String wrongClientIdInTransactionResponse(Long actual, Long expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);
        return String.format("Wrong Client ID in Transactions response. Actual value is: %s - But expected value is: %s", actualStr,
                expectedStr);
    }

    public static String wrongLoanIdInTransactionResponse(Long actual, Long expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);
        return String.format("Wrong Loan ID in Transactions response. Actual value is: %s - But expected value is: %s", actualStr,
                expectedStr);
    }

    public static String noHeaderKeyFound(List<Header> headersList, String headerKey) {
        return String.format("Header key: %s was not found in headers list: %s", headerKey, headersList.toString());
    }

    public static String idempotencyKeyNoMatch(String actual, String expected) {
        return String.format("Idempotency key is not matching:  Actual value is: %s - But expected value is: %s", actual, expected);
    }

    public static String wrongNumberOfLinesInRepaymentSchedule(int actual, int expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);
        return String.format("Number of lines in Repayment schedule is not correct. Actual value is: %s - Expected value is: %s", actualStr,
                expectedStr);
    }

    public static String wrongValueInLineInRepaymentSchedule(int line, List<List<String>> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String expectedStr = expected.toString();
        StringBuilder sb = new StringBuilder();
        for (List<String> innerList : actual) {
            sb.append(innerList.toString());
            sb.append(System.lineSeparator());
        }

        return String.format(
                "%nWrong value in Repayment schedule tab line %s. %nActual values in line (with the same due date) are: %n%s %nExpected values in line: %n%s",
                lineStr, sb.toString(), expectedStr);
    }

    public static String wrongValueInLineInTransactionsTab(int line, List<List<String>> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String expectedStr = expected.toString();
        StringBuilder sb = new StringBuilder();
        for (List<String> innerList : actual) {
            sb.append(innerList.toString());
            sb.append(System.lineSeparator());
        }

        return String.format(
                "%nWrong value in Transactions tab line %s. %nActual values in line (with the same date) are: %n%s %nExpected values in line: %n%s",
                lineStr, sb.toString(), expectedStr);
    }

    public static String nrOfLinesWrongInTransactionsTab(int actual, int expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);

        return String.format(
                "%nNumber of lines does not match in Transactions tab and expected datatable. %nNumber of transaction tab lines: %s %nNumber of expected datatable lines: %s%n",
                actualStr, expectedStr);
    }

    public static String wrongValueInLineInChargesTab(int line, List<List<String>> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String expectedStr = expected.toString();
        StringBuilder sb = new StringBuilder();
        for (List<String> innerList : actual) {
            sb.append(innerList.toString());
            sb.append(System.lineSeparator());
        }

        return String.format(
                "%nWrong value in Charges tab line %s. %nActual values in line (with the same date) are: %n%s %nExpected values in line: %n%s",
                lineStr, sb.toString(), expectedStr);
    }

    public static String wrongValueInLineInJournalEntries(int line, List<List<List<String>>> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String expectedStr = expected.toString();
        StringBuilder sb = new StringBuilder();
        for (List<List<String>> innerList : actual) {
            sb.append(innerList.toString());
            sb.append(System.lineSeparator());
        }

        return String.format(
                "%nWrong value in Journal entries line %s. %nActual values for the possible transactions in line (with the same date) are: %n%s %nExpected values in line: %n%s",
                lineStr, sb.toString(), expectedStr);
    }

    public static String wrongDataInJournalEntriesGlAccountType(int line, String actual, String expected) {
        return String.format("Wrong data in Journal entries, line %s / GL account type. Actual value is: %s - But expected value is: %s",
                line, actual, expected);
    }

    public static String wrongDataInJournalEntriesGlAccountCode(int line, String actual, String expected) {
        return String.format("Wrong data in Journal entries, line %s / GL account code. Actual value is: %s - But expected value is: %s",
                line, actual, expected);
    }

    public static String wrongDataInJournalEntriesGlAccountName(int line, String actual, String expected) {
        return String.format("Wrong data in Journal entries, line %s / GL account name. Actual value is: %s - But expected value is: %s",
                line, actual, expected);
    }

    public static String wrongDataInJournalEntriesDebit(int line, String actual, String expected) {
        return String.format("Wrong data in Journal entries, line %s / Debit. Actual value is: %s - But expected value is: %s", line,
                actual, expected);
    }

    public static String wrongDataInJournalEntriesCredit(int line, String actual, String expected) {
        return String.format("Wrong data in Journal entries, line %s / Credit. Actual value is: %s - But expected value is: %s", line,
                actual, expected);
    }

    public static String wrongDataInActualMaturityDate(String actual, String expected) {
        return String.format("Wrong data in Loan details/Timeline/actualMaturityDate. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInExpectedMaturityDate(String actual, String expected) {
        return String.format("Wrong data in Loan details/Timeline/expectedMaturityDate. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInLastPaymentAmount(String actual, String expected) {
        return String.format("Wrong data in Loan details/delinquent/lastPaymentAmount. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInLastPaymentDate(String actual, String expected) {
        return String.format("Wrong data in Loan details/delinquent/lastPaymentDate. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInLastRepaymentAmount(String actual, String expected) {
        return String.format("Wrong data in Loan details/delinquent/lastRepaymentAmount. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInLastRepaymentDate(String actual, String expected) {
        return String.format("Wrong data in Loan details/delinquent/lastRepaymentDate. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInLoanDetailsLoanChargePaidByListAmount(String actual, String expected) {
        return String.format("Wrong data in Loan details/loanChargePaidByList/amount. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInLoanDetailsLoanChargePaidByListName(String actual, String expected) {
        return String.format("Wrong data in Loan details/loanChargePaidByList/name. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInLoanTransactionMakeRepaymentPostEventLoanChargePaidByListAmount(String actual, String expected) {
        return String.format(
                "Wrong data in LoanTransactionMakeRepaymentPostEvent/loanChargePaidByList/amount. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInLoanTransactionMakeRepaymentPostEventLoanChargePaidByListName(String actual, String expected) {
        return String.format(
                "Wrong data in LoanTransactionMakeRepaymentPostEvent/loanChargePaidByList/name. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongDataInDelinquencyHistoryClassification(String actual, String expected) {
        return String.format("Wrong data in Delinquency History/classification. Actual value is: %s - But expected value is: %s", actual,
                expected);
    }

    public static String wrongDataInDelinquencyHistoryAddedOnDate(String actual, String expected) {
        return String.format("Wrong data in Delinquency History/addedOnDate. Actual value is: %s - But expected value is: %s", actual,
                expected);
    }

    public static String wrongDataInDelinquencyHistoryLiftedOnDate(String actual, String expected) {
        return String.format("Wrong data in Delinquency History/liftedOnDate. Actual value is: %s - But expected value is: %s", actual,
                expected);
    }

    public static String wrongDataInAssetExternalizationResponse(String actual, String expected) {
        return String.format("Wrong data in Asset Externalization response. Actual value is: %s - But expected value is: %s", actual,
                expected);
    }

    public static String wrongDataInAssetExternalizationResponse(Long actual, Long expected) {
        return String.format("Wrong data in Asset Externalization response. Actual value is: %s - But expected value is: %s", actual,
                expected);
    }

    public static String wrongDataInAssetExternalizationTransferExternalId(String actual, String expected) {
        return String.format("Wrong data in Asset Externalization - transfer_external_id. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongData(String actual, String expected) {
        return String.format("Wrong data. Actual value is: %s - But expected value is: %s", actual, expected);
    }

    public static String wrongValueInExternalAssetDetails(int line, List<List<String>> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String expectedStr = expected.toString();

        StringBuilder sb = new StringBuilder();
        for (List<String> innerList : actual) {
            sb.append(innerList.toString());
            sb.append(System.lineSeparator());
        }

        return String.format(
                "%nWrong value in External Asset details line %s. %nActual values in line are: %n%s %nExpected values in line: %n%s",
                lineStr, sb.toString(), expectedStr);
    }

    public static String wrongTotalFilteredRecordsInAssetExternalizationDetails(int actual, int expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);

        return String.format(
                "%nNumber of totalFilteredRecords does not match in Asset Externalization details. %nActual number of totalFilteredRecords: %s %nExpected number of totalFilteredRecords: %s%n",
                actualStr, expectedStr);
    }

    public static String wrongValueInLineInAssetExternalizationJournalEntry(int line, List<List<String>> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String expectedStr = expected.toString();
        StringBuilder sb = new StringBuilder();
        for (List<String> innerList : actual) {
            sb.append(innerList.toString());
            sb.append(System.lineSeparator());
        }

        return String.format(
                "%nWrong value in Asset Externalization Journal Entry tab line %s. %nActual values in line are: %n%s %nExpected values in line: %n%s",
                lineStr, sb.toString(), expectedStr);
    }

    public static String wrongNumberOfLinesInAssetExternalizationJournalEntry(int actual, int expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);
        return String.format(
                "Number of lines in Asset Externalization Journal Entry is not correct. Actual value is: %s - Expected value is: %s",
                actualStr, expectedStr);
    }

    public static String wrongErrorCode(Integer actual, Integer expected) {
        return String.format("Not the expected error code in error body: Actual error code is: %s. Expected error code is: %s",
                actual.toString(), expected.toString());
    }

    public static String idNull() {
        return "The requested ID is null";
    }

    public static String wrongLastCOBProcessedLoanDate(LocalDate actual, LocalDate expected) {
        String actualStr = FORMATTER.format(actual);
        String expectedStr = FORMATTER.format(expected);
        return String.format(
                "Processed date of last loan processed by COB is wrong. Actual value is %s, but it should be earlier than %s. ", actualStr,
                expectedStr);
    }

    public static String listOfLockedLoansNotEmpty(Response<GetLoanAccountLockResponse> response) {
        String bodyStr = response.body().toString();
        return String.format("List of locked loan accounts is not empty. Actual response is: %n%s", bodyStr);
    }

    public static String listOfLockedLoansContainsLoan(Long loanId, Response<GetLoanAccountLockResponse> response) {
        String bodyStr = response.body().toString();
        return String.format("List of locked loan accounts contains the loan with loanId %s. List of locked loans: %n%s", loanId, bodyStr);
    }

    public static String wrongValueInLineDelinquencyActions(int line, List<String> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String expectedStr = expected.toString();
        String actualStr = actual.toString();

        return String.format(
                "%nWrong value in Delinquency actions response line %s. %nActual values in line are: %s %nExpected values in line: \s\s%s",
                lineStr, actualStr, expectedStr);
    }

    public static String wrongNumberOfLinesInDelinquencyActions(int actual, int expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);
        return String.format(
                "Number of items (lines) in DelinquencyActions response is not correct. Actual value is: %s - Expected value is: %s",
                actualStr, expectedStr);
    }

    public static String wrongValueInPauseDelinquencyEventActive(int itemNr, Boolean actual, Boolean expected) {
        return String.format(
                "Wrong value in LoanAccountDelinquencyPauseChangedBusinessEvent/delinquent/delinquencyPausePeriods/active item Nr: %s . %nActual value is: %s - Expected value is: %s",
                itemNr, actual, expected);
    }

    public static String wrongValueInPauseDelinquencyEventStartDate(int itemNr, String actual, String expected) {
        return String.format(
                "Wrong value in LoanAccountDelinquencyPauseChangedBusinessEvent/delinquent/delinquencyPausePeriods/pausePeriodStart item Nr: %s . %nActual value is: %s - Expected value is: %s",
                itemNr, actual, expected);
    }

    public static String wrongValueInPauseDelinquencyEventEndDate(int itemNr, String actual, String expected) {
        return String.format(
                "Wrong value in LoanAccountDelinquencyPauseChangedBusinessEvent/delinquent/delinquencyPausePeriods/pausePeriodEnd item Nr: %s . %nActual value is: %s - Expected value is: %s",
                itemNr, actual, expected);
    }

    public static String wrongValueInLineInInstallmentLevelDelinquencyData(int line, List<String> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String actualStr = actual.toString();
        String expectedStr = expected.toString();

        return String.format(
                "%nWrong value in Installment level delinquency data, line %s. %nActual values in line: \s\s%s %nExpected values in line: %s",
                lineStr, actualStr, expectedStr);
    }

    public static String wrongValueInLoanLevelDelinquencyData(List<String> actual, List<String> expected) {
        String actualStr = actual.toString();
        String expectedStr = expected.toString();

        return String.format("%nWrong value in LOAN level delinquency data. %nActual values are:\s\s %s %nExpected values are: %s",
                actualStr, expectedStr);
    }

    public static String nrOfLinesWrongInInstallmentLevelDelinquencyData(int actual, int expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);

        return String.format(
                "%nNumber of lines does not match in Installment level delinquency data and expected datatable. %nNumber of transaction tab lines: %s %nNumber of expected datatable lines: %s%n",
                actualStr, expectedStr);
    }

    public static String wrongAmountInLoanDelinquencyRangeChangedEventTotalAmount(BigDecimal actual, BigDecimal expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format(
                "%nWrong amount in LoanDelinquencyRangeChangeBusinessEvent - totalAmount. %ninstallmentDelinquencyBuckets/amount/totalAmount: %s %nSum of installmentDelinquencyBuckets/amount/{principalAmount, interestAmount, feeAmount, penaltyAmount}: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongValueInLineInDelinquencyPausePeriodData(int line, List<List<String>> actual, List<String> expected) {
        String lineStr = String.valueOf(line);
        String expectedStr = expected.toString();
        StringBuilder sb = new StringBuilder();
        for (List<String> innerList : actual) {
            sb.append(innerList.toString());
            sb.append(System.lineSeparator());
        }

        return String.format(
                "%nWrong value in Delinquency pause periods line %s. %nActual values in line: %s %nExpected values in line: %s", lineStr,
                sb.toString(), expectedStr);
    }

    public static String nrOfLinesWrongInLoanDelinquencyPauseData(int actual, int expected) {
        String actualStr = String.valueOf(actual);
        String expectedStr = String.valueOf(expected);

        return String.format(
                "%nNumber of lines does not match in Loan delinquency pause data and expected datatable. %nNumber of items in loanDetails/delinquent/delinquencyPausePeriods: %s %nNumber of expected datatable lines: %s%n",
                actualStr, expectedStr);
    }

    public static String wrongDataInNextPaymentDueDate(String actual, String expected) {
        return String.format("Wrong data in Loan details / delinquent.nextPaymentDueDate. Actual value is: %s - But expected value is: %s",
                actual, expected);
    }

    public static String wrongAmountInTotalRepaymentTransaction(Double actual, Double expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format(
                "Wrong amount in Loan details / summary/totalRepaymentTransaction. Actual amount is: %s - But expected amount is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongValueInLoanDelinquencyRangeChangeBusinessEvent1(Long actual, Long expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format(
                "Wrong value in LoanDelinquencyRangeChangeBusinessEvent -> Installment level delinquency -> delinquencyRange/id. %nActual value is: %s %nExpected value is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongValueInLoanDelinquencyRangeChangeBusinessEvent2(String actual, String expected) {
        return String.format(
                "Wrong value in LoanDelinquencyRangeChangeBusinessEvent -> Installment level delinquency -> delinquencyRange/classification. %nActual value is: %s %nExpected value is: %s",
                actual, expected);
    }

    public static String wrongValueInLoanDelinquencyRangeChangeBusinessEvent3(BigDecimal actual, BigDecimal expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format(
                "Wrong value in LoanDelinquencyRangeChangeBusinessEvent -> Installment level delinquency -> amount/totalAmount. %nActual value is: %s %nExpected value is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongValueInLoanDelinquencyRangeChangeBusinessEvent4(Long actual, Long expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format(
                "Wrong value in LoanDelinquencyRangeChangeBusinessEvent -> Loan level delinquency -> delinquencyRange/id. %nActual value is: %s %nExpected value is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongValueInLoanDelinquencyRangeChangeBusinessEvent5(String actual, String expected) {
        return String.format(
                "Wrong value in LoanDelinquencyRangeChangeBusinessEvent -> Loan level delinquency -> delinquencyRange/classification. %nActual value is: %s %nExpected value is: %s",
                actual, expected);
    }

    public static String wrongValueInLoanDelinquencyRangeChangeBusinessEvent6(BigDecimal actual, BigDecimal expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format(
                "Wrong value in LoanDelinquencyRangeChangeBusinessEvent -> Loan level delinquency -> amount/totalAmount. %nActual value is: %s %nExpected value is: %s",
                actualToStr, expectedToStr);
    }

    public static String wrongValueInLoanDelinquencyRangeChangeBusinessEvent7(String actual, String expected) {
        return String.format(
                "Wrong value in LoanDelinquencyRangeChangeBusinessEvent -> Loan level delinquency -> delinquentDate. %nActual value is: %s %nExpected value is: %s",
                actual, expected);
    }

    public static String wrongfixedLength(Integer actual, Integer expected) {
        String actualToStr = actual.toString();
        String expectedToStr = expected.toString();
        return String.format("Wrong value in LoanDeteils/fixedLength. %nActual value is: %s %nExpected Value is: %s", actualToStr,
                expectedToStr);
    }
}
