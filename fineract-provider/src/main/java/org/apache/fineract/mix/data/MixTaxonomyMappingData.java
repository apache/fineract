/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.data;

public class MixTaxonomyMappingData {

    private final String identifier;
    private final String config;

    public MixTaxonomyMappingData(final String identifier, final String config) {
        this.identifier = identifier;
        this.config = config;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getConfig() {
        return this.config;
    }
}