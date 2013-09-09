package org.mifosplatform.mix.service;

import java.sql.Date;

import org.mifosplatform.mix.data.XBRLData;

public interface XBRLResultService {

    XBRLData getXBRLResult(Date startDate, Date endDate, String currency);

}