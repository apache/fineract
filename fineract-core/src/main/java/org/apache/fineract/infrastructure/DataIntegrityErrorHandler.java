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
package org.apache.fineract.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataIntegrityErrorHandler {

    public void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve, final String msgType,
            final String msgDescription) {
        if (realCause.getMessage().contains("external_id")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg." + msgType + ".duplicate.externalId",
                    msgDescription + ": externalId `" + externalId + "` already exists", "externalId", externalId);
        }
        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg." + msgType + ".unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + msgDescription);
    }
}
