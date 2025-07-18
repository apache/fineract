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
package org.apache.fineract.organisation.monetary.handler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CurrencyCreateCommandHandlerTest {

    // private List<CurrencyData> currenciesGood;
    //
    // @InjectMocks
    // private CurrencyCreateCommandHandler underTest;
    //
    // @Mock
    // private CurrencyWritePlatformService writePlatformService;

    // @BeforeEach
    // void setUp() throws Exception {
    // ObjectMapper objectMapper = new ObjectMapper();
    // InputStream inputStream =
    // getClass().getClassLoader().getResourceAsStream("test-resources/currenciesGoodData.json");
    // currenciesGood = objectMapper.readValue(inputStream, new TypeReference<List<CurrencyData>>() {});
    // }

    @Disabled
    @Test
    void testHandleHappyPath() {
        // for (CurrencyData element : currenciesGood) {
        // Command<CurrencyData> command = new Command<>();
        // command.setId(UUID.randomUUID());
        // command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        // command.setPayload(element);
        // // Mockito.when(writePlatformService.createCurrency(command.getPayload())).thenReturn(element);
        // // assertEquals(underTest.handle(command), element);
        // }
    }
}
