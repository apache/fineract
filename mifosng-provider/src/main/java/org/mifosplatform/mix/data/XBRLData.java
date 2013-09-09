package org.mifosplatform.mix.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;

public class XBRLData {

    private final HashMap<MixTaxonomyData, BigDecimal> resultMap;
    private final Date startDate;
    private final Date endDate;
    private final String currency;

    public XBRLData(final HashMap<MixTaxonomyData, BigDecimal> resultMap, final Date startDate, final Date endDate, final String currency) {
        this.resultMap = resultMap;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currency = currency;
    }

    public HashMap<MixTaxonomyData, BigDecimal> getResultMap() {
        return this.resultMap;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public String getCurrency() {
        return this.currency;
    }
}