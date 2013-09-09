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