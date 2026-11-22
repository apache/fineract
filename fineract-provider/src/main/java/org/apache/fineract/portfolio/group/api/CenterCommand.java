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
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;

/**
 * The commands served by {@code POST /centers/{centerId}?command=...}, each with the task permission it needs.
 * <p>
 * This enum is the single list of those commands: the resource switches over it and {@code ApiAuthorizationRules}
 * builds the per-command authorization rule from it, so a command added here is authorized by its own permission
 * without a separate matcher.
 */
public enum CenterCommand {

    ACTIVATE("activate", "ACTIVATE_CENTER"), GENERATE_COLLECTION_SHEET("generateCollectionSheet", null), SAVE_COLLECTION_SHEET(
            "saveCollectionSheet", "SAVECOLLECTIONSHEET_CENTER"), CLOSE("close", "CLOSE_CENTER"), ASSOCIATE_GROUPS("associateGroups",
                    "ASSOCIATEGROUPS_CENTER"), DISASSOCIATE_GROUPS("disassociateGroups", "DISASSOCIATEGROUPS_CENTER");

    private final String value;
    private final String permission;

    CenterCommand(final String value, final String permission) {
        this.value = value;
        this.permission = permission;
    }

    /** The value of the {@code command} query parameter. */
    public String getValue() {
        return value;
    }

    /**
     * The task permission the command needs, or {@code null} for a command that only reads and so needs no more than
     * the rule every other API request has.
     */
    public String getPermission() {
        return permission;
    }

    public static CenterCommand from(final String commandParam) {
        return Arrays.stream(values()).filter(command -> CommandParameterUtil.is(commandParam, command.value)).findFirst()
                .orElseThrow(() -> new UnrecognizedQueryParamException("command", commandParam,
                        Arrays.stream(values()).map(CenterCommand::getValue).toArray(Object[]::new)));
    }
}
