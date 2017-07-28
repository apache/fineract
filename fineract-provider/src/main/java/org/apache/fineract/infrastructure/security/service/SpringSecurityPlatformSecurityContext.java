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
package org.apache.fineract.infrastructure.security.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.exception.ResetPasswordException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.exception.UnAuthenticatedUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Wrapper around spring security's {@link SecurityContext} for extracted the
 * current authenticated {@link AppUser}.
 */

@Service
public class SpringSecurityPlatformSecurityContext implements PlatformSecurityContext {

    // private final static Logger logger =
    // LoggerFactory.getLogger(SpringSecurityPlatformSecurityContext.class);

    private final ConfigurationDomainService configurationDomainService;

    protected static final List<CommandWrapper> EXEMPT_FROM_PASSWORD_RESET_CHECK = new ArrayList<CommandWrapper>() {

        {
            add(new CommandWrapperBuilder().updateUser(null).build());
        }
    };

    @Autowired
    SpringSecurityPlatformSecurityContext(final ConfigurationDomainService configurationDomainService) {
        this.configurationDomainService = configurationDomainService;
    }

    @Override
    public AppUser authenticatedUser() {

        AppUser currentUser = null;
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            final Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }
        }

        if (currentUser == null) { throw new UnAuthenticatedUserException(); }

        if (this.doesPasswordHasToBeRenewed(currentUser)) { throw new ResetPasswordException(currentUser.getId()); }

        return currentUser;
    }

    @Override
    public AppUser getAuthenticatedUserIfPresent() {

        AppUser currentUser = null;
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            final Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }
        }

        if (currentUser == null) { return null; }

        if (this.doesPasswordHasToBeRenewed(currentUser)) { throw new ResetPasswordException(currentUser.getId()); }

        return currentUser;
    }

    @Override
    public AppUser authenticatedUser(CommandWrapper commandWrapper) {

        AppUser currentUser = null;
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            final Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }
        }

        if (currentUser == null) { throw new UnAuthenticatedUserException(); }

        if (this.shouldCheckForPasswordForceReset(commandWrapper) && this.doesPasswordHasToBeRenewed(currentUser)) { throw new ResetPasswordException(
                currentUser.getId()); }

        return currentUser;

    }

    @Override
    public void validateAccessRights(final String resourceOfficeHierarchy) {

        final AppUser user = authenticatedUser();
        final String userOfficeHierarchy = user.getOffice().getHierarchy();

        if (!resourceOfficeHierarchy.startsWith(userOfficeHierarchy)) { throw new NoAuthorizationException(
                "The user doesn't have enough permissions to access the resource."); }

    }

    @Override
    public String officeHierarchy() {
        return authenticatedUser().getOffice().getHierarchy();
    }

    @Override
    public boolean doesPasswordHasToBeRenewed(AppUser currentUser) {

        if (this.configurationDomainService.isPasswordForcedResetEnable() && !currentUser.getPasswordNeverExpires()) {

            Long passwordDurationDays = this.configurationDomainService.retrievePasswordLiveTime();
            final Date passWordLastUpdateDate = currentUser.getLastTimePasswordUpdated();

            Calendar c = Calendar.getInstance();
            c.setTime(passWordLastUpdateDate);
            c.add(Calendar.DATE, passwordDurationDays.intValue());

            final Date passwordExpirationDate = c.getTime();

            if (DateUtils.getDateOfTenant().after(passwordExpirationDate)) { return true; }
        }
        return false;

    }

    private boolean shouldCheckForPasswordForceReset(CommandWrapper commandWrapper) {
        for (CommandWrapper commandItem : EXEMPT_FROM_PASSWORD_RESET_CHECK) {
            if (commandItem.actionName().equals(commandWrapper.actionName())
                    && commandItem.getEntityName().equals(commandWrapper.getEntityName())) { return false; }
        }
        return true;
    }

}
