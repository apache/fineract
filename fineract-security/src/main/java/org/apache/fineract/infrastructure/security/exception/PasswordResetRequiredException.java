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
package org.apache.fineract.infrastructure.security.exception;

import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when a user must reset their password before proceeding.
 *
 * This exception is thrown during authentication when the user's credentials are valid but they are required to change
 * their password (e.g., on first login or after an admin reset). It carries the authenticated user data so the client
 * receives enough information to proceed with the password reset flow.
 */
public class PasswordResetRequiredException extends AuthenticationException {

    private final AuthenticatedUserData authenticatedUserData;

    public PasswordResetRequiredException(AuthenticatedUserData authenticatedUserData) {
        super("Password reset required");
        this.authenticatedUserData = authenticatedUserData;
    }

    public AuthenticatedUserData getAuthenticatedUserData() {
        return authenticatedUserData;
    }
}
