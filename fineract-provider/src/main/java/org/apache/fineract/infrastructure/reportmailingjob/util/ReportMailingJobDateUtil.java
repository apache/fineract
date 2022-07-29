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
package org.apache.fineract.infrastructure.reportmailingjob.util;

import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobStretchyReportParamDateOption;

public final class ReportMailingJobDateUtil {

    private ReportMailingJobDateUtil() {

    }

    public static final String MYSQL_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * get the current date as string using the mysql date format yyyy-MM-dd
     **/
    public static String getTodayDateAsString() {
        return DateUtils.getLocalDateOfTenant().format(DateUtils.DEFAULT_DATE_FORMATTER);
    }

    /**
     * get the yesterday's date as string using the mysql date format yyyy-MM-dd
     **/
    public static String getYesterdayDateAsString() {
        return DateUtils.getLocalDateOfTenant().minusDays(1).format(DateUtils.DEFAULT_DATE_FORMATTER);
    }

    /**
     * get the tomorrow's date as string using the mysql date format yyyy-MM-dd
     **/
    public static String getTomorrowDateAsString() {
        return DateUtils.getLocalDateOfTenant().plusDays(1).format(DateUtils.DEFAULT_DATE_FORMATTER);
    }

    /**
     * get date as string based on the value of the {@link ReportMailingJobStretchyReportParamDateOption} object
     *
     * @param reportMailingJobStretchyReportParamDateOption
     *            {@link ReportMailingJobStretchyReportParamDateOption} Enum
     **/
    public static String getDateAsString(
            final ReportMailingJobStretchyReportParamDateOption reportMailingJobStretchyReportParamDateOption) {
        String dateAsString = null;

        switch (reportMailingJobStretchyReportParamDateOption) {
            case TODAY:
                dateAsString = getTodayDateAsString();
            break;

            case YESTERDAY:
                dateAsString = getYesterdayDateAsString();
            break;

            case TOMORROW:
                dateAsString = getTomorrowDateAsString();
            break;

            default:
            break;
        }

        return dateAsString;
    }
}
