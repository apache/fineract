/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.domain;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Interface to protect platform from implementation detail of spring security.
 */
public interface PlatformUser extends UserDetails {

}