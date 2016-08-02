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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobStretchyReportParamDateOption;

public class ReportMailingJobDateUtil {
    public static final String MYSQL_DATE_FORMAT = "yyyy-MM-dd";
    
    /** 
     * get the current date as string using the mysql date format yyyy-MM-dd 
     **/
    public static String getTodayDateAsString() {
        // get a calendar instance, which defaults to "now"
        Calendar calendar = Calendar.getInstance();
        
        // get a date to represent "today"
        Date today = calendar.getTime();
        
        // get a SimpleDateFormat instance, passing the mysql date format as parameter
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MYSQL_DATE_FORMAT);
        
        // return date as string
        return simpleDateFormat.format(today);
    }
    
    /** 
     * get the yesterday's date as string using the mysql date format yyyy-MM-dd 
     **/
    public static String getYesterdayDateAsString() {
        // get a calendar instance, which defaults to "now"
        Calendar calendar = Calendar.getInstance();
        
        // add one day to the date/calendar
        calendar.add(Calendar.DAY_OF_YEAR, -1);
         
        // now get "yesterday"
        Date yesterday = calendar.getTime();
        
        // get a SimpleDateFormat instance, passing the mysql date format as parameter
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MYSQL_DATE_FORMAT);
        
        // return date as string
        return simpleDateFormat.format(yesterday);
    }
    
    /** 
     * get the tomorrow's date as string using the mysql date format yyyy-MM-dd 
     **/
    public static String getTomorrowDateAsString() {
        // get a calendar instance, which defaults to "now"
        Calendar calendar = Calendar.getInstance();
        
        // add one day to the date/calendar
        calendar.add(Calendar.DAY_OF_YEAR, 1);
         
        // now get "tomorrow"
        Date tomorrow = calendar.getTime();
        
        // get a SimpleDateFormat instance, passing the mysql date format as parameter
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MYSQL_DATE_FORMAT);
        
        // return date as string
        return simpleDateFormat.format(tomorrow);
    }
    
    /** 
     * get date as string based on the value of the {@link ReportMailingJobStretchyReportParamDateOption} object
     * 
     * @param reportMailingJobStretchyReportParamDateOption {@link ReportMailingJobStretchyReportParamDateOption} Enum
     **/
    public static String getDateAsString(final ReportMailingJobStretchyReportParamDateOption reportMailingJobStretchyReportParamDateOption) {
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
