/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.integrationtests.common.funds;

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
