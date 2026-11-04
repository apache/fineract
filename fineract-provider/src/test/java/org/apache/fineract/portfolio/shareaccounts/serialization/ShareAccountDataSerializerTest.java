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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccount;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountStatusType;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountTransaction;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

class ShareAccountDataSerializerTest {

    private static final String NOT_GREATER_THAN_ZERO = "validation.msg.sharesaccount.requestedShares.not.greater.than.zero";
    private static final String CANNOT_BE_BLANK = "validation.msg.sharesaccount.requestedShares.cannot.be.blank";

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final ShareAccountDataSerializer serializer = new ShareAccountDataSerializer(null, fromJsonHelper, null, null, null, null,
            null);

    private ShareAccount account;
    private ShareProduct product;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2025, 1, 1))));
        product = mock(ShareProduct.class);
        // Mockito would return 0L for these Long getters, which would fail the min/max checks for every request.
        when(product.getMaximumClientShares()).thenReturn(null);
        when(product.getMinimumClientShares()).thenReturn(null);
        when(product.deriveMarketPrice(any())).thenReturn(BigDecimal.TEN);
        account = mock(ShareAccount.class);
        when(account.status()).thenReturn(ShareAccountStatusType.ACTIVE.getValue());
        when(account.getShareProduct()).thenReturn(product);
        when(account.getTotalApprovedShares()).thenReturn(1000L);
        when(account.getShareAccountTransactions()).thenReturn(new HashSet<>());
        when(account.getCharges()).thenReturn(new HashSet<>());
    }

    private JsonCommand command(String json) {
        JsonElement parsed = fromJsonHelper.parse(json);
        return JsonCommand.from(json, parsed, fromJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null);
    }

    private static String purchaseBody(String requestedShares) {
        return "{\"requestedDate\":\"01 January 2025\",\"requestedShares\":" + requestedShares
                + ",\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\"}";
    }

    private static String updateBody(String requestedShares) {
        return "{\"requestedShares\":" + requestedShares + ",\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\"}";
    }

    private static void assertSingleError(PlatformApiDataValidationException e, String code) {
        assertEquals(1, e.getErrors().size());
        assertEquals(code, e.getErrors().get(0).getUserMessageGlobalisationCode());
        assertEquals("requestedShares", e.getErrors().get(0).getParameterName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-1", "-100", "-9223372036854775808" })
    void applyAdditionalSharesRejectsZeroAndNegative(String shares) {
        PlatformApiDataValidationException e = assertThrows(PlatformApiDataValidationException.class,
                () -> serializer.validateAndApplyAddtionalShares(command(purchaseBody(shares)), account));

        assertSingleError(e, NOT_GREATER_THAN_ZERO);
        verify(account, never()).addAdditionalPurchasedShares(any());
    }

    @Test
    void applyAdditionalSharesRejectsMissingShares() {
        String json = "{\"requestedDate\":\"01 January 2025\",\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\"}";

        PlatformApiDataValidationException e = assertThrows(PlatformApiDataValidationException.class,
                () -> serializer.validateAndApplyAddtionalShares(command(json), account));

        assertSingleError(e, CANNOT_BE_BLANK);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1", "100" })
    void applyAdditionalSharesAcceptsPositive(String shares) {
        Map<String, Object> changes = serializer.validateAndApplyAddtionalShares(command(purchaseBody(shares)), account);

        ArgumentCaptor<ShareAccountTransaction> captor = ArgumentCaptor.forClass(ShareAccountTransaction.class);
        verify(account).addAdditionalPurchasedShares(captor.capture());
        assertEquals(Long.parseLong(shares), captor.getValue().getTotalShares());
        assertNotNull(changes.get(ShareAccountApiConstants.additionalshares_paramname));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-1", "-100", "-9223372036854775808" })
    void updateRejectsZeroAndNegative(String shares) {
        PlatformApiDataValidationException e = assertThrows(PlatformApiDataValidationException.class,
                () -> serializer.validateAndUpdate(command(updateBody(shares)), account));

        assertSingleError(e, NOT_GREATER_THAN_ZERO);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1", "100" })
    void updateAcceptsPositive(String shares) {
        Map<String, Object> changes = serializer.validateAndUpdate(command(updateBody(shares)), account);

        assertEquals("Transaction", changes.get(ShareAccountApiConstants.requestedshares_paramname));
    }

    @Test
    void updateWithoutSharesIsStillAllowed() {
        String json = "{\"externalId\":\"ext-1\",\"locale\":\"en\"}";

        Map<String, Object> changes = serializer.validateAndUpdate(command(json), account);

        assertFalse(changes.containsKey(ShareAccountApiConstants.requestedshares_paramname));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-100" })
    void redeemSharesRejectsZeroAndNegative(String shares) {
        PlatformApiDataValidationException e = assertThrows(PlatformApiDataValidationException.class,
                () -> serializer.validateAndRedeemShares(command(purchaseBody(shares)), account));

        assertSingleError(e, NOT_GREATER_THAN_ZERO);
        verify(account, never()).addAdditionalPurchasedShares(any());
    }
}
