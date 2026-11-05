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
package org.apache.fineract.infrastructure.security.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.exceptionmapper.DatabaseUnavailableSupport;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Spring Security turns every failure of the user lookup into an authentication failure, so a database outage during
 * the lookup would be answered with 401 (the credentials are wrong). This answers 503 when the cause is a database
 * outage and otherwise defers to the wrapped entry point.
 */
@RequiredArgsConstructor
public class DatabaseUnavailableAwareAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate;

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException)
            throws IOException, ServletException {
        final Throwable unavailable = DatabaseUnavailableSupport.findDatabaseUnavailable(authException);
        if (unavailable == null || response.isCommitted()) {
            delegate.commence(request, response, authException);
            return;
        }
        DatabaseUnavailableSupport.writeServiceUnavailable(response, unavailable);
    }
}
