/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exception;

/**
 * A {@link RuntimeException} thrown when data integrity problems happen due to
 * state modifying actions.
 */
public class PlatformDataIntegrityException extends RuntimeException {

    private final String globalisationMessageCode;
    private final String defaultUserMessage;
    private final String parameterName;
    private final Object[] defaultUserMessageArgs;

    public PlatformDataIntegrityException(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        this.globalisationMessageCode = globalisationMessageCode;
        this.defaultUserMessage = defaultUserMessage;
        this.parameterName = null;
        this.defaultUserMessageArgs = defaultUserMessageArgs;
    }

    public PlatformDataIntegrityException(final String globalisationMessageCode, final String defaultUserMessage,
            final String parameterName, final Object... defaultUserMessageArgs) {
        this.globalisationMessageCode = globalisationMessageCode;
        this.defaultUserMessage = defaultUserMessage;
        this.parameterName = parameterName;
        this.defaultUserMessageArgs = defaultUserMessageArgs;
    }

    public String getGlobalisationMessageCode() {
        return this.globalisationMessageCode;
    }

    public String getDefaultUserMessage() {
        return this.defaultUserMessage;
    }

    public Object[] getDefaultUserMessageArgs() {
        return this.defaultUserMessageArgs;
    }

    public String getParameterName() {
        return this.parameterName;
    }
}