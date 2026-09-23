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
package org.apache.fineract.portfolio.savings.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsAccountsApiResourceTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private AppUser user;
    @Mock
    private SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    @Mock
    private SqlValidator sqlValidator;
    @Mock
    private ApiRequestParameterHelper apiRequestParameterHelper;
    @Mock
    private DefaultToApiJsonSerializer<SavingsAccountData> toApiJsonSerializer;
    @Mock
    private UriInfo uriInfo;
    @InjectMocks
    private SavingsAccountsApiResource resource;

    @BeforeEach
    void setUp() {
        when(context.authenticatedUser()).thenReturn(user);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "100", "200", "300", "303", "304", "400", "500", "600", "700", "800" })
    void forwardsCanonicalStatusAndExistingParameters(String status) {
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        resource.retrieveAll(uriInfo, "external-reference", 2, 10, "sa.id", "ASC", status);
        ArgumentCaptor<SearchParameters> captor = ArgumentCaptor.forClass(SearchParameters.class);
        verify(savingsAccountReadPlatformService).retrieveAll(captor.capture());
        SearchParameters parameters = captor.getValue();
        assertThat(parameters.getStatus()).isEqualTo(status);
        assertThat(parameters.getExternalId()).isEqualTo("external-reference");
        assertThat(parameters.getOffset()).isEqualTo(2);
        assertThat(parameters.getLimit()).isEqualTo(10);
        assertThat(parameters.getOrderBy()).isEqualTo("sa.id");
        assertThat(parameters.getSortOrder()).isEqualTo("ASC");
        assertThat(parameters.getOfficeId()).isNull();
        verify(user).validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "active", "savingsAccountStatusType.active", "300.0", "2147483648", "0", "-1", "999" })
    void rejectsMalformedOrUnsupportedStatusBeforeQuery(String status) {
        assertThatThrownBy(() -> resource.retrieveAll(uriInfo, null, null, null, null, null, status))
                .isInstanceOfSatisfying(PlatformApiDataValidationException.class, exception -> {
                    assertThat(exception.getErrors()).hasSize(1);
                    assertThat(exception.getErrors().get(0).getParameterName()).isEqualTo("status");
                });
        verifyNoInteractions(savingsAccountReadPlatformService);
    }
}
