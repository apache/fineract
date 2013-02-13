/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exception;

public class UnrecognizedQueryParamException extends RuntimeException {

    private final String queryParamKey;
    private final String queryParamValue;

    public UnrecognizedQueryParamException(String queryParamKey, String queryParamValue) {
        this.queryParamKey = queryParamKey;
        this.queryParamValue = queryParamValue;
    }

    public String getQueryParamKey() {
        return queryParamKey;
    }

    public String getQueryParamValue() {
        return queryParamValue;
    }
}