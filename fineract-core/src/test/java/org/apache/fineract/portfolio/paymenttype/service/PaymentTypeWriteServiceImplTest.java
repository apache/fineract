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
package org.apache.fineract.portfolio.paymenttype.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeUpdateRequest;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.paymenttype.exception.PaymentTypeNotFoundException;
import org.apache.fineract.portfolio.paymenttype.mapper.PaymentTypeCreateRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

@ExtendWith(MockitoExtension.class)
class PaymentTypeWriteServiceImplTest {

    private static final Long PAYMENT_TYPE_ID = 7L;

    @Mock
    private PaymentTypeRepository repository;

    @Mock
    private PaymentTypeCreateRequestMapper createRequestMapper;

    private PaymentTypeWriteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentTypeWriteServiceImpl(repository, createRequestMapper);
    }

    @Test
    void updateWithNullIsSystemDefinedLeavesStoredValueUnchanged() {
        final PaymentType paymentType = existingPaymentType(true);
        when(repository.findById(PAYMENT_TYPE_ID)).thenReturn(Optional.of(paymentType));

        service.updatePaymentType(updateRequest(null));

        assertTrue(paymentType.getIsSystemDefined());
        assertEquals("Bank transfer", paymentType.getName());
        verify(repository).saveAndFlush(paymentType);
    }

    @Test
    void updateWithoutIsSystemDefinedLeavesStoredValueUnchanged() {
        final PaymentType paymentType = existingPaymentType(true);
        when(repository.findById(PAYMENT_TYPE_ID)).thenReturn(Optional.of(paymentType));

        service.updatePaymentType(PaymentTypeUpdateRequest.builder().id(PAYMENT_TYPE_ID).name("Bank transfer").build());

        assertTrue(paymentType.getIsSystemDefined());
    }

    @Test
    void updateWithIsSystemDefinedOverwritesStoredValue() {
        final PaymentType paymentType = existingPaymentType(true);
        when(repository.findById(PAYMENT_TYPE_ID)).thenReturn(Optional.of(paymentType));

        service.updatePaymentType(updateRequest(false));

        assertFalse(paymentType.getIsSystemDefined());
    }

    @Test
    void updateDataIntegrityViolationIsReportedAsDataIntegrityIssueNotAsNotFound() {
        when(repository.findById(PAYMENT_TYPE_ID)).thenReturn(Optional.of(existingPaymentType(false)));
        when(repository.saveAndFlush(any(PaymentType.class))).thenThrow(new DataIntegrityViolationException("not-null violation",
                new SQLException("null value in column \"value\" violates not-null constraint")));

        final PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class,
                () -> service.updatePaymentType(updateRequest(false)));

        assertEquals("error.msg.paymenttypes.unknown.data.integrity.issue", exception.getGlobalisationMessageCode());
    }

    @Test
    void updateJpaSystemExceptionIsReportedAsDataIntegrityIssueNotAsNotFound() {
        when(repository.findById(PAYMENT_TYPE_ID)).thenReturn(Optional.of(existingPaymentType(false)));
        when(repository.saveAndFlush(any(PaymentType.class))).thenThrow(new JpaSystemException(new RuntimeException("database error")));

        final PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class,
                () -> service.updatePaymentType(updateRequest(false)));

        assertEquals("error.msg.paymenttypes.unknown.data.integrity.issue", exception.getGlobalisationMessageCode());
    }

    @Test
    void updateOfMissingPaymentTypeIsReportedAsNotFound() {
        when(repository.findById(PAYMENT_TYPE_ID)).thenReturn(Optional.empty());

        assertThrows(PaymentTypeNotFoundException.class, () -> service.updatePaymentType(updateRequest(false)));

        verify(repository, never()).saveAndFlush(any(PaymentType.class));
    }

    private static PaymentTypeUpdateRequest updateRequest(final Boolean isSystemDefined) {
        return PaymentTypeUpdateRequest.builder().id(PAYMENT_TYPE_ID).name("Bank transfer").isSystemDefined(isSystemDefined).build();
    }

    private static PaymentType existingPaymentType(final boolean isSystemDefined) {
        return new PaymentType("Cash", null, true, 1L, null, isSystemDefined);
    }
}
