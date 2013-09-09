package org.mifosplatform.xbrl.report.data;

import org.mifosplatform.xbrl.taxonomy.data.TaxonomyData;

public class ContextData {

    private final String dimensionType;
    private final String dimension;
    private final Integer periodType;

    public ContextData(String dimensionType, String dimension, Integer taxonomyType) {
        this.dimensionType = dimensionType;
        this.dimension = dimension;
        this.periodType = (taxonomyType == TaxonomyData.BALANCESHEET || taxonomyType == TaxonomyData.PORTFOLIO) ? 0 : 1;
    }

    public String getDimensionType() {
        return this.dimensionType;
    }

    public String getDimension() {
        return this.dimension;
    }

    public Integer getPeriodType() {
        return this.periodType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.dimension == null) ? 0 : this.dimension.hashCode());
        result = prime * result + ((this.dimensionType == null) ? 0 : this.dimensionType.hashCode());
        result = prime * result + ((this.periodType == null) ? 0 : this.periodType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ContextData other = (ContextData) obj;
        if (this.dimension == null) {
            if (other.dimension != null) return false;
        } else if (!this.dimension.equals(other.dimension)) return false;
        if (this.dimensionType == null) {
            if (other.dimensionType != null) return false;
        } else if (!this.dimensionType.equals(other.dimensionType)) return false;
        if (this.periodType == null) {
            if (other.periodType != null) return false;
        } else if (!this.periodType.equals(other.periodType)) return false;
        return true;
    }

}
