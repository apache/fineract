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
package org.apache.fineract.infrastructure.core.jersey.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import java.util.Map;
import org.apache.fineract.infrastructure.security.data.AuthenticatedOauthUserData;
import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.data.PasswordValidationPolicyData;
import org.apache.fineract.useradministration.data.PermissionData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.data.RolePermissionsData;

/** Runtime-only field visibility: keep the Gson response fields without adding them to the OpenAPI models. */
public final class SecurityApiJacksonMixins {

    public static final Map<Class<?>, Class<?>> MIXINS = Map.of(AppUserData.class, Fields.class, PermissionData.class, Fields.class,
            RoleData.class, Fields.class, RolePermissionsData.class, Fields.class, PasswordValidationPolicyData.class, Fields.class,
            AuthenticatedUserData.class, Fields.class, AuthenticatedOauthUserData.class, Fields.class);

    private SecurityApiJacksonMixins() {}

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
    abstract static class Fields {}
}
