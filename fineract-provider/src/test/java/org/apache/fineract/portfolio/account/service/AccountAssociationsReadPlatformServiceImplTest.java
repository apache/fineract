/**
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
package org.apache.fineract.portfolio.account.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

public class AccountAssociationsReadPlatformServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AccountAssociationsReadPlatformServiceImpl service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRetriveLoanAssociations_HandlesShortFromDatabase() {
        List<Map<String, Object>> mockStatusList = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("type", (short) 1); // Simulates the Short/SmallInt
        mockStatusList.add(row);

        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(mockStatusList);

        assertDoesNotThrow(() -> {
            service.retriveLoanAssociations(1L, 1);
        });
    }

    @Test
    public void testRetriveLoanAssociations_HandlesIntegerFromDatabase() {
        List<Map<String, Object>> mockStatusList = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("type", 1); // Simulates standard Integer
        mockStatusList.add(row);

        lenient().when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(mockStatusList);

        assertDoesNotThrow(() -> {
            service.retriveLoanAssociations(1L, 1);
        });
    }
}
