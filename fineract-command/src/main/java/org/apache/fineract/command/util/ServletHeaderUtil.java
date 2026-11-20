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
package org.apache.fineract.command.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ServletHeaderUtil {

    private ServletHeaderUtil() {}

    /**
     * Reads a header of the request being served on the <strong>current thread</strong>. Returns {@code null} off that
     * thread, because {@link RequestContextHolder} is not inheritable - so anything calling this from an asynchronous
     * dispatcher silently gets nothing.
     */
    public static String getHeader(String name, boolean searchParameter) {
        var attributes = RequestContextHolder.getRequestAttributes();

        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            var value = servletAttributes.getRequest().getHeader(name.toLowerCase());

            if (searchParameter && StringUtils.isEmpty(value)) {
                value = servletAttributes.getRequest().getParameter(name.toLowerCase());
            }

            return value;
        }

        return null;
    }

    /**
     * Sets a header on the response being written on the <strong>current thread</strong>, and reports whether it could.
     * Like {@link #getHeader(String, boolean)} this does nothing off that thread, and nothing once the response has
     * been committed.
     */
    public static boolean setResponseHeader(String name, String value) {
        var attributes = RequestContextHolder.getRequestAttributes();

        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            var response = servletAttributes.getResponse();

            if (response != null && !response.isCommitted()) {
                response.setHeader(name, value);

                return true;
            }
        }

        return false;
    }
}
