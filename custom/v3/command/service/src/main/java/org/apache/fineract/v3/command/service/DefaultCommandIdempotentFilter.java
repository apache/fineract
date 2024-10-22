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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.v3.command.data.CommandRequest;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultCommandIdempotentFilter implements CommandFilter {

    // TODO: more sophisticated implementation, possibly with distributed SET/MAP (Redis)
    private static final Set<UUID> REQUEST_IDS = new HashSet<>();

    @Override
    public boolean filter(CommandRequest<?> request) {

        var idempotencyKey = request.getRequestIdempotencyKey();

        boolean pass = !(idempotencyKey != null && REQUEST_IDS.contains(request.getRequestIdempotencyKey()));

        REQUEST_IDS.add(request.getRequestIdempotencyKey());

        return pass;
    }
}
