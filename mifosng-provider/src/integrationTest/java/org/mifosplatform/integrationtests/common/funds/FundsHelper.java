/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.funds;

import com.google.gson.Gson;

public class FundsHelper {

    public static class Builder {

        private String name;

        private Builder(final String name) {
            this.name = name;
        }

        public FundsHelper build() {
            return new FundsHelper(this.name);
        }

    }

    private String name;
    private Integer resourceId;

    public FundsHelper() {
        super();
    }

    private FundsHelper(final String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Integer getResourceID() {
        return this.resourceId;
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }

    public static FundsHelper fromJSON(final String jsonData) {
        return new Gson().fromJson(jsonData, FundsHelper.class);
    }

    public static Builder create(final String name) {
        return new Builder(name);
    }

}
