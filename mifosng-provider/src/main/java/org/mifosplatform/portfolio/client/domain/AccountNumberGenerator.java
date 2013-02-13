/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

/**
 * Responsible for generating unique account number based on some rules or patterns.
 * 
 * @see ZeroPaddedAccountNumberGenerator
 */
public interface AccountNumberGenerator {

    String generate();

}
