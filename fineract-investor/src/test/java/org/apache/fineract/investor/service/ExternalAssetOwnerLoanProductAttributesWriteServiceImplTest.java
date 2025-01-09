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
package org.apache.fineract.investor.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanProductAttributeRequestParameters;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributesRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeAlreadyExistsException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeNotFoundException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributesException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExternalAssetOwnerLoanProductAttributesWriteServiceImplTest {

    @Test
    public void testCreateExternalAssetOwnerLoanProductAttributeHappyPath() {
        TestContext testContext = new TestContext();
        ArgumentCaptor<ExternalAssetOwnerLoanProductAttributes> loanProductAttributeArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanProductAttributes.class);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.existsByLoanProductIdAndKey(testContext.loanProductId,
                testContext.attributeKey)).thenReturn(false);
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);

        // when
        testContext.externalAssetOwnerLoanProductAttributesWriteService.createExternalAssetOwnerLoanProductAttribute(command);

        // then
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).existsByLoanProductIdAndKey(any(), any());
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).saveAndFlush(loanProductAttributeArgumentCaptor.capture());
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        assertLoanProductAttributeValues(testContext, loanProductAttributeArgumentCaptor.getValue());
    }

    @Test
    public void testUpdateExternalAssetOwnerLoanProductAttributeHappyPath() {
        TestContext testContext = new TestContext();
        ArgumentCaptor<ExternalAssetOwnerLoanProductAttributes> loanProductAttributeArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanProductAttributes.class);

        ExternalAssetOwnerLoanProductAttributes attributeInDB = new ExternalAssetOwnerLoanProductAttributes();
        attributeInDB.setLoanProductId(testContext.loanProductId);
        attributeInDB.setAttributeKey(testContext.attributeKey);
        attributeInDB.setAttributeValue("DIFFERENT_VALUE");
        attributeInDB.setId(1L);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, attributeInDB.getId());
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.findById(command.entityId()))
                .thenReturn(Optional.of(attributeInDB));

        testContext.externalAssetOwnerLoanProductAttributesWriteService.updateExternalAssetOwnerLoanProductAttribute(command);

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).findById(eq(command.entityId()));
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).saveAndFlush(loanProductAttributeArgumentCaptor.capture());
    }

    @Test
    public void testUpdateExternalAssetOwnerLoanProductAttributeUpdateNotRequired() {
        TestContext testContext = new TestContext();
        ArgumentCaptor<ExternalAssetOwnerLoanProductAttributes> loanProductAttributeArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanProductAttributes.class);

        ExternalAssetOwnerLoanProductAttributes attributeInDB = new ExternalAssetOwnerLoanProductAttributes();
        attributeInDB.setLoanProductId(testContext.loanProductId);
        attributeInDB.setAttributeKey(testContext.attributeKey);
        attributeInDB.setAttributeValue(testContext.attributeValue);
        attributeInDB.setId(1L);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, attributeInDB.getId());
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.findById(command.entityId()))
                .thenReturn(Optional.of(attributeInDB));

        testContext.externalAssetOwnerLoanProductAttributesWriteService.updateExternalAssetOwnerLoanProductAttribute(command);

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).findById(eq(command.entityId()));
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0))
                .saveAndFlush(loanProductAttributeArgumentCaptor.capture());
    }

    @Test
    public void testUpdateExternalAssetOwnerLoanProductAttributeOnAttributeThatDoesNotExist() {
        TestContext testContext = new TestContext();
        ArgumentCaptor<ExternalAssetOwnerLoanProductAttributes> loanProductAttributeArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanProductAttributes.class);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, 1L);
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.findById(1L)).thenReturn(Optional.empty());

        ExternalAssetOwnerLoanProductAttributeNotFoundException thrownException = Assert.assertThrows(
                ExternalAssetOwnerLoanProductAttributeNotFoundException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .updateExternalAssetOwnerLoanProductAttribute(command));

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).findById(eq(1L));
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0))
                .saveAndFlush(loanProductAttributeArgumentCaptor.capture());
        Assertions.assertEquals(thrownException.getMessage(), "Loan product attribute with id " + 1L + " was not found");
    }

    @Test
    public void testUpdateExternalAssetOwnerLoanProductAttributeOnAttributeWithDifferentKeyValue() {
        TestContext testContext = new TestContext();
        ArgumentCaptor<ExternalAssetOwnerLoanProductAttributes> loanProductAttributeArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanProductAttributes.class);

        ExternalAssetOwnerLoanProductAttributes attributeInDB = new ExternalAssetOwnerLoanProductAttributes();
        attributeInDB.setLoanProductId(testContext.loanProductId);
        attributeInDB.setAttributeKey("DIFFERENT_KEY");
        attributeInDB.setAttributeValue(testContext.attributeValue);
        attributeInDB.setId(1L);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, attributeInDB.getId());
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.findById(command.entityId()))
                .thenReturn(Optional.of(attributeInDB));

        ExternalAssetOwnerLoanProductAttributesException thrownException = Assert.assertThrows(
                ExternalAssetOwnerLoanProductAttributesException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .updateExternalAssetOwnerLoanProductAttribute(command));

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).findById(eq(command.entityId()));
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0))
                .saveAndFlush(loanProductAttributeArgumentCaptor.capture());
        Assertions.assertEquals(thrownException.getMessage(),
                "The attribute key of requested update attribute does not match the attribute key from database.");
    }

    @Test
    public void testCreateExternalAssetOwnerLoanProductAttributeUsingDefaultSettlementValue() {
        TestContext testContext = new TestContext();
        ArgumentCaptor<ExternalAssetOwnerLoanProductAttributes> loanProductAttributeArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanProductAttributes.class);

        // given
        final JsonElement jsonCommandElement = testContext.fromJsonHelper.parse(testContext.jsonCommandString);
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.existsByLoanProductIdAndKey(testContext.loanProductId,
                testContext.attributeKey)).thenReturn(false);
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE,
                jsonCommandElement)).thenReturn("DEFAULT_SETTLEMENT");
        testContext.setAttributeValue("DEFAULT_SETTLEMENT");

        // when
        testContext.externalAssetOwnerLoanProductAttributesWriteService.createExternalAssetOwnerLoanProductAttribute(command);

        // then
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).existsByLoanProductIdAndKey(any(), any());
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).saveAndFlush(loanProductAttributeArgumentCaptor.capture());
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        assertLoanProductAttributeValues(testContext, loanProductAttributeArgumentCaptor.getValue());
    }

    @ParameterizedTest
    @MethodSource("externalAssetOwnerLoanProductAttributeApiRequestDataValidationErrors")
    public void testExternalAssetOwnerLoanProductAttributeRequestWithApiDataValidationErrors(String testName, String attributeKey,
            String attributeValue, String expectedErrorString) {
        TestContext testContext = new TestContext();
        final JsonElement jsonCommandElement = testContext.fromJsonHelper.parse(testContext.jsonCommandString);
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);
        when(testContext.fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
                jsonCommandElement)).thenReturn(attributeKey);
        when(testContext.fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE,
                jsonCommandElement)).thenReturn(attributeValue);

        PlatformApiDataValidationException thrownException = Assert.assertThrows(PlatformApiDataValidationException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .createExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).saveAndFlush(any());
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).existsByLoanProductIdAndKey(any(), any());
        verify(testContext.loanProductRepository, times(0)).existsById(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(), expectedErrorString);
    }

    @Test
    public void testCreateLoanProductAttributeExternalAssetOwnerExternalAssetOwnerLoanProductNotFound() {
        TestContext testContext = new TestContext();
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);

        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(false);

        LoanProductNotFoundException thrownException = Assert.assertThrows(LoanProductNotFoundException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .createExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).saveAndFlush(any());
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).existsByLoanProductIdAndKey(any(), any());
        verify(testContext.loanProductRepository, times(1)).existsById(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(),
                "Loan product with identifier " + testContext.loanProductId + " does not exist");
    }

    @Test
    public void testCreateLoanProductAttributeExternalAssetOwnerExternalAssetOwnerLoanProductAlreadyHasAttribute() {
        TestContext testContext = new TestContext();
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);

        when(testContext.externalAssetOwnerLoanProductAttributesRepository.existsByLoanProductIdAndKey(testContext.loanProductId,
                testContext.attributeKey)).thenReturn(true);
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);

        ExternalAssetOwnerLoanProductAttributeAlreadyExistsException thrownException = Assert.assertThrows(
                ExternalAssetOwnerLoanProductAttributeAlreadyExistsException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .createExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).saveAndFlush(any());
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(1)).existsByLoanProductIdAndKey(any(), any());
        verify(testContext.loanProductRepository, times(1)).existsById(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(), "attributeKey already exists for the loanProductId: "
                + testContext.loanProductId + ". Use PUT call to UPDATE the attribute.");
    }

    @Test
    public void testExternalAssetOwnerLoanProductAttributeInvalidKey() {
        TestContext testContext = new TestContext();

        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);

        final JsonElement jsonCommandElement = testContext.fromJsonHelper.parse(testContext.jsonCommandString);
        when(testContext.fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
                jsonCommandElement)).thenReturn("BAD_KEY");
        ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException thrownException = Assert.assertThrows(
                ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .createExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).saveAndFlush(any());
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).existsByLoanProductIdAndKey(any(), any());
        verify(testContext.loanProductRepository, times(0)).existsById(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(), "The given attribute key or attribute value is not valid.");
    }

    @Test
    public void testExternalAssetOwnerLoanProductAttributeInvalidValue() {
        TestContext testContext = new TestContext();

        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);

        final JsonElement jsonCommandElement = testContext.fromJsonHelper.parse(testContext.jsonCommandString);
        when(testContext.fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE,
                jsonCommandElement)).thenReturn("BAD_VALUE");
        ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException thrownException = Assert.assertThrows(
                ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .createExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).saveAndFlush(any());
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).existsByLoanProductIdAndKey(any(), any());
        verify(testContext.loanProductRepository, times(0)).existsById(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(), "The given attribute key or attribute value is not valid.");
    }

    private static Stream<Arguments> externalAssetOwnerLoanProductAttributeApiRequestDataValidationErrors() {

        return Stream.of(Arguments.of("blankAttributeValue", "SETTLEMENT_MODEL", "", "Validation errors exist."),
                Arguments.of("blankAttributeKey", "", "DELAYED_SETTLEMENT", "Validation errors exist."));
    }

    private void assertLoanProductAttributeValues(final TestContext testContext,
            final ExternalAssetOwnerLoanProductAttributes loanProductAttribute) {
        Assertions.assertEquals(testContext.loanProductId, loanProductAttribute.getLoanProductId());
        Assertions.assertEquals(testContext.attributeKey, loanProductAttribute.getAttributeKey());
        Assertions.assertEquals(testContext.attributeValue, loanProductAttribute.getAttributeValue());
    }

    /**
     * Helper method to create {@link JsonCommand} object from json command string.
     *
     * @param jsonCommand
     *            the json command string
     * @param loanProductId
     *            the loanProductId
     * @return the {@link JsonCommand} object.
     */
    private JsonCommand createJsonCommand(final String jsonCommand, final Long loanProductId, final Long resourceId) {
        return new JsonCommand(null, jsonCommand, null, null, null, resourceId, null, null, null, null, null, null, null, loanProductId,
                null, null, null, null);
    }

    @SuppressFBWarnings({ "VA_FORMAT_STRING_USES_NEWLINE" })
    static class TestContext {

        @Mock
        private FromJsonHelper fromApiJsonHelper;

        @Mock
        private ExternalAssetOwnerLoanProductAttributesRepository externalAssetOwnerLoanProductAttributesRepository;

        @Mock
        private LoanProductRepository loanProductRepository;

        @InjectMocks
        private ExternalAssetOwnerLoanProductAttributesWriteServiceImpl externalAssetOwnerLoanProductAttributesWriteService;

        private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
        private final Long loanProductId = Long.valueOf(RandomStringUtils.randomNumeric(2));
        @Setter
        private String attributeKey = "SETTLEMENT_MODEL";
        @Setter
        private String attributeValue = "DELAYED_SETTLEMENT";

        private String jsonCommandString = String.format("""
                {
                    "attributeKey": "%s",
                    "attributeValue": "%s"
                }
                """, attributeKey, attributeValue);

        @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
        TestContext() {
            MockitoAnnotations.openMocks(this);
            stubFromApiJsonHelper();
        }

        private void stubFromApiJsonHelper() {
            final JsonElement jsonCommandElement = fromJsonHelper.parse(jsonCommandString);
            when(fromApiJsonHelper.parse(anyString())).thenReturn(jsonCommandElement);
            when(fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
                    jsonCommandElement)).thenReturn(attributeKey);
            when(fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE,
                    jsonCommandElement)).thenReturn(attributeValue);
        }
    }
}
