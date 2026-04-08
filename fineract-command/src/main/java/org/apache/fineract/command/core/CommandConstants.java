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
package org.apache.fineract.command.core;

public final class CommandConstants {

    private CommandConstants() {}

    public static final String COMMAND_JSON_CLASS_ATTRIBUTE = "@class";
    public static final String COMMAND_HTTP_HEADER_REQUEST_ID = "x-fineract-request-id";
    public static final String COMMAND_HTTP_HEADER_TENANT_ID = "Fineract-Platform-TenantId";
    public static final String COMMAND_HTTP_HEADER_IP = "IP";
    public static final int COMMAND_HOOK_ORDER_HEADERS = 10;
    public static final int COMMAND_HOOK_ORDER_TIMESTAMP = 11;
    public static final int COMMAND_HOOK_ORDER_USERNAME = 12;
}
