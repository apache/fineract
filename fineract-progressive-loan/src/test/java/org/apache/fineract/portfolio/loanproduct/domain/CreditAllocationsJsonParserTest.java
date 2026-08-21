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

import static org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationsValidator.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.mockito.Mockito.times;

import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
public class CreditAllocationsJsonParserTest {

    @InjectMocks
    private CreditAllocationsJsonParser creditAllocationsJsonParser;

    @Mock
    private CreditAllocationsValidator creditAllocationsValidator;

    private FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Test
    public void testEmptyJson() throws JacksonException {
        Map<String, Object> map = new HashMap<>();
        JsonCommand command = createJsonCommand(map);

        // when
        List<LoanProductCreditAllocationRule> loanProductCreditAllocationRules = creditAllocationsJsonParser
                .assembleLoanProductCreditAllocationRules(command, "other-strategy");

        // then
        Assertions.assertNull(loanProductCreditAllocationRules);
        Mockito.verify(creditAllocationsValidator, times(1)).validate(null, "other-strategy");
    }

    @Test
    public void testParseSingleChargebackAllocation() throws JacksonException {
        // given
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> creditAllocations = new ArrayList<>();
        map.put("creditAllocation", creditAllocations);
        List<String> allocationRule = EnumSet.allOf(AllocationType.class).stream().map(Enum::name).toList();
        creditAllocations.add(createCreditAllocationEntry("CHARGEBACK", allocationRule));
        JsonCommand command = createJsonCommand(map);

        // when
        List<LoanProductCreditAllocationRule> creditAllocationRules = creditAllocationsJsonParser
                .assembleLoanProductCreditAllocationRules(command, ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

        // then
        Assertions.assertEquals(1, creditAllocationRules.size());
        Assertions.assertEquals(CreditAllocationTransactionType.CHARGEBACK, creditAllocationRules.get(0).getTransactionType());
        Assertions.assertEquals(4, creditAllocationRules.get(0).getAllocationTypes().size());
        Assertions.assertEquals(EnumSet.allOf(AllocationType.class).stream().toList(), creditAllocationRules.get(0).getAllocationTypes());

        Mockito.verify(creditAllocationsValidator, times(1)).validate(creditAllocationRules, ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
        Mockito.verify(creditAllocationsValidator, times(1)).validatePairOfOrderAndCreditAllocationType(createAllocationTypeList());
        Mockito.verifyNoMoreInteractions(creditAllocationsValidator);
    }

    private static List<Pair<Integer, AllocationType>> createAllocationTypeList() {
        AtomicInteger i = new AtomicInteger(1);
        return EnumSet.allOf(AllocationType.class).stream().map(p -> Pair.of(i.getAndIncrement(), p)).toList();
    }

    public Map<String, Object> createCreditAllocationEntry(String transactionType, List<String> orderedRules) {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionType", transactionType);
        List<Map<String, Object>> creditAllocationOrder = new ArrayList<>();
        map.put("creditAllocationOrder", creditAllocationOrder);
        for (int i = 0; i < orderedRules.size(); i++) {
            HashMap<String, Object> entry = new HashMap<>();
            entry.put("creditAllocationRule", orderedRules.get(i));
            entry.put("order", i + 1);
            creditAllocationOrder.add(entry);
        }
        return map;
    }

    @NonNull
    private JsonCommand createJsonCommand(Map<String, Object> jsonMap) throws JacksonException {
        ObjectMapper objectMapper = new JsonMapper();
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
        return JsonCommand.from(json, JsonParser.parseString(json), fromJsonHelper, null, 1L, 2L, 3L, 4L, null, null, null, null, null,
                null, null, null, null);
    }

}
