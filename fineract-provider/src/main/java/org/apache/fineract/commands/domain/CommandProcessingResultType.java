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
package org.apache.fineract.commands.domain;

public enum CommandProcessingResultType {

    INVALID(0, "commandProcessingResultType.invalid"), //
    PROCESSED(1, "commandProcessingResultType.processed"), //
    AWAITING_APPROVAL(2, "commandProcessingResultType.awaiting.approval"), //
    REJECTED(3, "commandProcessingResultType.rejected");

    private final Integer value;
    private final String code;

    private CommandProcessingResultType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static CommandProcessingResultType fromInt(final Integer typeValue) {
        CommandProcessingResultType type = CommandProcessingResultType.INVALID;
        switch (typeValue) {
            case 1:
                type = PROCESSED;
            break;
            case 2:
                type = AWAITING_APPROVAL;
            break;
            case 3:
                type = REJECTED;
            break;
        }
        return type;
    }
}