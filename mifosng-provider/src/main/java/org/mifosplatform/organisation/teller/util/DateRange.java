/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class DateRange {

    private static final String ISO_8601_DATE_PATTERN = "yyy-MM-dd";
    private static final String RANGE_DELIMITER = "..";

    private Date startDate;
    private Date endDate;

    public DateRange() {
        super();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public static DateRange fromString(final String dateToParse) {

        final DateRange dateRange = new DateRange();
        final SimpleDateFormat sdf = new SimpleDateFormat(DateRange.ISO_8601_DATE_PATTERN);
        final Calendar cal = Calendar.getInstance();

        final String testee;
        if (dateToParse == null) {
            testee = sdf.format(cal.getTime());
        } else {
            testee = dateToParse;
        }

        final StringTokenizer tokenizer = new StringTokenizer(testee, DateRange.RANGE_DELIMITER);

        try {
            cal.setTime(sdf.parse(tokenizer.nextToken()));
        } catch (ParseException ex) {

        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        dateRange.setStartDate(cal.getTime());

        if (tokenizer.hasMoreTokens()) {
            try {
                cal.setTime(sdf.parse(tokenizer.nextToken()));
            } catch (ParseException ex) {

            }
        }
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        dateRange.setEndDate(cal.getTime());

        return dateRange;
    }
}
