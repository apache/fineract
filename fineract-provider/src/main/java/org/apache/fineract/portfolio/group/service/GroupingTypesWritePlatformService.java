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
package org.apache.fineract.portfolio.group.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface GroupingTypesWritePlatformService {

    CommandProcessingResult createCenter(JsonCommand command);

    CommandProcessingResult updateCenter(Long entityId, JsonCommand command);

    CommandProcessingResult createGroup(Long centerId, JsonCommand command);

    CommandProcessingResult activateGroupOrCenter(Long entityId, JsonCommand command);

    CommandProcessingResult updateGroup(Long groupId, JsonCommand command);

    CommandProcessingResult deleteGroup(Long groupId);

    CommandProcessingResult closeGroup(final Long groupId, final JsonCommand command);

    CommandProcessingResult closeCenter(final Long centerId, final JsonCommand command);

    CommandProcessingResult unassignGroupOrCenterStaff(Long groupId, JsonCommand command);

    CommandProcessingResult assignGroupOrCenterStaff(Long groupId, JsonCommand command);

    CommandProcessingResult associateClientsToGroup(Long groupId, JsonCommand command);

    CommandProcessingResult disassociateClientsFromGroup(Long groupId, JsonCommand command);

    CommandProcessingResult associateGroupsToCenter(final Long centerId, final JsonCommand command);

    CommandProcessingResult disassociateGroupsToCenter(final Long centerId, final JsonCommand command);
}
