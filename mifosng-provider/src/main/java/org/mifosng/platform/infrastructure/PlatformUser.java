package org.mifosng.platform.infrastructure;

import org.springframework.security.core.userdetails.UserDetails;

/**
 *	Interface to protect platform from implementation detail of spring security.
 */
public interface PlatformUser extends UserDetails {

//	void updatePasswordOnFirstTimeLogin(String newPasswordEncoded);
}