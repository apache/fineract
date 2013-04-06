/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exception;

public class UnrecognizedQueryParamException extends RuntimeException {

    private final String queryParamKey;
    private final String queryParamValue;
    private final Object[] supportedParams;

    public UnrecognizedQueryParamException(final String queryParamKey, final String queryParamValue, final Object... supportedParams) {
        this.queryParamKey = queryParamKey;
        this.queryParamValue = queryParamValue;
        this.supportedParams = supportedParams;
    }

    public String getQueryParamKey() {
        return queryParamKey;
    }

    public String getQueryParamValue() {
        return queryParamValue;
    }

    public Object[] getSupportedParams() {
        return this.supportedParams;
    }
}