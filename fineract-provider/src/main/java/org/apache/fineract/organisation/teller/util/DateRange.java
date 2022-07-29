/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.organisation.teller.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.StringTokenizer;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateRange {

    private static final Logger LOG = LoggerFactory.getLogger(DateRange.class);
    private static final String ISO_8601_DATE_PATTERN = "yyy-MM-dd";
    private static final String RANGE_DELIMITER = "..";

    private LocalDate startDate;
    private LocalDate endDate;

    public DateRange() {

    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public static DateRange fromString(final String dateToParse) {

        final DateRange dateRange = new DateRange();

        final String testee;
        if (dateToParse == null) {
            testee = DateUtils.DEFAULT_DATE_FORMATTER.format(DateUtils.getBusinessLocalDate());
        } else {
            testee = dateToParse;
        }

        final StringTokenizer tokenizer = new StringTokenizer(testee, DateRange.RANGE_DELIMITER);

        try {
            dateRange.setStartDate(LocalDate.parse(tokenizer.nextToken(), DateUtils.DEFAULT_DATE_FORMATTER));
        } catch (DateTimeParseException ex) {
            LOG.error("Problem occurred in DateRange function Could not parse the date recieved.", ex);
        }

        if (tokenizer.hasMoreTokens()) {
            try {
                dateRange.setEndDate(LocalDate.parse(tokenizer.nextToken(), DateUtils.DEFAULT_DATE_FORMATTER));
            } catch (DateTimeParseException ex) {
                LOG.error("Problem occurred in DateRange function Could not parse the date recieved.", ex);
            }
        }
        return dateRange;
    }
}
