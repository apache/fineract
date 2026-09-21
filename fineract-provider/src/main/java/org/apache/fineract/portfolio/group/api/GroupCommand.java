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
package org.apache.fineract.portfolio.group.api;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;

public enum GroupCommand {

    ACTIVATE("activate"), //
    ASSOCIATE_CLIENTS("associateClients"), //
    DISASSOCIATE_CLIENTS("disassociateClients"), //
    GENERATE_COLLECTION_SHEET("generateCollectionSheet"), //
    SAVE_COLLECTION_SHEET("saveCollectionSheet"), //
    UNASSIGN_STAFF("unassignStaff"), //
    ASSIGN_STAFF("assignStaff"), //
    ASSIGN_ROLE("assignRole"), //
    UNASSIGN_ROLE("unassignRole"), //
    UPDATE_ROLE("updateRole"), //
    TRANSFER_CLIENTS("transferClients"), //
    CLOSE("close");

    private final String value;

    GroupCommand(final String value) {
        this.value = value;
    }

    public static GroupCommand from(final String commandParam) {
        return Arrays.stream(values()).filter(c -> StringUtils.isNotBlank(commandParam) && c.value.equalsIgnoreCase(commandParam.trim()))
                .findFirst().orElseThrow(() -> new UnrecognizedQueryParamException("command", commandParam,
                        Arrays.stream(values()).map(c -> c.value).toArray(Object[]::new)));
    }
}
