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
package org.apache.fineract.portfolio.loanproduct.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Set;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixData;
import org.apache.fineract.portfolio.loanproduct.productmix.service.ProductMixReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LoanProductsApiResourceTemplateDatatablesTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private AppUser appUser;
    @Mock
    private LoanProductReadPlatformService loanProductReadPlatformService;
    @Mock
    private DefaultToApiJsonSerializer<LoanProductData> toApiJsonSerializer;
    @Mock
    private ApiRequestParameterHelper apiRequestParameterHelper;
    @Mock
    private EntityDatatableChecksReadService entityDatatableChecksReadService;
    @Mock
    private UriInfo uriInfo;

    private LoanProductsApiResource resource;

    @BeforeEach
    void setUp() {
        resource = new LoanProductsApiResource(context, loanProductReadPlatformService, mock(ChargeReadPlatformService.class),
                mock(CurrencyReadPlatformService.class), mock(FundReadPlatformService.class), toApiJsonSerializer,
                apiRequestParameterHelper, mock(LoanDropdownReadPlatformService.class),
                mock(PortfolioCommandSourceWritePlatformService.class), mock(ProductToGLAccountMappingReadPlatformService.class),
                mock(AccountingDropdownReadPlatformService.class),
                (DefaultToApiJsonSerializer<ProductMixData>) mock(DefaultToApiJsonSerializer.class),
                mock(ProductMixReadPlatformService.class), mock(DropdownReadPlatformService.class), mock(PaymentTypeReadService.class),
                mock(FloatingRatesReadService.class), mock(RateReadService.class), mock(ConfigurationDomainService.class),
                mock(DelinquencyReadPlatformService.class), mock(CodeValueReadPlatformService.class), entityDatatableChecksReadService);

        when(context.authenticatedUser()).thenReturn(appUser);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        when(loanProductReadPlatformService.retrieveNewLoanProductDetails())
                .thenReturn(LoanProductData.sensibleDefaultsForNewLoanProductCreation());
        when(toApiJsonSerializer.serialize(any(), any(LoanProductData.class), any(Set.class))).thenReturn("{}");
    }

    @Test
    void templateCarriesTheLoanProductCreateCheckDatatables() {
        final List<DatatableData> createCheckDatatables = List
                .of(DatatableData.create("m_product_loan", "dt_loan_product_extra", null, List.of()));
        when(entityDatatableChecksReadService.retrieveTemplates(StatusEnum.CREATE.getValue(), EntityTables.LOAN_PRODUCT.getName(), null))
                .thenReturn(createCheckDatatables);

        resource.retrieveTemplate(uriInfo, false);

        final ArgumentCaptor<LoanProductData> data = ArgumentCaptor.forClass(LoanProductData.class);
        final ArgumentCaptor<Set<String>> parameters = ArgumentCaptor.forClass(Set.class);
        verify(toApiJsonSerializer).serialize((ApiRequestJsonSerializationSettings) any(), data.capture(), parameters.capture());
        assertEquals(createCheckDatatables, data.getValue().getDatatables());
        assertTrue(parameters.getValue().contains("datatables"));
    }

    @Test
    void templateHasNoDatatablesWhenNoCreateCheckExists() {
        when(entityDatatableChecksReadService.retrieveTemplates(StatusEnum.CREATE.getValue(), EntityTables.LOAN_PRODUCT.getName(), null))
                .thenReturn(null);

        resource.retrieveTemplate(uriInfo, false);

        final ArgumentCaptor<LoanProductData> data = ArgumentCaptor.forClass(LoanProductData.class);
        verify(toApiJsonSerializer).serialize((ApiRequestJsonSerializationSettings) any(), data.capture(), any(Set.class));
        assertNull(data.getValue().getDatatables());
    }
}
