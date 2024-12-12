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
package org.apache.fineract.portfolio.loanproduct.domain;

import static org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsValidator.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule.NEXT_INSTALLMENT;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType.DEFAULT;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType.IN_ADVANCE_PENALTY;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdvancedPaymentAllocationsJsonParserTest {

    @Mock
    private AdvancedPaymentAllocationsValidator advancedPaymentAllocationsValidator;

    @InjectMocks
    private AdvancedPaymentAllocationsJsonParser advancedPaymentAllocationsJsonParser;

    private FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Test
    public void testEmptyJson() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        JsonCommand command = createJsonCommand(map);

        // when
        List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules = advancedPaymentAllocationsJsonParser
                .assembleLoanProductPaymentAllocationRules(command, "other-strategy");

        // then
        Assertions.assertNull(loanProductPaymentAllocationRules);
        Mockito.verify(advancedPaymentAllocationsValidator, times(1)).validate(null, "other-strategy");
    }

    @Test
    public void testNullAllocationRuleJson() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        JsonCommand command = createJsonCommand(map);

        // when
        List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules = advancedPaymentAllocationsJsonParser
                .assembleLoanProductPaymentAllocationRules(command, "advanced-payment-allocation-strategy");

        // then
        Assertions.assertNull(loanProductPaymentAllocationRules);
        Mockito.verify(advancedPaymentAllocationsValidator, times(1)).validate(null, "advanced-payment-allocation-strategy");
    }

    @Test
    public void testParseSinglePaymentAllocation() throws JsonProcessingException {
        // given
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> paymentAllocations = new ArrayList<>();
        map.put("paymentAllocation", paymentAllocations);
        List<String> allocationRule = EnumSet.allOf(PaymentAllocationType.class).stream().map(Enum::name).toList();
        paymentAllocations.add(createPaymentAllocationEntry("DEFAULT", "NEXT_INSTALLMENT", allocationRule));
        JsonCommand command = createJsonCommand(map);

        // when
        List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules = advancedPaymentAllocationsJsonParser
                .assembleLoanProductPaymentAllocationRules(command, ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

        // then
        Assertions.assertEquals(1, loanProductPaymentAllocationRules.size());
        Assertions.assertEquals(NEXT_INSTALLMENT, loanProductPaymentAllocationRules.get(0).getFutureInstallmentAllocationRule());
        Assertions.assertEquals(DEFAULT, loanProductPaymentAllocationRules.get(0).getTransactionType());
        Assertions.assertEquals(12, loanProductPaymentAllocationRules.get(0).getAllocationTypes().size());
        Assertions.assertEquals(EnumSet.allOf(PaymentAllocationType.class).stream().toList(),
                loanProductPaymentAllocationRules.get(0).getAllocationTypes());

        Mockito.verify(advancedPaymentAllocationsValidator, times(1)).validate(loanProductPaymentAllocationRules,
                ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
        Mockito.verify(advancedPaymentAllocationsValidator, times(1))
                .validatePairOfOrderAndPaymentAllocationType(createPaymentAllocationTypeList());
        Mockito.verifyNoMoreInteractions(advancedPaymentAllocationsValidator);
    }

    @Test
    public void testInvalidTransactionTypeAndFutureAllocation() throws JsonProcessingException {
        // given
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> paymentAllocations = new ArrayList<>();
        map.put("paymentAllocation", paymentAllocations);
        List<String> allocationRule = EnumSet.allOf(PaymentAllocationType.class).stream().map(Enum::name).toList();
        paymentAllocations.add(createPaymentAllocationEntry("INVALID", "INVALID", allocationRule));
        JsonCommand command = createJsonCommand(map);

        // when
        List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules = advancedPaymentAllocationsJsonParser
                .assembleLoanProductPaymentAllocationRules(command, ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

        // then
        Assertions.assertEquals(1, loanProductPaymentAllocationRules.size());
        Assertions.assertNull(loanProductPaymentAllocationRules.get(0).getFutureInstallmentAllocationRule());
        Assertions.assertNull(loanProductPaymentAllocationRules.get(0).getTransactionType());
        Assertions.assertEquals(12, loanProductPaymentAllocationRules.get(0).getAllocationTypes().size());
        Assertions.assertEquals(EnumSet.allOf(PaymentAllocationType.class).stream().toList(),
                loanProductPaymentAllocationRules.get(0).getAllocationTypes());

        Mockito.verify(advancedPaymentAllocationsValidator, times(1)).validate(loanProductPaymentAllocationRules,
                ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
        Mockito.verify(advancedPaymentAllocationsValidator, times(1))
                .validatePairOfOrderAndPaymentAllocationType(createPaymentAllocationTypeList());
        Mockito.verifyNoMoreInteractions(advancedPaymentAllocationsValidator);
    }

    @Test
    public void testInvalidAndNullAllocationRules() throws JsonProcessingException {
        // given
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> paymentAllocations = new ArrayList<>();
        map.put("paymentAllocation", paymentAllocations);
        List<String> allocationRule = Arrays.asList(new String[] { "invalid", null, "IN_ADVANCE_PENALTY" });
        paymentAllocations.add(createPaymentAllocationEntry("DEFAULT", "NEXT_INSTALLMENT", allocationRule));
        JsonCommand command = createJsonCommand(map);

        // when
        List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules = advancedPaymentAllocationsJsonParser
                .assembleLoanProductPaymentAllocationRules(command, ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

        // then
        Assertions.assertEquals(1, loanProductPaymentAllocationRules.size());
        Assertions.assertEquals(NEXT_INSTALLMENT, loanProductPaymentAllocationRules.get(0).getFutureInstallmentAllocationRule());
        Assertions.assertEquals(DEFAULT, loanProductPaymentAllocationRules.get(0).getTransactionType());
        Assertions.assertEquals(3, loanProductPaymentAllocationRules.get(0).getAllocationTypes().size());
        Assertions.assertNull(loanProductPaymentAllocationRules.get(0).getAllocationTypes().get(0));
        Assertions.assertNull(loanProductPaymentAllocationRules.get(0).getAllocationTypes().get(1));
        Assertions.assertEquals(IN_ADVANCE_PENALTY, loanProductPaymentAllocationRules.get(0).getAllocationTypes().get(2));

        Mockito.verify(advancedPaymentAllocationsValidator, times(1)).validate(loanProductPaymentAllocationRules,
                ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
        Mockito.verify(advancedPaymentAllocationsValidator, times(1))
                .validatePairOfOrderAndPaymentAllocationType(List.of(Pair.of(1, null), Pair.of(2, null), Pair.of(3, IN_ADVANCE_PENALTY)));
        Mockito.verifyNoMoreInteractions(advancedPaymentAllocationsValidator);
    }

    @Test
    public void testNullTransactionTypeAndFutureAllocation() throws JsonProcessingException {
        // given
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> paymentAllocations = new ArrayList<>();
        map.put("paymentAllocation", paymentAllocations);
        List<String> allocationRule = EnumSet.allOf(PaymentAllocationType.class).stream().map(Enum::name).toList();
        paymentAllocations.add(createPaymentAllocationEntry(null, null, allocationRule));
        JsonCommand command = createJsonCommand(map);

        // when
        List<LoanProductPaymentAllocationRule> loanProductPaymentAllocationRules = advancedPaymentAllocationsJsonParser
                .assembleLoanProductPaymentAllocationRules(command, ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

        // then
        Assertions.assertEquals(1, loanProductPaymentAllocationRules.size());
        Assertions.assertNull(loanProductPaymentAllocationRules.get(0).getFutureInstallmentAllocationRule());
        Assertions.assertNull(loanProductPaymentAllocationRules.get(0).getTransactionType());
        Assertions.assertEquals(12, loanProductPaymentAllocationRules.get(0).getAllocationTypes().size());
        Assertions.assertEquals(EnumSet.allOf(PaymentAllocationType.class).stream().toList(),
                loanProductPaymentAllocationRules.get(0).getAllocationTypes());

        Mockito.verify(advancedPaymentAllocationsValidator, times(1)).validate(loanProductPaymentAllocationRules,
                ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
        Mockito.verify(advancedPaymentAllocationsValidator, times(1))
                .validatePairOfOrderAndPaymentAllocationType(createPaymentAllocationTypeList());
        Mockito.verifyNoMoreInteractions(advancedPaymentAllocationsValidator);
    }

    public Map<String, Object> createPaymentAllocationEntry(String transactionType, String futureInstallmentAllocation,
            List<String> orderedRules) {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionType", transactionType);
        map.put("futureInstallmentAllocationRule", futureInstallmentAllocation);
        List<Map<String, Object>> paymentAllocationOrder = new ArrayList<>();
        map.put("paymentAllocationOrder", paymentAllocationOrder);
        for (int i = 0; i < orderedRules.size(); i++) {
            HashMap<String, Object> entry = new HashMap<>();
            entry.put("paymentAllocationRule", orderedRules.get(i));
            entry.put("order", i + 1);
            paymentAllocationOrder.add(entry);
        }
        return map;
    }

    private static List<Pair<Integer, PaymentAllocationType>> createPaymentAllocationTypeList() {
        AtomicInteger i = new AtomicInteger(1);
        List<Pair<Integer, PaymentAllocationType>> list = EnumSet.allOf(PaymentAllocationType.class).stream()
                .map(p -> Pair.of(i.getAndIncrement(), p)).toList();
        return list;
    }

    @NotNull
    private JsonCommand createJsonCommand(Map<String, Object> jsonMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        JsonCommand command = JsonCommand.from(json, JsonParser.parseString(json), fromJsonHelper, null, 1L, 2L, 3L, 4L, null, null, null,
                null, null, null, null, null, null);
        return command;
    }

}
