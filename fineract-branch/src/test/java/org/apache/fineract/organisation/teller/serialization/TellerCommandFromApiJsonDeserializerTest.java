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
package org.apache.fineract.organisation.teller.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

class TellerCommandFromApiJsonDeserializerTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final TellerCommandFromApiJsonDeserializer underTest = new TellerCommandFromApiJsonDeserializer(fromJsonHelper);

    @Test
    public void testCashTxnValidJsonPassesValidation() throws JsonProcessingException {
        String json = cashTxnJson(BigDecimal.valueOf(1000), "10 September 2022", "Test note", "USD");
        assertDoesNotThrow(() -> underTest.validateForCashTxnForCashier(json));
    }

    @Test
    public void testCashTxnBlankOrNullJsonThrowsInvalidJsonException() {
        String whitespaceOnly = "   ";
        assertThrows(InvalidJsonException.class, () -> underTest.validateForCashTxnForCashier(null));
        assertThrows(InvalidJsonException.class, () -> underTest.validateForCashTxnForCashier(""));
        assertThrows(InvalidJsonException.class, () -> underTest.validateForCashTxnForCashier(whitespaceOnly));
    }

    @Test
    public void testCashTxnMissingTxnAmountThrowsValidationException() {
        assertPlatformValidationException("The parameter `txnAmount` is mandatory.", "validation.msg.teller.txnAmount.cannot.be.blank",
                () -> underTest.validateForCashTxnForCashier(cashTxnJson(null, "10 September 2022", "Test note", "USD")));
    }

    @Test
    public void testCashTxnMissingTxnDateThrowsValidationException() {
        assertPlatformValidationException("The parameter `txnDate` is mandatory.", "validation.msg.teller.txnDate.cannot.be.blank",
                () -> underTest.validateForCashTxnForCashier(cashTxnJson(BigDecimal.valueOf(1000), null, "Test note", "USD")));
    }

    @Test
    public void testCashTxnTxnNoteExceedingMaxLengthThrowsValidationException() {
        assertPlatformValidationException("The parameter `txnNote` exceeds max length of 200.",
                "validation.msg.teller.txnNote.exceeds.max.length", () -> underTest
                        .validateForCashTxnForCashier(cashTxnJson(BigDecimal.valueOf(1000), "10 September 2022", "A".repeat(201), "USD")));
    }

    @Test
    public void testCashTxnCurrencyCodeExceedingMaxLengthThrowsValidationException() {
        assertPlatformValidationException("The parameter `currencyCode` exceeds max length of 3.",
                "validation.msg.teller.currencyCode.exceeds.max.length", () -> underTest
                        .validateForCashTxnForCashier(cashTxnJson(BigDecimal.valueOf(1000), "10 September 2022", "Test note", "ABCD")));
    }

    @NonNull
    private String cashTxnJson(@Nullable BigDecimal txnAmount, @Nullable String txnDate, @Nullable String txnNote,
            @Nullable String currencyCode) throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        Optional.ofNullable(txnAmount).ifPresent(a -> map.put("txnAmount", a));
        Optional.ofNullable(txnDate).ifPresent(d -> map.put("txnDate", d));
        Optional.ofNullable(txnNote).ifPresent(n -> map.put("txnNote", n));
        Optional.ofNullable(currencyCode).ifPresent(c -> map.put("currencyCode", c));
        return createJsonCommand(map);
    }

    @NonNull
    private String createJsonCommand(Map<String, Object> jsonMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
    }

    private void assertPlatformValidationException(String message, String code, Executable executable) {
        PlatformApiDataValidationException validationException = assertThrows(PlatformApiDataValidationException.class, executable);
        assertPlatformException(message, code, validationException);
    }

    private void assertPlatformException(String expectedMessage, String expectedCode,
            PlatformApiDataValidationException platformApiDataValidationException) {
        Assertions.assertEquals(expectedMessage, platformApiDataValidationException.getErrors().get(0).getDefaultUserMessage());
        Assertions.assertEquals(expectedCode, platformApiDataValidationException.getErrors().get(0).getUserMessageGlobalisationCode());
    }
}
