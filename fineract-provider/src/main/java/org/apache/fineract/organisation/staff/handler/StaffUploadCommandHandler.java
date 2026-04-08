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

package org.apache.fineract.organisation.staff.handler;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.organisation.staff.data.StaffUploadRequest;
import org.apache.fineract.organisation.staff.data.StaffUploadResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StaffUploadCommandHandler implements CommandHandler<StaffUploadRequest, StaffUploadResponse> {

    private final BulkImportWorkbookService bulkImportWorkbookService;

    @Retry(name = "commandStaffUpload", fallbackMethod = "fallback")
    @Override
    @Transactional
    public StaffUploadResponse handle(Command<StaffUploadRequest> command) {
        var payload = command.getPayload();

        var id = bulkImportWorkbookService.importWorkbook(GlobalEntityType.STAFF.toString(), payload.getUploadedInputStream(),
                payload.getFileDetail(), payload.getLocale(), payload.getDateFormat());

        return StaffUploadResponse.builder().resourceId(id).build();
    }

    @Override
    public StaffUploadResponse fallback(Command<StaffUploadRequest> command, Throwable t) {
        // NOTE: fallback method needs to be in the same class
        return CommandHandler.super.fallback(command, t);
    }
}
