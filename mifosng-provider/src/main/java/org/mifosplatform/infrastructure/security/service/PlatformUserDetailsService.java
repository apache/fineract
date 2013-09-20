/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.service;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Interface to hide implementation detail of spring security.
 */
public interface PlatformUserDetailsService extends UserDetailsService {
    // no added behaviour
}