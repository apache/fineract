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
package org.apache.fineract.command.sample.api;

import static org.apache.fineract.command.core.CommandConstants.COMMAND_REQUEST_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.command.sample.command.DummyCommand;
import org.apache.fineract.command.sample.data.DummyRequest;
import org.apache.fineract.command.sample.data.DummyResponse;
import org.apache.fineract.command.sample.service.DefaultDummyTenantService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/test/dummy", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_JSON_VALUE,
        APPLICATION_PROBLEM_JSON_VALUE })
class DummyApiController {
    // "application/vnd.fineract+json;charset=UTF-8;version=1.0"

    private final DefaultDummyTenantService tenantService;

    private final CommandPipeline pipeline;

    @PostMapping("/sync")
    DummyResponse dummySync(@RequestHeader(value = COMMAND_REQUEST_ID, required = false) UUID requestId,
            @RequestHeader(value = "x-fineract-tenant-id", required = false) String tenantId, @RequestBody DummyRequest request) {
        var command = new DummyCommand();
        command.setId(requestId);
        command.setPayload(request);
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));

        tenantService.set(tenantId);

        Supplier<DummyResponse> result = pipeline.send(command);

        return result.get();
    }

    @Async
    @PostMapping("/async")
    CompletableFuture<DummyResponse> dummyAsync(@RequestHeader(value = COMMAND_REQUEST_ID, required = false) UUID requestId,
            @RequestHeader(value = "x-fineract-tenant-id", required = false) String tenantId, @RequestBody DummyRequest request) {
        var command = new DummyCommand();
        command.setId(requestId);
        command.setPayload(request);

        tenantService.set(tenantId);

        Supplier<DummyResponse> result = pipeline.send(command);

        return CompletableFuture.supplyAsync(result);
    }
}
