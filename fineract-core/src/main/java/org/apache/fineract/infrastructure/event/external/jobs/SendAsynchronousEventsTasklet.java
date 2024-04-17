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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.apache.fineract.infrastructure.core.diagnostics.performance.MeasuringUtil.measure;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.MessageV1;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.external.producer.ExternalEventProducer;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventStatus;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventView;
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
    private final ConfigurationDomainService configurationDomainService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            if (isDownstreamChannelEnabled()) {
                List<ExternalEventView> events = getQueuedEventsBatch();
                log.debug("Queued events size: {}", events.size());
                sendEvents(events);
            }
        } catch (Exception e) {
            log.error("Error occurred while processing events: ", e);
        }
        return RepeatStatus.FINISHED;
    }

    private boolean isDownstreamChannelEnabled() {
        return fineractProperties.getEvents().getExternal().getProducer().getJms().isEnabled()
                || fineractProperties.getEvents().getExternal().getProducer().getKafka().isEnabled();
    }

    private List<ExternalEventView> getQueuedEventsBatch() {
        int readBatchSize = getBatchSize();
        Pageable batchSize = PageRequest.ofSize(readBatchSize);
        return measure(() -> repository.findByStatusOrderById(ExternalEventStatus.TO_BE_SENT, batchSize),
                (events, timeTaken) -> log.debug("Loaded {} events in {}ms", events.size(), timeTaken.toMillis()));
    }

    private void sendEvents(List<ExternalEventView> queuedEvents) {
        Map<Long, List<byte[]>> partitions = generatePartitions(queuedEvents);
        List<Long> eventIds = queuedEvents.stream().map(ExternalEventView::getId).toList();
        sendEventsToProducer(partitions);
        markEventsAsSent(eventIds);
    }

    private void sendEventsToProducer(Map<Long, List<byte[]>> partitions) {
        eventProducer.sendEvents(partitions);
    }

    private void markEventsAsSent(List<Long> eventIds) {
        OffsetDateTime sentAt = DateUtils.getAuditOffsetDateTime();

        // Partitioning dataset to avoid exception: PreparedStatement can have at most 65,535 parameters
        final int partitionSize = fineractProperties.getEvents().getExternal().getPartitionSize();
        List<List<Long>> partitions = Lists.partition(eventIds, partitionSize);
        partitions //
                .forEach(partitionedEventIds -> {
                    measure(() -> {
                        repository.markEventsSent(partitionedEventIds, sentAt);
                    }, timeTaken -> {
                        log.debug("Took {}ms to update {} events", timeTaken.toMillis(), partitionedEventIds.size());
                    });
                });
    }

    private Map<Long, List<byte[]>> generatePartitions(List<ExternalEventView> queuedEvents) {
        Map<Long, List<ExternalEventView>> initialPartitions = queuedEvents.stream().collect(groupingBy(externalEvent -> {
            Long aggregateRootId = externalEvent.getAggregateRootId();
            if (aggregateRootId == null) {
                aggregateRootId = -1L;
            }
            return aggregateRootId;
        }));
        Map<Long, List<byte[]>> partitions = measure(
                () -> initialPartitions.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> createMessages(e.getValue()))),
                timeTaken -> {
                    log.debug("Took {}ms to create message partitions", timeTaken.toMillis());
                });
        return partitions;
    }

    private List<byte[]> createMessages(List<ExternalEventView> events) {
        try {
            List<byte[]> messages = new ArrayList<>();
            for (ExternalEventView event : events) {
                MessageV1 message = messageFactory.createMessage(event);
                ByteBuffer toByteBuffer = message.toByteBuffer();
                byte[] convert = byteBufferConverter.convert(toByteBuffer);
                messages.add(convert);
                log.trace("Created message to send with id: [{}], type: [{}], idempotency key: [{}]", message.getId(), message.getType(),
                        message.getIdempotencyKey());
            }
            return messages;
        } catch (IOException e) {
            throw new RuntimeException("Error while serializing the message", e);
        }
    }

    private int getBatchSize() {
        Long externalEventBatchSize = configurationDomainService.retrieveExternalEventBatchSize();
        return externalEventBatchSize.intValue();
    }

}
