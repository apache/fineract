package org.mifosplatform.xbrl.taxonomy.data;

public class TaxonomyData {

    public static final Integer PORTFOLIO = 0;
    public static final Integer BALANCESHEET = 1;
    public static final Integer INCOME = 2;
    public static final Integer EXPENSE = 3;

    private final Long id;
    private final String name;
    private final String namespace;
    private final String dimension;
    private final Integer type;
    private final String description;

    public TaxonomyData(Long id, String name, String namespace, String dimension, Integer type, String description) {

        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.dimension = dimension;
        this.type = type;
        this.description = description;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getDimension() {
        return this.dimension;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getType() {
        return this.type;
    }

    public boolean isPortfolio() {
        return this.type == 5;
    }

}
