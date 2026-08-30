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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.workingcapitalloan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanTransactionDataV1;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanAdjustTransactionBusinessEvent.Data;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanRepaymentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.workingcapitalloan.WorkingCapitalLoanTransactionDataMapper;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanAdjustTransactionBusinessEventSerializerTest {

    private static final Long LOAN_ID = 11L;

    @Mock
    private WorkingCapitalLoanTransactionDataMapper mapper;

    @InjectMocks
    private WorkingCapitalLoanAdjustTransactionBusinessEventSerializer serializer;

    @Test
    void canSerializeAdjustTransactionEvent() {
        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = new WorkingCapitalLoanAdjustTransactionBusinessEvent(
                Data.reversal(transaction(1L)), LOAN_ID);

        assertThat(serializer.canSerialize(event)).isTrue();
    }

    @Test
    void cannotSerializeOtherWorkingCapitalTransactionEvent() {
        final WorkingCapitalLoanRepaymentTransactionBusinessEvent event = new WorkingCapitalLoanRepaymentTransactionBusinessEvent(
                mock(WorkingCapitalLoanTransaction.class), LOAN_ID);

        assertThat(serializer.canSerialize(event)).isFalse();
    }

    @Test
    void reversalLeavesNewTransactionDetailNullWithoutMappingIt() {
        final WorkingCapitalLoanTransactionData reversedTransaction = transaction(1L);
        final WorkingCapitalLoanTransactionDataV1 mappedTransaction = avroTransaction(1L);
        when(mapper.map(reversedTransaction)).thenReturn(mappedTransaction);

        final WorkingCapitalLoanTransactionAdjustmentDataV1 result = (WorkingCapitalLoanTransactionAdjustmentDataV1) serializer
                .toAvroDTO(new WorkingCapitalLoanAdjustTransactionBusinessEvent(Data.reversal(reversedTransaction), LOAN_ID));

        assertThat(result.getTransactionToAdjust()).isSameAs(mappedTransaction);
        assertThat(result.getNewTransactionDetail()).isNull();
        verify(mapper).map(reversedTransaction);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void adjustmentMapsPreviousStateToTransactionToAdjustAndCurrentStateToNewTransactionDetail() {
        final WorkingCapitalLoanTransactionData previousState = transaction(1L);
        final WorkingCapitalLoanTransactionData currentState = transaction(2L);
        final WorkingCapitalLoanTransactionDataV1 mappedPreviousState = avroTransaction(1L);
        final WorkingCapitalLoanTransactionDataV1 mappedCurrentState = avroTransaction(2L);
        when(mapper.map(previousState)).thenReturn(mappedPreviousState);
        when(mapper.map(currentState)).thenReturn(mappedCurrentState);

        final WorkingCapitalLoanTransactionAdjustmentDataV1 result = (WorkingCapitalLoanTransactionAdjustmentDataV1) serializer
                .toAvroDTO(new WorkingCapitalLoanAdjustTransactionBusinessEvent(new Data(previousState, currentState), LOAN_ID));

        assertThat(result.getTransactionToAdjust()).isSameAs(mappedPreviousState);
        assertThat(result.getNewTransactionDetail()).isSameAs(mappedCurrentState);
    }

    @Test
    void supportedSchemaIsTheAdjustmentRecord() {
        assertThat(serializer.getSupportedSchema()).isEqualTo(WorkingCapitalLoanTransactionAdjustmentDataV1.class);
    }

    private WorkingCapitalLoanTransactionData transaction(final Long id) {
        return WorkingCapitalLoanTransactionData.builder().id(id).wcLoanId(LOAN_ID).build();
    }

    private WorkingCapitalLoanTransactionDataV1 avroTransaction(final Long id) {
        return WorkingCapitalLoanTransactionDataV1.newBuilder().setId(id).setWcLoanId(LOAN_ID).build();
    }
}
