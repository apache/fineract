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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanPaymentAllocationMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanAssemblerAccountNumberTest {

    private static final Long CLIENT_ID = 1L;
    private static final Long PRODUCT_ID = 2L;

    @Spy
    private FromJsonHelper fromApiJsonHelper;

    @Mock
    private WorkingCapitalLoanProductRepository loanProductRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ExternalIdFactory externalIdFactory;

    @Mock
    private WorkingCapitalLoanRepository workingCapitalLoanRepository;

    @Mock
    private WorkingCapitalLoanPaymentAllocationMapper workingCapitalLoanPaymentAllocationMapper;

    @Mock
    private Client client;

    @Mock
    private WorkingCapitalLoanProduct product;

    @Mock
    private WorkingCapitalLoanProductRelatedDetail productDetail;

    @InjectMocks
    private WorkingCapitalLoanAssemblerImpl assembler;

    @BeforeEach
    void stubClientAndProduct() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(loanProductRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(product.getRelatedDetail()).thenReturn(productDetail);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void assembleFrom_missingOrBlankAccountNo_leavesAccountNumberNullForGeneration(final String accountNo) {
        final WorkingCapitalLoan loan = assembler.assembleFrom(createCommand(accountNo));

        assertNull(loan.getAccountNumber());
    }

    @Test
    void assembleFrom_providedAccountNo_keepsIt() {
        final WorkingCapitalLoan loan = assembler.assembleFrom(createCommand("WC-001"));

        assertEquals("WC-001", loan.getAccountNumber());
    }

    private static JsonCommand createCommand(final String accountNo) {
        final JsonObject json = new JsonObject();
        json.addProperty(WorkingCapitalLoanConstants.clientIdParameterName, CLIENT_ID);
        json.addProperty(WorkingCapitalLoanConstants.productIdParameterName, PRODUCT_ID);
        json.addProperty(WorkingCapitalLoanConstants.submittedOnDateParameterName, "01 January 2026");
        json.addProperty(WorkingCapitalLoanConstants.dateFormatParameterName, "dd MMMM yyyy");
        json.addProperty(WorkingCapitalLoanConstants.localeParameterName, "en");
        Optional.ofNullable(accountNo).ifPresent(value -> json.addProperty(WorkingCapitalLoanConstants.accountNoParameterName, value));
        return JsonCommand.fromJsonElement(1L, JsonParser.parseString(json.toString()), new FromJsonHelper());
    }
}
