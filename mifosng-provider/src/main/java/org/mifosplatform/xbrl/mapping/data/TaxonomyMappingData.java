package org.mifosplatform.xbrl.mapping.data;

public class TaxonomyMappingData {

    private final String identifier;
    private final String config;

    public TaxonomyMappingData(final String identifier, final String config) {
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
