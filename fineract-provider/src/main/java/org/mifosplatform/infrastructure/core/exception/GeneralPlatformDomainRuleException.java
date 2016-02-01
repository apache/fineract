/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exception;

/**
 * A {@link RuntimeException} thrown when valid api request end up violating
 * some domain rule.
 */
public final class GeneralPlatformDomainRuleException extends AbstractPlatformDomainRuleException {

    public GeneralPlatformDomainRuleException(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
    }
}