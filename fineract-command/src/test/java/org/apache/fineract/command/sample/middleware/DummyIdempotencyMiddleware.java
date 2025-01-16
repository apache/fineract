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
package org.apache.fineract.command.sample.middleware;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandMiddleware;
import org.apache.fineract.command.core.exception.CommandIllegalArgumentException;
import org.apache.fineract.command.sample.command.DummyCommand;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DummyIdempotencyMiddleware implements CommandMiddleware {

    // NOTE: in production you would use of course a database or Redis
    private static final List<UUID> IDS = new ArrayList<>();

    @Override
    public void invoke(Command<?> command) {
        if (command instanceof DummyCommand c) {
            if (IDS.contains(c.getId())) {
                throw new CommandIllegalArgumentException(c, "Duplicate request ID: " + c.getId());
            }

            IDS.add(c.getId());
        }
    }
}
