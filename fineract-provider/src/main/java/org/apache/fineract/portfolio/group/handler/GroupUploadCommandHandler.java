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
package org.apache.fineract.portfolio.group.handler;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.portfolio.group.data.GroupUploadRequest;
import org.apache.fineract.portfolio.group.data.GroupUploadResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupUploadCommandHandler implements CommandHandler<GroupUploadRequest, GroupUploadResponse> {

    private final BulkImportWorkbookService bulkImportWorkbookService;

    @Retry(name = "commandGroupUpload", fallbackMethod = "fallback")
    @Override
    public GroupUploadResponse handle(Command<GroupUploadRequest> command) {
        final var payload = command.getPayload();
        final Long id = bulkImportWorkbookService.importWorkbook(GlobalEntityType.GROUPS.toString(), payload.getUploadedInputStream(),
                payload.getFileDetail(), payload.getLocale(), payload.getDateFormat());
        return GroupUploadResponse.builder().resourceId(id).build();
    }

    @Override
    public GroupUploadResponse fallback(Command<GroupUploadRequest> command, Throwable t) {
        return CommandHandler.super.fallback(command, t);
    }
}
