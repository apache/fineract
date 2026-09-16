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
package org.apache.fineract.command.idempotency;

import static org.apache.fineract.command.core.CommandConstants.COMMAND_PROPERTIES_PREFIX;

public final class IdempotencyCommandConstants {

    private IdempotencyCommandConstants() {}

    public static final String COMMAND_IDEMPOTENCY_PROPERTIES_PREFIX = COMMAND_PROPERTIES_PREFIX + ".idempotency";
    public static final String COMMAND_IDEMPOTENCY_PROPERTY_ENABLED = COMMAND_IDEMPOTENCY_PROPERTIES_PREFIX + ".enabled";
    public static final String COMMAND_IDEMPOTENCY_PROPERTY_HOOK_KEY_HEADER_PRE = COMMAND_IDEMPOTENCY_PROPERTIES_PREFIX
            + ".hook-key-header-pre";
    public static final String COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE = COMMAND_IDEMPOTENCY_PROPERTIES_PREFIX + ".hook-check-pre";
    public static final String COMMAND_IDEMPOTENCY_PROPERTY_KEY_HEADER_NAME = COMMAND_IDEMPOTENCY_PROPERTIES_PREFIX + ".key-header-name";
    public static final String COMMAND_IDEMPOTENCY_HOOK_BASE_PACKAGE = "org.apache.fineract.command.idempotency.hook";
    public static final int COMMAND_IDEMPOTENCY_HOOK_HEADERS_BEFORE = 20;
    public static final int COMMAND_IDEMPOTENCY_HOOK_CHECK_BEFORE = 21;
}
