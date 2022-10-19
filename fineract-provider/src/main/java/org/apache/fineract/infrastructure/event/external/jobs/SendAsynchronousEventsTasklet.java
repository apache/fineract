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
package org.apache.fineract.infrastructure.event.external.jobs;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.MessageV1;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.external.producer.ExternalEventProducer;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventStatus;
import org.apache.fineract.infrastructure.event.external.service.message.MessageFactory;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendAsynchronousEventsTasklet implements Tasklet {

    private final FineractProperties fineractProperties;
    private final ExternalEventRepository repository;
    private final ExternalEventProducer eventProducer;
    private final MessageFactory messageFactory;
    private final ByteBufferConverter byteBufferConverter;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            if (isDownstreamChannelEnabled()) {
                List<ExternalEvent> events = getQueuedEventsBatch();
                processEvents(events);
            }
        } catch (Exception e) {
            log.error("Error occurred while processing events: ", e);
        }
        return RepeatStatus.FINISHED;
    }

    private boolean isDownstreamChannelEnabled() {
        return fineractProperties.getEvents().getExternal().getProducer().getJms().isEnabled();
    }

    private List<ExternalEvent> getQueuedEventsBatch() {
        int readBatchSize = getBatchSize();
        Pageable batchSize = PageRequest.ofSize(readBatchSize);
        return repository.findByStatusOrderById(ExternalEventStatus.TO_BE_SENT, batchSize);
    }

    private void processEvents(List<ExternalEvent> queuedEvents) throws IOException {
        for (ExternalEvent event : queuedEvents) {
            MessageV1 message = messageFactory.createMessage(event);
            byte[] byteMessage = byteBufferConverter.convert(message.toByteBuffer());
            eventProducer.sendEvent(byteMessage);
            event.setStatus(ExternalEventStatus.SENT);
            event.setSentAt(DateUtils.getOffsetDateTimeOfTenant());
            repository.save(event);
        }
    }

    private int getBatchSize() {
        return fineractProperties.getEvents().getExternal().getProducer().getReadBatchSize();
    }

}
