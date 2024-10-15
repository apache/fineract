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

package org.apache.fineract.v3.command.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.v3.command.data.CommandRequest;
import org.apache.fineract.v3.command.data.CommandResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultCommandDispatcher implements CommandDispatcher {

    private final CommandProcessor defaultCommandProcessor;
    private final CommandFilter idempotentFilter;
    private final ApplicationEventPublisher publisher;

    @Override
    public CommandResponse<?> dispatch(CommandRequest<?> request) {

        if (!idempotentFilter.filter(request)) {
            // TODO: throw proper platform exception
            throw new RuntimeException("Duplicate request: " + request.getRequestIdempotencyKey());
        }

        // TODO: eventually add more processors/filters if needed until we match the current implementation
        request.setRequestIdempotencyKey(UUID.randomUUID());

        // TODO: create event object and do not send the entire request
        publisher.publishEvent(request);

        return defaultCommandProcessor.process(request);
    }
}
