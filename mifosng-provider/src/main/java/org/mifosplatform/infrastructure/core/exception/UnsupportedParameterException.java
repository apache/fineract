/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exception;

import java.util.List;

/**
 * A {@link RuntimeException} that is thrown in the case where invalid
 * parameters are sent in the body of the request to the platform api.
 */
public class UnsupportedParameterException extends RuntimeException {

    private final List<String> unsupportedParameters;

    public UnsupportedParameterException(final List<String> unsupportedParameters) {
        this.unsupportedParameters = unsupportedParameters;
    }

    public List<String> getUnsupportedParameters() {
        return this.unsupportedParameters;
    }
}