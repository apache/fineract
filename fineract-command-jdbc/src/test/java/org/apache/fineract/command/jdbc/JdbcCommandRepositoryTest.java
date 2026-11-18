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
package org.apache.fineract.command.jdbc;

import static org.apache.fineract.command.core.CommandState.ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.apache.fineract.command.jdbc.store.mapping.JdbcCommandMapper;
import org.apache.fineract.command.test.CommandBaseTest;
import org.apache.fineract.command.test.sample.command.DummyCommand;
import org.apache.fineract.command.test.sample.data.DummyRequest;
import org.apache.fineract.command.test.sample.data.DummyResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
class JdbcCommandRepositoryTest extends CommandBaseTest {

    @Autowired
    protected JdbcCommandRepository commandRepository;

    @Autowired
    protected JdbcCommandMapper commandMapper;

    @Test
    void save() {
        var content = "hello";
        var command = new DummyCommand();
        var now = Instant.now();

        command.setPayload(DummyRequest.builder().content(content).build());
        command.setIdempotencyKey("1234567890");
        command.setIpAddress("127.0.0.1");
        command.setCreatedAt(now);
        command.setExecutedAt(now);
        command.setApprovedAt(now);
        command.setRejectedAt(now);
        command.setInitiatedByUsername("abc");
        command.setExecutedByUsername("abc");
        command.setApprovedByUsername("abc");
        command.setRejectedByUsername("abc");

        var response = DummyResponse.builder().content(content).build();

        var commandEntity = commandMapper.map(command, response);
        commandEntity.setState(ERROR);
        commandEntity.setError("Some error message");

        var result = commandRepository.save(commandEntity);

        log.info("Saved command: {}", result);

        var found = commandRepository.findById(result.getId());

        log.info("Found command: {}", found.orElse(null));
    }

    @Test
    void theCommandRecordsWhatItActedOnAndWhatItDid() {
        var command = new DummyCommand();

        command.setPayload(DummyRequest.builder().content("hello").build());
        command.setCreatedAt(Instant.now());

        var saved = commandRepository.save(commandMapper.map(command));
        var stored = commandRepository.findById(saved.getId()).orElseThrow();

        // the names an audit search and a checker inbox look a command up by, and from which the approval permission
        // is derived; a row without them is invisible to both
        assertEquals("DUMMY", stored.getEntityName(), "The entity the command acted on must be recorded.");
        assertEquals("CREATE", stored.getActionName(), "The action it performed must be recorded.");

        var readBack = commandMapper.map(stored);

        assertEquals("DUMMY", readBack.entityName(), "A command read back must carry the names it was recorded with.");
        assertEquals("CREATE", readBack.actionName());
    }
}
