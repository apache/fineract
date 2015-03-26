/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.exception.ResetPasswordException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.exception.UnAuthenticatedUserException;
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

    public static final List<CommandWrapper> EXEMPT_FROM_PASSWORD_RESET_CHECK = new ArrayList<CommandWrapper>() {

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
