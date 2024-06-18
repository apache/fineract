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

package org.apache.fineract.v3.note.handler;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.v3.command.service.CommandHandler;
import org.apache.fineract.v3.note.data.NoteCreateRequest;
import org.apache.fineract.v3.note.data.NoteCreateResponse;
import org.apache.fineract.v3.note.service.NoteWritePlatformService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateNoteHandler implements CommandHandler<NoteCreateRequest, NoteCreateResponse> {

    private final NoteWritePlatformService writePlatformService;

    @Override
    public Class<NoteCreateRequest> getRequestType() {
        return NoteCreateRequest.class;
    }

    @Transactional
    @Override
    public NoteCreateResponse handle(NoteCreateRequest request) {
        return writePlatformService.createNote(request);
    }
}
