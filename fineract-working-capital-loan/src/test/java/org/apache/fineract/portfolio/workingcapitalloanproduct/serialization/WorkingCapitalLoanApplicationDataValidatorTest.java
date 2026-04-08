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
package org.apache.fineract.portfolio.workingcapitalloanproduct.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.loanaccount.domain.ExpectedDisbursementDateValidator;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanApplicationDataValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanApplicationDataValidatorTest {

    private static final Long CLIENT_ID = 1L;
    private static final Long PRODUCT_ID = 1L;

    @Mock
    private WorkingCapitalLoanProductRepository productRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private WorkingCapitalLoanRepository workingCapitalLoanRepository;
    @Mock
    private ExpectedDisbursementDateValidator expectedDisbursementDateValidator;
    @Mock
    private WorkingCapitalPaymentAllocationDataValidator paymentAllocationDataValidator;

    private WorkingCapitalLoanApplicationDataValidator validator;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));

        final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
        validator = new WorkingCapitalLoanApplicationDataValidator(fromApiJsonHelper, paymentAllocationDataValidator, productRepository,
                clientRepository, workingCapitalLoanRepository, expectedDisbursementDateValidator);

        final Client client = createMockClient();
        final Office office = org.mockito.Mockito.mock(Office.class);
        when(office.getId()).thenReturn(1L);
        lenient().when(client.getOffice()).thenReturn(office);
        lenient().when(client.isNotActive()).thenReturn(false);
        lenient().when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

        final WorkingCapitalLoanProduct product = createMockProduct();
        lenient().when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        final GlobalConfigurationProperty officeSpecificProp = new GlobalConfigurationProperty();
        officeSpecificProp.setEnabled(false);

        lenient().when(workingCapitalLoanRepository.existsByAccountNumber(any())).thenReturn(false);
        lenient().when(workingCapitalLoanRepository.existsByExternalId(any())).thenReturn(false);
        lenient().doNothing().when(expectedDisbursementDateValidator).validate(any(), anyLong());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testValidateForCreate_WithValidData_ShouldNotThrowException() {
        final JsonCommand command = jsonCommand(createValidJson());
        assertDoesNotThrow(() -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithEmptyJson_ShouldThrowException() {
        final JsonCommand command = jsonCommand("");
        assertThrows(InvalidJsonException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithNullJson_ShouldThrowException() {
        final JsonCommand command = jsonCommand(null);
        assertThrows(InvalidJsonException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithMissingClientId_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithoutField(WorkingCapitalLoanConstants.clientIdParameterName));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithMissingProductId_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithoutField(WorkingCapitalLoanConstants.productIdParameterName));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithMissingPrincipal_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithoutField(WorkingCapitalLoanConstants.principalAmountParamName));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithMissingPeriodPaymentRate_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithoutField(WorkingCapitalLoanProductConstants.periodPaymentRateParamName));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithMissingTotalPayment_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithoutField(WorkingCapitalLoanConstants.totalPaymentParamName));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithMissingExpectedDisbursementDate_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithoutField(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithNegativePrincipal_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithField(WorkingCapitalLoanConstants.principalAmountParamName, -100));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithZeroPrincipal_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithField(WorkingCapitalLoanConstants.principalAmountParamName, 0));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithNegativePeriodPaymentRate_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithField(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, -1.0));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithNegativeDiscount_ShouldThrowException() {
        final JsonCommand command = jsonCommand(createJsonWithField(WorkingCapitalLoanProductConstants.discountParamName, -0.1));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithUnsupportedParameter_ShouldThrowException() {
        final JsonObject json = createBaseJsonObject();
        json.addProperty("unsupportedField", "value");
        final JsonCommand command = jsonCommand(json.toString());
        assertThrows(UnsupportedParameterException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForCreate_WithSubmittedOnNoteExceedingLength_ShouldThrowException() {
        final JsonCommand command = jsonCommand(
                createJsonWithField(WorkingCapitalLoanConstants.submittedOnNoteParameterName, "x".repeat(501)));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
    }

    @Test
    void testValidateForUpdate_WithEmptyJson_ShouldThrowException() {
        final JsonCommand command = jsonCommand("");
        assertThrows(InvalidJsonException.class, () -> validator.validateForUpdate(command));
    }

    @Test
    void testValidateForUpdate_WithNoParameters_ShouldThrowException() {
        final JsonObject json = new JsonObject();
        json.addProperty(WorkingCapitalLoanConstants.localeParameterName, "en");
        json.addProperty(WorkingCapitalLoanConstants.dateFormatParameterName, "yyyy-MM-dd");
        final JsonCommand command = jsonCommand(json.toString());
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForUpdate(command));
    }

    @Test
    void testHandleDataIntegrityIssues_WithDuplicateAccountNo_ShouldThrowPlatformDataIntegrityException() {
        final JsonCommand command = jsonCommand(createJsonWithField(WorkingCapitalLoanConstants.accountNoParameterName, "ACC-001"));
        final Throwable cause = new RuntimeException("wc_loan_account_no_UNIQUE");
        assertThrows(org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException.class,
                () -> validator.handleDataIntegrityIssues(command, cause, new RuntimeException()));
    }

    @Test
    void testHandleDataIntegrityIssues_WithDuplicateExternalId_ShouldThrowPlatformDataIntegrityException() {
        final JsonCommand command = jsonCommand(createJsonWithField(WorkingCapitalLoanConstants.externalIdParameterName, "ext-1"));
        final Throwable cause = new RuntimeException("wc_loan_externalid_UNIQUE");
        assertThrows(org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException.class,
                () -> validator.handleDataIntegrityIssues(command, cause, new RuntimeException()));
    }

    private JsonCommand jsonCommand(final String json) {
        final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
        final String safeJson = json != null ? json : "";
        return JsonCommand.from(safeJson, fromApiJsonHelper.parse("{}".equals(safeJson) || safeJson.isEmpty() ? "{}" : safeJson),
                fromApiJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private JsonObject createBaseJsonObject() {
        final LocalDate expectedDate = LocalDate.now(ZoneId.systemDefault()).plusDays(7);
        final JsonObject json = new JsonObject();
        json.addProperty(WorkingCapitalLoanConstants.localeParameterName, "en");
        json.addProperty(WorkingCapitalLoanConstants.dateFormatParameterName, "yyyy-MM-dd");
        json.addProperty(WorkingCapitalLoanConstants.clientIdParameterName, CLIENT_ID);
        json.addProperty(WorkingCapitalLoanConstants.productIdParameterName, PRODUCT_ID);
        json.addProperty(WorkingCapitalLoanConstants.principalAmountParamName, 5000);
        json.addProperty(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, 1.0);
        json.addProperty(WorkingCapitalLoanConstants.totalPaymentParamName, 5500);
        json.addProperty(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, expectedDate.toString());
        return json;
    }

    private String createValidJson() {
        return createBaseJsonObject().toString();
    }

    private String createJsonWithoutField(final String fieldName) {
        final JsonObject json = createBaseJsonObject();
        json.remove(fieldName);
        return json.toString();
    }

    private String createJsonWithField(final String fieldName, final Object value) {
        final JsonObject json = createBaseJsonObject();
        if (value instanceof Number) {
            json.addProperty(fieldName, (Number) value);
        } else if (value instanceof String) {
            json.addProperty(fieldName, (String) value);
        }
        return json.toString();
    }

    private Client createMockClient() {
        final Client client = org.mockito.Mockito.mock(Client.class);
        when(client.getId()).thenReturn(CLIENT_ID);
        return client;
    }

    private WorkingCapitalLoanProduct createMockProduct() {
        final WorkingCapitalLoanProduct product = org.mockito.Mockito.mock(WorkingCapitalLoanProduct.class);
        final org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductMinMaxConstraints minMax = new org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductMinMaxConstraints(
                BigDecimal.valueOf(1000), BigDecimal.valueOf(10000), BigDecimal.valueOf(0.5), BigDecimal.valueOf(2.0));
        when(product.getId()).thenReturn(PRODUCT_ID);
        when(product.getMinMaxConstraints()).thenReturn(minMax);
        when(product.getConfigurableAttributes()).thenReturn(null);
        when(product.getStartDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()).minusDays(30));
        when(product.getCloseDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()).plusYears(1));
        return product;
    }
}
