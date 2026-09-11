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
package org.apache.fineract.command.hook;

import static org.apache.fineract.command.core.CommandConstants.COMMAND_HOOK_ORDER_HEADERS;
import static org.apache.fineract.command.core.CommandConstants.COMMAND_HTTP_HEADER_IP;
import static org.apache.fineract.command.core.CommandConstants.COMMAND_PROPERTY_HOOKS_SERVLET_HEADERS_ENABLED;
import static org.apache.fineract.command.util.ServletHeaderUtil.getHeader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHookBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(COMMAND_HOOK_ORDER_HEADERS)
@ConditionalOnProperty(value = COMMAND_PROPERTY_HOOKS_SERVLET_HEADERS_ENABLED, havingValue = "true")
final class ServletHeadersCommandHook implements CommandHookBefore<Object, Object> {

    @Override
    public void onBefore(CommandContext<Object, Object> ctx) {
        var command = ctx.getCommand();

        if (StringUtils.isEmpty(command.getIpAddress())) {
            command.setIpAddress(getHeader(COMMAND_HTTP_HEADER_IP, false));
        }
    }
}
