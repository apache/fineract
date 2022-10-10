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
package com.acme.fineract.portfolio.note.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.note.service.NoteWritePlatformService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty("acme.note.enabled")
public class AcmeNoteWritePlatformService implements NoteWritePlatformService, InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        log.warn("Note Write Service: '{}'", getClass().getCanonicalName());
    }

    @Override
    public CommandProcessingResult createNote(JsonCommand command) {
        return null;
    }

    @Override
    public CommandProcessingResult updateNote(JsonCommand command) {
        return null;
    }

    @Override
    public CommandProcessingResult deleteNote(JsonCommand command) {
        return null;
    }

    @Override
    public void createAndPersistClientNote(Client client, JsonCommand command) {

    }
}
