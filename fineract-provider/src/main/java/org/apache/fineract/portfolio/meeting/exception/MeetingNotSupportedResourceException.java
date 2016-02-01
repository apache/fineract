/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.meeting.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when meeting is captured
 * against not supported resource.
 */
public class MeetingNotSupportedResourceException extends AbstractPlatformDomainRuleException {

    public MeetingNotSupportedResourceException(final String defaultUserMessage, final Object... defaultUserMessageArgs) {
        super("error.msg.meeting.not.supported.resource", defaultUserMessage, defaultUserMessageArgs);
    }
}