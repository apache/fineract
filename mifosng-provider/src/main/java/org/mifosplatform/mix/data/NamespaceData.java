package org.mifosplatform.mix.data;

public class NamespaceData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String prefix;
    private final String url;

    public NamespaceData(final Long id, final String prefix, final String url) {

        this.id = id;
        this.prefix = prefix;
        this.url = url;
    }

    public String url() {
        return this.url;
    }
}