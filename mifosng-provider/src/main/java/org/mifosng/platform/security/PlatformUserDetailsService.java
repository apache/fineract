package org.mifosng.platform.security;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Interface to hide implementation detail of spring security.
 */
public interface PlatformUserDetailsService extends UserDetailsService {
	// no added behaviour
}