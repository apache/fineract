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
package org.apache.fineract.portfolio.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.client.mapper.ClientMapper;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class ClientReadPlatformServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private CodeValueReadPlatformService codeValueReadPlatformService;
    @Mock
    private PaginationHelper paginationHelper;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private ColumnValidator columnValidator;
    @Mock
    private ClientCollateralManagementRepositoryWrapper collateralRepoWrapper;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientReadPlatformServiceImpl clientReadPlatformService;

    @Test
    void testRetrieveOne_Success() {
        // Arrange
        Long clientId = 1L;
        String mockHierarchy = "Root/";
        Client mockClientEntity = mock(Client.class);

        // FIX: Using .lookup() instead of .builder() to match Fineract's ClientData patterns
        ClientData mockClientData = ClientData.lookup(clientId, "Test Client", 1L, "Test Office");

        // Stubbing dependencies
        when(context.officeHierarchy()).thenReturn(mockHierarchy);
        when(clientRepositoryWrapper.getClientByClientIdAndHierarchy(clientId, mockHierarchy + "%")).thenReturn(mockClientEntity);
        when(clientMapper.map(mockClientEntity)).thenReturn(mockClientData);
        when(collateralRepoWrapper.getCollateralsPerClient(clientId)).thenReturn(Collections.emptyList());

        // Mock the groups query to return an empty list
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong())).thenReturn(Collections.emptyList());

        // Act
        ClientData result = clientReadPlatformService.retrieveOne(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getId());
    }

    @Test
    void testRetrieveOne_ClientNotFound_ThrowsException() {
        // Arrange
        Long clientId = 99L;
        when(context.officeHierarchy()).thenReturn("Root/");

        // Simulate database "Not Found" error
        when(clientRepositoryWrapper.getClientByClientIdAndHierarchy(anyLong(), anyString()))
                .thenThrow(new EmptyResultDataAccessException(1));

        // Act & Assert
        assertThrows(ClientNotFoundException.class, () -> {
            clientReadPlatformService.retrieveOne(clientId);
        });
    }

    @Test
    void testRetrieveAll_ThrowsExceptionWhenStatusIsInvalid() {
        // Arrange
        // We create a SearchParameters object with an "INVALID" status
        org.apache.fineract.infrastructure.core.service.SearchParameters searchParameters = org.apache.fineract.infrastructure.core.service.SearchParameters
                .builder().status("INVALID").build();

        // Act & Assert
        // The service should catch the "INVALID" status and throw a validation exception
        assertThrows(org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException.class, () -> {
            clientReadPlatformService.retrieveAll(searchParameters);
        });
    }
}
