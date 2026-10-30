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
package org.apache.fineract.cob.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * One item's processing and writing commit or roll back on their own, so a failing item cannot doom the chunk.
 */
class ItemTransactionProcessorTest {

    private final RecordingTransactionManager transactionManager = new RecordingTransactionManager();
    @SuppressWarnings("unchecked")
    private final AbstractItemProcessor<Item> delegate = mock(AbstractItemProcessor.class);
    @SuppressWarnings("unchecked")
    private final ItemWriter<Item> writer = mock(ItemWriter.class);
    private final Item readItem = new Item(7L);
    private final Item loadedItem = new Item(7L);
    private ItemTransactionProcessor<Item> processor;

    @BeforeEach
    void setUp() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        processor = new ItemTransactionProcessor<>(delegate, writer, id -> loadedItem, template);
    }

    @Test
    void processesTheItemLoadedInItsOwnTransactionAndWritesIt() throws Exception {
        when(delegate.process(loadedItem)).thenReturn(loadedItem);

        assertThat(processor.process(readItem)).isSameAs(loadedItem);

        verify(delegate).process(loadedItem);
        verify(writer).write(Chunk.of(loadedItem));
        assertThat(transactionManager.events).containsExactly("begin", "commit");
    }

    @Test
    void rollsBackOnlyItsOwnTransactionAndRethrowsTheCauseWhenProcessingFails() throws Exception {
        Exception failure = new Exception("business step failed");
        when(delegate.process(loadedItem)).thenThrow(failure);

        Exception thrown = assertThrows(Exception.class, () -> processor.process(readItem));

        assertThat(thrown).isSameAs(failure);
        verify(writer, never()).write(any());
        assertThat(transactionManager.events).containsExactly("begin", "rollback");
    }

    @Test
    void rollsBackWhenWritingFails() throws Exception {
        when(delegate.process(loadedItem)).thenReturn(loadedItem);
        IllegalStateException failure = new IllegalStateException("write failed");
        doThrow(failure).when(writer).write(any());

        assertThat(assertThrows(IllegalStateException.class, () -> processor.process(readItem))).isSameAs(failure);
        assertThat(transactionManager.events).containsExactly("begin", "rollback");
    }

    @Test
    void passesTheStepExecutionToTheDelegate() {
        StepExecution stepExecution = mock(StepExecution.class);

        processor.beforeStep(stepExecution);
        processor.afterStep(stepExecution);

        verify(delegate).beforeStep(stepExecution);
        verify(delegate).afterStep(stepExecution);
    }

    private static final class Item extends AbstractPersistableCustom<Long> {

        private final Long itemId;

        private Item(Long itemId) {
            this.itemId = itemId;
        }

        @Override
        public Long getId() {
            return itemId;
        }
    }

    private static final class RecordingTransactionManager extends AbstractPlatformTransactionManager {

        private final List<String> events = new ArrayList<>();

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            events.add("begin");
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            events.add("commit");
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            events.add("rollback");
        }
    }
}
