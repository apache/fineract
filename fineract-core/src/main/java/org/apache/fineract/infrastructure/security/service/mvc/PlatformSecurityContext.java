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
package org.apache.fineract.infrastructure.security.service.mvc;

import org.apache.fineract.commands.mvc.domain.CommandTypeWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformUserRightsContext;
import org.apache.fineract.useradministration.domain.AppUser;

public interface PlatformSecurityContext extends PlatformUserRightsContext {

    AppUser authenticatedUser();

    /**
     * Convenience method returns null (does not throw an exception) if an authenticated user is not present
     *
     * To be used only in service layer methods that can be triggered via both the API and batch Jobs (which do not have
     * an authenticated user)
     *
     * @return
     */
    AppUser getAuthenticatedUserIfPresent();

    void validateAccessRights(String resourceOfficeHierarchy);

    String officeHierarchy();

    boolean doesPasswordHasToBeRenewed(AppUser currentUser);

    AppUser authenticatedUser(CommandTypeWrapper<?> commandWrapper);
}
