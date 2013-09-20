/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.data;

public class ApiErrorMessageArg {

    /**
     * The actual value of the parameter (if any) as passed to API.
     */
    private Object value;

    public static ApiErrorMessageArg from(final Object object) {
        return new ApiErrorMessageArg(object);
    }

    protected ApiErrorMessageArg() {
        //
    }

    public ApiErrorMessageArg(final Object object) {
        this.value = object;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }
}