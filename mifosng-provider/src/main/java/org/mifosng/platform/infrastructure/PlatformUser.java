package org.mifosng.platform.infrastructure;

import org.springframework.security.core.userdetails.UserDetails;

/**
 *	Interface to protect platform from implementation detail of spring security.
 */
public interface PlatformUser extends UserDetails {

	boolean isFirstTimeLoginRemaining();

	void updateUsernamePasswordOnFirstTimeLogin(String newUsername, String newPasswordEncoded);

	void updatePasswordOnFirstTimeLogin(String newPasswordEncoded);
}