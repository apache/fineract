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
package org.apache.fineract.organisation.teller.handler;

import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.organisation.teller.service.TellerWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles a delete teller command.
 *
 * @author Markus Geiss
 * @see org.apache.fineract.organisation.teller.service.TellerWritePlatformService
 * @since 2.0.0
 */
@Service
@CommandType(entity = "TELLER", action = "UPDATECASHIERALLOCATION")
public class UpdateCashierAllocationCommandHandler implements NewCommandSourceHandler {

    private final TellerWritePlatformService writePlatformService;

    /**
     * Creates a new instance
     *
     * @param writePlatformService the {@code TellerWritePlatformService} used to access the backend
     */
    @Autowired
    public UpdateCashierAllocationCommandHandler(final TellerWritePlatformService writePlatformService) {
        super();
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.writePlatformService.updateCashierAllocation(command.entityId(), 
        		command.subentityId(), command);
    }
}
