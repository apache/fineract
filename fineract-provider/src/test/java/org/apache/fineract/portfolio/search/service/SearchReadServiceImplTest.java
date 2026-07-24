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
package org.apache.fineract.portfolio.search.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.search.data.AdHocQuerySearchRequest;
import org.apache.fineract.portfolio.search.data.AdHocSearchQueryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@ExtendWith(MockitoExtension.class)
class SearchReadServiceImplTest {

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Mock
    private LoanProductReadPlatformService loanProductReadPlatformService;
    @Mock
    private OfficeReadPlatformService officeReadPlatformService;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;

    private SearchReadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SearchReadServiceImpl(namedParameterJdbcTemplate, loanProductReadPlatformService, officeReadPlatformService,
                sqlGenerator);
    }

    @Test
    void retrieveAdHocQueryMatchingDataUsesWhitelistedOutStandingAmountPercentageCondition() {
        final AdHocQuerySearchRequest request = AdHocQuerySearchRequest.builder().includeOutStandingAmountPercentage(true)
                .outStandingAmountPercentageCondition("<=").outStandingAmountPercentage(BigDecimal.valueOf(80)).build();
        doReturn(List.<AdHocSearchQueryData>of()).when(namedParameterJdbcTemplate).query(anyString(), any(MapSqlParameterSource.class),
                any(RowMapper.class));

        service.retrieveAdHocQueryMatchingData(request);

        final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(namedParameterJdbcTemplate).query(sqlCaptor.capture(), any(MapSqlParameterSource.class), any(RowMapper.class));
        assertTrue(sqlCaptor.getValue().contains(" a.percentOut <= :outStandingAmountPercentage "));
    }

    @Test
    void retrieveAdHocQueryMatchingDataRejectsInjectedOutStandingAmountPercentageCondition() {
        final AdHocQuerySearchRequest request = AdHocQuerySearchRequest.builder().includeOutStandingAmountPercentage(true)
                .outStandingAmountPercentageCondition("<= 0 union select").outStandingAmountPercentage(BigDecimal.valueOf(80)).build();

        assertThrows(PlatformApiDataValidationException.class, () -> service.retrieveAdHocQueryMatchingData(request));
        verify(namedParameterJdbcTemplate, never()).query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @Test
    void retrieveAdHocQueryMatchingDataRejectsInjectedOutstandingAmountCondition() {
        final AdHocQuerySearchRequest request = AdHocQuerySearchRequest.builder().includeOutstandingAmount(true)
                .outstandingAmountCondition(">= 0 or 1 = 1").outstandingAmount(BigDecimal.valueOf(100)).build();

        assertThrows(PlatformApiDataValidationException.class, () -> service.retrieveAdHocQueryMatchingData(request));
        verify(namedParameterJdbcTemplate, never()).query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
    }
}
