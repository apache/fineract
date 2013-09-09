package org.mifosplatform.xbrl.report.service;

import java.sql.Date;

import org.mifosplatform.xbrl.taxonomy.data.XBRLData;

public interface XBRLResultService {

    XBRLData getXBRLResult(Date startDate, Date endDate, String currency);

}