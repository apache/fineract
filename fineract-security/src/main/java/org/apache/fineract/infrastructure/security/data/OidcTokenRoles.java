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
package org.apache.fineract.infrastructure.security.data;

import java.util.Locale;
import java.util.Set;

/**
 * What an OIDC token says about the roles of its user.
 *
 * @param decides
 *            whether the token decides the user's roles: its issuer is configured with a roles claim and the token
 *            carries that claim. When false, the user's roles are left as they are.
 * @param issuer
 *            the issuer of the token
 * @param names
 *            the role names in the claim
 * @param managedRoles
 *            lower-case names of the roles the issuer may grant and remove; empty means every role
 */
public record OidcTokenRoles(boolean decides, String issuer, Set<String> names, Set<String> managedRoles) {

    private static final OidcTokenRoles UNDECIDED = new OidcTokenRoles(false, null, Set.of(), Set.of());

    public static OidcTokenRoles undecided() {
        return UNDECIDED;
    }

    public static OidcTokenRoles of(String issuer, Set<String> names, Set<String> managedRoles) {
        return new OidcTokenRoles(true, issuer, Set.copyOf(names), Set.copyOf(managedRoles));
    }

    public boolean manages(String roleName) {
        return managedRoles.isEmpty() || managedRoles.contains(roleName.toLowerCase(Locale.ROOT));
    }
}
