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

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty("acme.note.enabled")
public class AcmeNoteReadPlatformService implements NoteReadPlatformService, InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        log.warn("Note Read Service: '{}'", getClass().getCanonicalName());
    }

    @Override
    public NoteData retrieveNote(Long noteId, Long resourceId, Integer noteTypeId) {
        return null;
    }

    @Override
    public Collection<NoteData> retrieveNotesByResource(Long resourceId, Integer noteTypeId) {
        return null;
    }
}
