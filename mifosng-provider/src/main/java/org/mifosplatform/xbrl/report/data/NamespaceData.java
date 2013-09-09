package org.mifosplatform.xbrl.report.data;

public class NamespaceData {

    private final Long id;
    private final String prefix;
    private final String url;

    public NamespaceData(Long id, String prefix, String url) {

        this.id = id;
        this.prefix = prefix;
        this.url = url;
    }

    public Long getId() {
        return this.id;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getUrl() {
        return this.url;
    }

}
