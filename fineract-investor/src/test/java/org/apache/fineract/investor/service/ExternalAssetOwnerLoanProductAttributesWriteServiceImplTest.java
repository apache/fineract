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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanProductAttributeRequestParameters;
import org.apache.fineract.investor.data.attribute.ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributes;
import org.apache.fineract.investor.domain.ExternalAssetOwnerLoanProductAttributesRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeAlreadyExistsException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributeNotFoundException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerLoanProductAttributesException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.annotation.CacheEvict;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

        testContext.externalAssetOwnerLoanProductAttributesWriteService.updateExternalAssetOwnerLoanProductAttribute(command,
                testContext.attributeKey, testContext.attributeValue);

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).findById(command.entityId());
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

        testContext.externalAssetOwnerLoanProductAttributesWriteService.updateExternalAssetOwnerLoanProductAttribute(command,
                testContext.attributeKey, testContext.attributeValue);

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).findById(command.entityId());
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

        ExternalAssetOwnerLoanProductAttributeNotFoundException thrownException = assertThrows(
                ExternalAssetOwnerLoanProductAttributeNotFoundException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService.updateExternalAssetOwnerLoanProductAttribute(command,
                        testContext.attributeKey, testContext.attributeValue));

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).findById(1L);
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

        ExternalAssetOwnerLoanProductAttributesException thrownException = assertThrows(
                ExternalAssetOwnerLoanProductAttributesException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService.updateExternalAssetOwnerLoanProductAttribute(command,
                        testContext.attributeKey, testContext.attributeValue));

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).findById(command.entityId());
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

        PlatformApiDataValidationException thrownException = assertThrows(PlatformApiDataValidationException.class,
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

        LoanProductNotFoundException thrownException = assertThrows(LoanProductNotFoundException.class,
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

        ExternalAssetOwnerLoanProductAttributeAlreadyExistsException thrownException = assertThrows(
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
        ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException thrownException = assertThrows(
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
        ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException thrownException = assertThrows(
                ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .createExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).saveAndFlush(any());
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).existsByLoanProductIdAndKey(any(), any());
        verify(testContext.loanProductRepository, times(0)).existsById(testContext.loanProductId);
        Assertions.assertEquals(thrownException.getMessage(), "The given attribute key or attribute value is not valid.");
    }

    @Test
    public void testCreateExcludedTransactionTypesAttributeNormalizesTheStoredValue() {
        TestContext testContext = new TestContext(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY,
                "buy_down_fee, BUY_DOWN_FEE_ADJUSTMENT");
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
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).saveAndFlush(loanProductAttributeArgumentCaptor.capture());
        ExternalAssetOwnerLoanProductAttributes savedAttribute = loanProductAttributeArgumentCaptor.getValue();
        Assertions.assertEquals(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY,
                savedAttribute.getAttributeKey());
        Assertions.assertEquals("BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT", savedAttribute.getAttributeValue());
    }

    @Test
    public void testUpdateExcludedTransactionTypesAttributeReplacesTheValue() {
        TestContext testContext = new TestContext(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY,
                "BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT,BUY_DOWN_FEE_AMORTIZATION,BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT");
        ArgumentCaptor<ExternalAssetOwnerLoanProductAttributes> loanProductAttributeArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanProductAttributes.class);

        ExternalAssetOwnerLoanProductAttributes attributeInDB = new ExternalAssetOwnerLoanProductAttributes();
        attributeInDB.setLoanProductId(testContext.loanProductId);
        attributeInDB.setAttributeKey(testContext.attributeKey);
        attributeInDB.setAttributeValue("BUY_DOWN_FEE");
        attributeInDB.setId(1L);

        // given
        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, attributeInDB.getId());
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.findById(command.entityId()))
                .thenReturn(Optional.of(attributeInDB));

        // when
        testContext.externalAssetOwnerLoanProductAttributesWriteService.updateExternalAssetOwnerLoanProductAttribute(command,
                testContext.attributeKey, testContext.attributeValue);

        // then
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).saveAndFlush(loanProductAttributeArgumentCaptor.capture());
        Assertions.assertEquals(testContext.attributeValue, loanProductAttributeArgumentCaptor.getValue().getAttributeValue());
    }

    @ParameterizedTest
    @ValueSource(strings = { "BUY_DOWN_FEE,NOT_A_TYPE", ",", "BUY_DOWN_FEE,", "BUY_DOWN_FEE,BUY_DOWN_FEE" })
    public void testCreateExcludedTransactionTypesAttributeWithInvalidValuePersistsNothing(String attributeValue) {
        TestContext testContext = new TestContext(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY,
                attributeValue);

        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);

        assertThrows(ExternalAssetOwnerLoanProductAttributeInvalidSettlementAttributeException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .createExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).saveAndFlush(any());
        verify(testContext.loanProductRepository, times(0)).existsById(testContext.loanProductId);
    }

    @Test
    public void testCreateExcludedTransactionTypesAttributeExceedingTheMaximumLengthIsRejected() {
        String tooLongValue = "A".repeat(2001);
        TestContext testContext = new TestContext(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY,
                tooLongValue);

        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);

        assertThrows(PlatformApiDataValidationException.class, () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                .createExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0)).saveAndFlush(any());
    }

    @Test
    public void testCreateExcludedTransactionTypesAttributeAcceptsTheFullListOfTransactionTypes() {
        String allTypes = String.join(",", new ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute().getAttributeValues());
        TestContext testContext = new TestContext(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY, allTypes);
        ArgumentCaptor<ExternalAssetOwnerLoanProductAttributes> loanProductAttributeArgumentCaptor = ArgumentCaptor
                .forClass(ExternalAssetOwnerLoanProductAttributes.class);

        final JsonCommand command = createJsonCommand(testContext.jsonCommandString, testContext.loanProductId, null);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.existsByLoanProductIdAndKey(testContext.loanProductId,
                testContext.attributeKey)).thenReturn(false);
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);

        testContext.externalAssetOwnerLoanProductAttributesWriteService.createExternalAssetOwnerLoanProductAttribute(command);

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).saveAndFlush(loanProductAttributeArgumentCaptor.capture());
        Assertions.assertEquals(allTypes, loanProductAttributeArgumentCaptor.getValue().getAttributeValue());
    }

    @Test
    public void testDeleteExternalAssetOwnerLoanProductAttributeHappyPath() {
        TestContext testContext = new TestContext(ExcludedTransactionTypesExternalAssetOwnerLoanProductAttribute.ATTRIBUTE_KEY,
                "BUY_DOWN_FEE");

        ExternalAssetOwnerLoanProductAttributes attributeInDB = new ExternalAssetOwnerLoanProductAttributes();
        attributeInDB.setLoanProductId(testContext.loanProductId);
        attributeInDB.setAttributeKey(testContext.attributeKey);
        attributeInDB.setAttributeValue(testContext.attributeValue);
        attributeInDB.setId(1L);

        // given
        final JsonCommand command = createJsonCommand(null, testContext.loanProductId, attributeInDB.getId());
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.findById(command.entityId()))
                .thenReturn(Optional.of(attributeInDB));

        // when
        CommandProcessingResult result = testContext.externalAssetOwnerLoanProductAttributesWriteService
                .deleteExternalAssetOwnerLoanProductAttribute(command);

        // then
        verify(testContext.loanProductRepository).existsById(testContext.loanProductId);
        verify(testContext.externalAssetOwnerLoanProductAttributesRepository).delete(attributeInDB);
        Assertions.assertEquals(testContext.loanProductId, result.getResourceId());
    }

    @Test
    public void testDeleteExternalAssetOwnerLoanProductAttributeOnAttributeThatDoesNotExist() {
        TestContext testContext = new TestContext();

        final JsonCommand command = createJsonCommand(null, testContext.loanProductId, 1L);
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.findById(command.entityId())).thenReturn(Optional.empty());

        assertThrows(ExternalAssetOwnerLoanProductAttributeNotFoundException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .deleteExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0))
                .delete(any(ExternalAssetOwnerLoanProductAttributes.class));
    }

    @Test
    public void testDeleteExternalAssetOwnerLoanProductAttributeOnUnknownLoanProduct() {
        TestContext testContext = new TestContext();

        final JsonCommand command = createJsonCommand(null, testContext.loanProductId, 1L);
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(false);

        assertThrows(LoanProductNotFoundException.class, () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                .deleteExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0))
                .delete(any(ExternalAssetOwnerLoanProductAttributes.class));
    }

    @Test
    public void testDeleteExternalAssetOwnerLoanProductAttributeBelongingToAnotherLoanProduct() {
        TestContext testContext = new TestContext();

        ExternalAssetOwnerLoanProductAttributes attributeInDB = new ExternalAssetOwnerLoanProductAttributes();
        attributeInDB.setLoanProductId(testContext.loanProductId + 1);
        attributeInDB.setAttributeKey(testContext.attributeKey);
        attributeInDB.setAttributeValue(testContext.attributeValue);
        attributeInDB.setId(1L);

        final JsonCommand command = createJsonCommand(null, testContext.loanProductId, attributeInDB.getId());
        when(testContext.loanProductRepository.existsById(testContext.loanProductId)).thenReturn(true);
        when(testContext.externalAssetOwnerLoanProductAttributesRepository.findById(command.entityId()))
                .thenReturn(Optional.of(attributeInDB));

        ExternalAssetOwnerLoanProductAttributesException thrownException = assertThrows(
                ExternalAssetOwnerLoanProductAttributesException.class,
                () -> testContext.externalAssetOwnerLoanProductAttributesWriteService
                        .deleteExternalAssetOwnerLoanProductAttribute(command));

        verify(testContext.externalAssetOwnerLoanProductAttributesRepository, times(0))
                .delete(any(ExternalAssetOwnerLoanProductAttributes.class));
        Assertions.assertEquals("The requested attribute does not belong to the loanProductId: " + testContext.loanProductId + ".",
                thrownException.getMessage());
    }

    /**
     * Creating an attribute must evict the loan product attribute cache. The attribute key is only present in the
     * request body, so the per-key cache key that update uses cannot be built here and the whole cache is evicted
     * instead. Without this, a cached "attribute not configured" result would survive the create and the new
     * configuration would silently not take effect.
     */
    @Test
    public void testCreateEvictsTheLoanProductAttributeCache() throws NoSuchMethodException {
        CacheEvict cacheEvict = ExternalAssetOwnerLoanProductAttributesWriteServiceImpl.class
                .getMethod("createExternalAssetOwnerLoanProductAttribute", JsonCommand.class).getAnnotation(CacheEvict.class);

        Assertions.assertNotNull(cacheEvict, "createExternalAssetOwnerLoanProductAttribute must be annotated with @CacheEvict");
        Assertions.assertTrue(cacheEvict.allEntries(), "the whole attribute cache must be evicted on create");
        Assertions.assertArrayEquals(new String[] { "externalAssetOwnerLoanProductAttributes" }, cacheEvict.cacheNames());
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

        private ExternalAssetOwnerLoanProductAttributesWriteServiceImpl externalAssetOwnerLoanProductAttributesWriteService;

        private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
        private final Long loanProductId = ThreadLocalRandom.current().nextLong(10, 100);
        @Setter
        private String attributeKey;
        @Setter
        private String attributeValue;

        private String jsonCommandString;

        TestContext() {
            this("SETTLEMENT_MODEL", "DELAYED_SETTLEMENT");
        }

        TestContext(final String attributeKey, final String attributeValue) {
            this.attributeKey = attributeKey;
            this.attributeValue = attributeValue;
            this.jsonCommandString = String.format("""
                    {
                        "attributeKey": "%s",
                        "attributeValue": "%s"
                    }
                    """, attributeKey, attributeValue);
            MockitoAnnotations.openMocks(this);
            this.externalAssetOwnerLoanProductAttributesWriteService = new ExternalAssetOwnerLoanProductAttributesWriteServiceImpl(
                    fromApiJsonHelper, externalAssetOwnerLoanProductAttributesRepository, loanProductRepository,
                    new ExternalAssetOwnerLoanProductAttributeProvider());
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
