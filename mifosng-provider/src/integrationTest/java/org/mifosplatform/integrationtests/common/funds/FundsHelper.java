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
        private String externalId;

        private Builder(final String name) {
            this.name = name;
        }

        public Builder externalId(final String externalId) {
            this.externalId = externalId;
            return this;
        }

        public FundsHelper build() {
            return new FundsHelper(this.name, this.externalId);
        }

    }

    private String name;
    private String externalId;
    private Long resourceId;

    FundsHelper() {
        super();
    }

    private FundsHelper(final String name,
                        final String externalId) {
        super();
        this.name = name;
        this.externalId = externalId;
    }

    public String getName() {
        return this.name;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public Long getResourceId() {
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

    @Override
    public int hashCode() {
        if (this.name != null) {
            return this.name.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof FundsHelper)) {
            return false;
        }

        FundsHelper fh = (FundsHelper)o;

        if (this.name.equals(fh.name)) {
            return true;
        }

        return false;
    }

}
