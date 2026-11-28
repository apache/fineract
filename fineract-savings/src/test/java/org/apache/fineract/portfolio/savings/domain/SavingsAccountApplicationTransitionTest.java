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
package org.apache.fineract.portfolio.savings.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonParser;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.service.SavingsAccountApplicationTransitionValidator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pins the checks for approving, rejecting and withdrawing a savings application.
 */
class SavingsAccountApplicationTransitionTest {

    private static final LocalDate SUBMITTED_ON = LocalDate.of(2024, 1, 10);

    private final AppUser user = mock(AppUser.class);
    private final SavingsAccountApplicationTransitionValidator validator = new SavingsAccountApplicationTransitionValidator();

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 7, 1))));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void approvalChangesTheStatusAndRecordsTheDate() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        validator.validateApproval(account, command("approvedOnDate", "2024-02-01"));
        final Map<String, Object> changes = account.approveApplication(user, command("approvedOnDate", "2024-02-01"));

        assertEquals(SavingsAccountStatusType.APPROVED, account.getStatus());
        assertEquals(LocalDate.of(2024, 2, 1), account.getApprovedOnDate());
        assertEquals("[status, locale, dateFormat, approvedOnDate]", changes.keySet().toString());
        assertEquals("2024-02-01", changes.get("approvedOnDate"));
    }

    @Test
    void approvalOutsideThePendingStateIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.ACTIVE);

        assertEquals("validation.msg.savingsaccount.approval.not.in.submittedandpendingapproval.state|approvedOnDate|null",
                errors(() -> validator.validateApproval(account, command("approvedOnDate", "2024-02-01"))));
        assertEquals(SavingsAccountStatusType.ACTIVE, account.getStatus());
    }

    @Test
    void approvalBeforeTheSubmittalDateIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        assertEquals("validation.msg.savingsaccount.approval.cannot.be.before.submittal.date|approvedOnDate|2024-01-10",
                errors(() -> validator.validateApproval(account, command("approvedOnDate", "2024-01-09"))));
    }

    @Test
    void approvalInTheFutureIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        assertEquals("validation.msg.savingsaccount.approval.cannot.be.a.future.date|approvedOnDate|null",
                errors(() -> validator.validateApproval(account, command("approvedOnDate", "2024-07-02"))));
    }

    @Test
    void rejectionChangesTheStatusAndClosesTheApplication() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        validator.validateRejection(account, command("rejectedOnDate", "2024-02-01"));
        final Map<String, Object> changes = account.rejectApplication(user, command("rejectedOnDate", "2024-02-01"));

        assertEquals(SavingsAccountStatusType.REJECTED, account.getStatus());
        assertEquals("[status, locale, dateFormat, rejectedOnDate, closedOnDate]", changes.keySet().toString());
        assertEquals("2024-02-01", changes.get("closedOnDate"));
    }

    @Test
    void rejectionOutsideThePendingStateIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.APPROVED);

        assertEquals("validation.msg.savingsaccount.reject.not.in.submittedandpendingapproval.state|rejectedOnDate|null",
                errors(() -> validator.validateRejection(account, command("rejectedOnDate", "2024-02-01"))));
    }

    @Test
    void rejectionBeforeTheSubmittalDateIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        assertEquals("validation.msg.savingsaccount.reject.cannot.be.before.submittal.date|rejectedOnDate|2024-01-10",
                errors(() -> validator.validateRejection(account, command("rejectedOnDate", "2024-01-09"))));
    }

    @Test
    void rejectionInTheFutureIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        assertEquals("validation.msg.savingsaccount.reject.cannot.be.a.future.date|rejectedOnDate|2024-07-02",
                errors(() -> validator.validateRejection(account, command("rejectedOnDate", "2024-07-02"))));
    }

    @Test
    void withdrawalByTheApplicantChangesTheStatusAndClosesTheApplication() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        validator.validateApplicantWithdrawal(account, command("withdrawnOnDate", "2024-02-01"));
        final Map<String, Object> changes = account.applicantWithdrawsFromApplication(user, command("withdrawnOnDate", "2024-02-01"));

        assertEquals(SavingsAccountStatusType.WITHDRAWN_BY_APPLICANT, account.getStatus());
        assertEquals("[status, locale, dateFormat, withdrawnOnDate, closedOnDate]", changes.keySet().toString());
        assertEquals("2024-02-01", changes.get("closedOnDate"));
    }

    @Test
    void withdrawalOutsideThePendingStateIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.ACTIVE);

        assertEquals("validation.msg.savingsaccount.withdrawnByApplicant.not.in.submittedandpendingapproval.state|withdrawnOnDate|null",
                errors(() -> validator.validateApplicantWithdrawal(account, command("withdrawnOnDate", "2024-02-01"))));
    }

    @Test
    void withdrawalBeforeTheSubmittalDateIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        assertEquals("validation.msg.savingsaccount.withdrawnByApplicant.cannot.be.before.submittal.date|withdrawnOnDate|2024-01-10",
                errors(() -> validator.validateApplicantWithdrawal(account, command("withdrawnOnDate", "2024-01-09"))));
    }

    @Test
    void withdrawalInTheFutureIsRejected() {
        final SavingsAccount account = account(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        assertEquals("validation.msg.savingsaccount.withdrawnByApplicant.cannot.be.a.future.date|withdrawnOnDate|2024-07-02",
                errors(() -> validator.validateApplicantWithdrawal(account, command("withdrawnOnDate", "2024-07-02"))));
    }

    @Test
    void theStateIsCheckedBeforeTheDates() {
        final SavingsAccount account = account(SavingsAccountStatusType.ACTIVE);

        assertEquals("validation.msg.savingsaccount.approval.not.in.submittedandpendingapproval.state|approvedOnDate|null",
                errors(() -> validator.validateApproval(account, command("approvedOnDate", "2024-01-09"))));
    }

    private static String errors(final Runnable action) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class, action::run);
        return exception.getErrors().stream().map(error -> error.getUserMessageGlobalisationCode() + "|" + error.getParameterName() + "|"
                + String.valueOf(error.getArgs().get(0).getValue())).collect(Collectors.joining("; "));
    }

    private static JsonCommand command(final String dateParameter, final String date) {
        final String json = "{\"" + dateParameter + "\":\"" + date + "\",\"dateFormat\":\"yyyy-MM-dd\",\"locale\":\"en\"}";
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "savingsaccount", 1L, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    private static SavingsAccount account(final SavingsAccountStatusType status) {
        final SavingsAccount account = new SavingsAccount();
        setField(account, "status", status.getValue());
        setField(account, "submittedOnDate", SUBMITTED_ON);
        return account;
    }

    private static void setField(final Object target, final String fieldName, final Object value) {
        try {
            final Field field = SavingsAccount.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }
}
