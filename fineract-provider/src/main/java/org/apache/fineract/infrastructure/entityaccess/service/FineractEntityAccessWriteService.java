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
package org.apache.fineract.infrastructure.entityaccess.service;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface FineractEntityAccessWriteService {

    CommandProcessingResult createEntityAccess(JsonCommand command);

    CommandProcessingResult createEntityToEntityMapping(Long relId, JsonCommand command);

    CommandProcessingResult updateEntityToEntityMapping(Long mapId, JsonCommand command);

    CommandProcessingResult deleteEntityToEntityMapping(Long mapId);

    void addNewEntityAccess(String entityType, Long entityId, CodeValue accessType, String secondEntityType, Long secondEntityId);

    /*
     * CommandProcessingResult updateEntityAccess ( Long entityAccessId, JsonCommand command);
     *
     * CommandProcessingResult removeEntityAccess ( String entityType, Long entityId, Long accessType, String
     * secondEntityType, Long secondEntityId);
     */
}
