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
package org.apache.fineract.infrastructure.documentmanagement.handler;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageWritePlatformService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCreateCommandHandler implements CommandHandler<ImageCreateRequest, ImageCreateResponse> {

    private final ImageWritePlatformService writePlatformService;

    @Retry(name = "commandImageCreate", fallbackMethod = "fallback")
    @Transactional
    @Override
    public ImageCreateResponse handle(final Command<ImageCreateRequest> command) {
        return writePlatformService.createImage(command.getPayload());
    }

    @Override
    public ImageCreateResponse fallback(Command<ImageCreateRequest> command, Throwable t) {
        // NOTE: fallback method needs to be in the same class
        return CommandHandler.super.fallback(command, t);
    }
}
