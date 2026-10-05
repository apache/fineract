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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.NoteCreateRequest;
import org.apache.fineract.client.models.NoteCreateResponse;
import org.apache.fineract.client.models.NoteData;
import org.apache.fineract.client.models.NoteDeleteResponse;
import org.apache.fineract.client.models.NoteUpdateRequest;
import org.apache.fineract.client.models.NoteUpdateResponse;

/**
 * Notes on any resource type the notes API supports, such as {@code clients}, {@code loans} or
 * {@code loanTransactions}.
 */
public class FeignNoteHelper {

    private final FineractFeignClient fineractClient;

    public FeignNoteHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public NoteCreateResponse addNote(String resourceType, Long resourceId, String note) {
        return ok(() -> fineractClient.notes().addNewNote(resourceType, resourceId, new NoteCreateRequest().note(note)));
    }

    public NoteData getNote(String resourceType, Long resourceId, Long noteId) {
        return ok(() -> fineractClient.notes().retrieveNote(resourceType, resourceId, noteId));
    }

    public CallFailedRuntimeException getNoteExpectingError(String resourceType, Long resourceId, Long noteId) {
        return fail(() -> fineractClient.notes().retrieveNote(resourceType, resourceId, noteId));
    }

    public NoteUpdateResponse updateNote(String resourceType, Long resourceId, Long noteId, String note) {
        return ok(() -> fineractClient.notes().updateNote(resourceType, resourceId, noteId, new NoteUpdateRequest().note(note)));
    }

    public NoteDeleteResponse deleteNote(String resourceType, Long resourceId, Long noteId) {
        return ok(() -> fineractClient.notes().deleteNote(resourceType, resourceId, noteId));
    }
}
