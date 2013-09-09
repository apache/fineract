package org.mifosplatform.xbrl.taxonomy.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;

public class XBRLData {

    private HashMap<TaxonomyData, BigDecimal> resultMap;
    private Date startDate;
    private Date endDate;
    private String currency;

    public XBRLData(HashMap<TaxonomyData, BigDecimal> resultMap, Date startDate, Date endDate, String currency) {
        this.resultMap = resultMap;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currency = currency;
    }

    public HashMap<TaxonomyData, BigDecimal> getResultMap() {
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
