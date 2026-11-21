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
package org.apache.fineract.portfolio.shareaccounts.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountStatusType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Regression test for FINERACT-3319: undo approval is only allowed on an APPROVED share account and reject only on a
 * SUBMITTED_AND_PENDING_APPROVAL one. Any other status is refused with a validation error and the account is left
 * unchanged.
 */
class ShareAccountUndoApprovalAndRejectStatusTest {

    private static final String NOT_APPROVED = "validation.msg.sharesaccount.is.not.in.approved.status";
    private static final String NOT_PENDING_FOR_APPROVAL = "validation.msg.sharesaccount.is.not.pending.for.approval";

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final PlatformSecurityContext securityContext = mock(PlatformSecurityContext.class);
    private final ShareAccountDataSerializer serializer = new ShareAccountDataSerializer(securityContext, fromJsonHelper, null, null, null,
            null, null);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2025, 1, 1))));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private JsonCommand command(String json) {
        JsonElement parsed = fromJsonHelper.parse(json);
        return JsonCommand.from(json, parsed, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null);
    }

    private static ShareAccount accountIn(ShareAccountStatusType status) {
        ShareAccount account = mock(ShareAccount.class);
        when(account.status()).thenReturn(status.getValue());
        when(account.getShareAccountTransactions()).thenReturn(new HashSet<>());
        when(account.getPendingForApprovalSharePurchaseTransactions()).thenReturn(new HashSet<>());
        when(account.getCharges()).thenReturn(new HashSet<>());
        return account;
    }

    private static void assertSingleError(PlatformApiDataValidationException e, String code) {
        assertEquals(1, e.getErrors().size());
        assertEquals(code, e.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @ParameterizedTest
    @EnumSource(value = ShareAccountStatusType.class, names = { "SUBMITTED_AND_PENDING_APPROVAL", "ACTIVE", "REJECTED", "CLOSED" })
    void undoApprovalIsRefusedUnlessAccountIsApproved(ShareAccountStatusType status) {
        ShareAccount account = accountIn(status);

        PlatformApiDataValidationException e = assertThrows(PlatformApiDataValidationException.class,
                () -> serializer.validateAndUndoApprove(command("{}"), account));

        assertSingleError(e, NOT_APPROVED);
        verify(account, never()).undoApprove();
    }

    @Test
    void undoApprovalOfApprovedAccountIsAllowed() {
        ShareAccount account = accountIn(ShareAccountStatusType.APPROVED);

        serializer.validateAndUndoApprove(command("{}"), account);

        verify(account).undoApprove();
    }

    @ParameterizedTest
    @EnumSource(value = ShareAccountStatusType.class, names = { "APPROVED", "ACTIVE", "REJECTED", "CLOSED" })
    void rejectIsRefusedUnlessAccountIsPendingApproval(ShareAccountStatusType status) {
        ShareAccount account = accountIn(status);

        PlatformApiDataValidationException e = assertThrows(PlatformApiDataValidationException.class,
                () -> serializer.validateAndReject(command("{}"), account));

        assertSingleError(e, NOT_PENDING_FOR_APPROVAL);
        verify(account, never()).reject(any(), any());
    }

    @Test
    void rejectOfPendingAccountIsAllowed() {
        ShareAccount account = accountIn(ShareAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL);

        serializer.validateAndReject(command("{}"), account);

        verify(account).reject(any(), any());
    }
}
