/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.commands.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class KafkaProducer {

    private static final String COMMAND_ATTRIBUTE = "\"command\":{";
    private final static Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaProducer(final KafkaTemplate<String, String> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public <T> void sendMessage(String topic, String key, T message) {

        try {
            String payload = objectMapper.writeValueAsString(message);

            // This is because not all objects "CommandNotification" has the command attribute with nested objects. In those cases we don't need to unwrap the object "command"
            if (!payload.contains(COMMAND_ATTRIBUTE)) {
                payload = this.objectMapper.writeValueAsString(message);
            }

            logger.info("About to send message model via kafka topic: [{}] payload: [{}]", topic, payload);

            ListenableFuture<SendResult<String, String>> future = this.kafkaTemplate.send(topic, key, payload);

            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

                @Override
                public void onSuccess(SendResult<String, String> result) {

                    logger.info("Message sent to kafka successfully. Message type: {} ; Topic: [{}] ; Offset: [{}]",
                            result.getProducerRecord().value(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                }
                @Override
                public void onFailure(Throwable ex) {

                    logger.error("Unable to send message to kafka. Message type: {} ; due to: {}",
                            message.toString(),
                            ex.getMessage());
                }
            });

        } catch (JsonProcessingException ex) {
            logger.error("Error occurred during deserialization message: {}, stackTrace: ", message,  ex);
            throw new RuntimeException("Error during deserialization", ex);
        }

    }
}
