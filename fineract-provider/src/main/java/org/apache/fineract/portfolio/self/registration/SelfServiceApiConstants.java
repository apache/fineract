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
package org.apache.fineract.portfolio.self.registration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SelfServiceApiConstants {

    public static final String accountNumberParamName = "accountNumber";
    public static final String passwordParamName = "password";
    public static final String firstNameParamName = "firstName";
    public static final String mobileNumberParamName = "mobileNumber";
    public static final String lastNameParamName = "lastName";
    public static final String emailParamName = "email";
    public static final String usernameParamName = "username";
    public static final String authenticationTokenParamName = "authenticationToken";
    public static final String authenticationModeParamName = "authenticationMode";
    public static final String emailModeParamName = "email";
    public static final String mobileModeParamName = "mobile";
    public static final String requestIdParamName = "requestId";
    public static final String createRequestSuccessMessage = "Self service request created.";
    public static final Set<String> REGISTRATION_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(usernameParamName,
            accountNumberParamName, passwordParamName, firstNameParamName, mobileNumberParamName, lastNameParamName, emailParamName,
            authenticationModeParamName));
    public static final Set<String> CREATE_USER_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(requestIdParamName,
            authenticationTokenParamName));
    public static final Object[] SUPPORTED_AUTHENTICATION_MODE_PARAMETERS = new Object[] {emailModeParamName,
            mobileModeParamName};
    public static final String SELF_SERVICE_USER_ROLE = "Self Service User";
    
}
