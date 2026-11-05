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
package org.apache.fineract.portfolio.client.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;

/**
 * Turns a database constraint violation on a client write into a user-facing error.
 */
@Slf4j
final class ClientDataIntegrityIssueHandler {

    private ClientDataIntegrityIssueHandler() {}

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    static void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("external_id")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId",
                    "Client with externalId `" + externalId + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo",
                    "Client with accountNo `" + accountNo + "` already exists", "accountNo", accountNo);
        } else if (realCause.getMessage().contains("mobile_no")) {
            final String mobileNo = command.stringValueOfParameterNamed("mobileNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.mobileNo",
                    "Client with mobileNo `" + mobileNo + "` already exists", "mobileNo", mobileNo);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw ErrorHandler.getMappable(dve, "error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    private static void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("Error occurred.", dve);
    }
}
