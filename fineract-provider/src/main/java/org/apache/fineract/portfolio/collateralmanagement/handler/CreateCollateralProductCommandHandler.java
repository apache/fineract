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
package org.apache.fineract.portfolio.collateralmanagement.handler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductCreateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductResponse;
import org.apache.fineract.portfolio.collateralmanagement.service.CollateralManagementWritePlatformService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateCollateralProductCommandHandler
        implements CommandHandler<CollateralManagementProductCreateRequest, CollateralManagementProductResponse> {

    private final CollateralManagementWritePlatformService collateralManagementWritePlatformService;

    @Transactional
    @Override
    public CollateralManagementProductResponse handle(Command<CollateralManagementProductCreateRequest> command) {
        return this.collateralManagementWritePlatformService.createCollateral(command.getPayload());
    }
}
