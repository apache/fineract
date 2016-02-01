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

import java.util.ArrayList;

import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * A {@link RuntimeException} that is thrown in the case where a user does not
 * have sufficient authorization to execute operation on platform.
 */
public class ResetPasswordException extends PlatformApiDataValidationException {

    public ResetPasswordException(final Long userId) {

        super("error.msg.password.outdated", "The password of the user with id " + userId + " has expired, please reset it",
                new ArrayList<ApiParameterError>() {

                    {
                        add(ApiParameterError.parameterError("error.msg.password.outdated", "The password of the user with id " + userId
                                + " has expired, please reset it", "userId", userId));

                    }
                }

        );

    }

}