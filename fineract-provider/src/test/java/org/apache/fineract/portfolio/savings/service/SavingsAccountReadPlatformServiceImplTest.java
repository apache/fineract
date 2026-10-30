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
package org.apache.fineract.portfolio.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

class SavingsAccountReadPlatformServiceImplTest {

    private static final Pattern PRODUCT_MAPPING_JOIN = Pattern.compile("acc_product_mapping (\\w+) on (.+?)(?= left join | join |$)");

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    @SuppressWarnings("unchecked")
    void retrieveAllSavingsDataForInterestPosting_onlyJoinsSavingsProductMappings() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 31))));
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), any(Object[].class))).thenReturn(List.of());

        new SavingsAccountReadPlatformServiceImpl(null, jdbcTemplate, null, null, null, null, null, null)
                .retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L);

        final ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(), any(ResultSetExtractor.class), any(Object[].class));

        // acc_product_mapping holds loan mappings too, and loan and savings product ids overlap
        final List<String> aliases = new ArrayList<>();
        final Matcher join = PRODUCT_MAPPING_JOIN.matcher(sql.getValue());
        while (join.find()) {
            aliases.add(join.group(1));
            assertThat(join.group(2)).as("acc_product_mapping join %s", join.group(1)).contains(join.group(1) + ".product_type = 2");
        }
        assertThat(aliases).contains("apm", "apm1", "apm2", "apm3", "apm4", "apm5");
    }
}
