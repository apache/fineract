package org.mifosplatform.template.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.gson.annotations.SerializedName;

@JsonSerialize(using = TemplateTypeSerializer.class)
public enum TemplateType {

    @SerializedName("Document")
    DOCUMENT(0, "Document");

    /**
     * @SerializedName("E-Mail") EMAIL(1, "E-Mail"),
     * @SerializedName("SMS") SMS(2, "SMS");
     */
    private int id;
    private String name;

    private TemplateType(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
