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
package org.apache.fineract.portfolio.calendar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.fineract.portfolio.calendar.domain.CalendarHistory;
import org.joda.time.LocalDate;


public class CalendarHistoryDataWrapper {
    
    private final List<CalendarHistory> calendarHistoryList;
    
    public CalendarHistoryDataWrapper(final Set<CalendarHistory> calendarHistoryList){
        this.calendarHistoryList = new ArrayList<>();       
        this.calendarHistoryList.addAll(calendarHistoryList);
        final Comparator<CalendarHistory> orderByDate = new Comparator<CalendarHistory>() {
            @Override
            public int compare(CalendarHistory calendarHistory1, CalendarHistory calendarHistory2) {
                return calendarHistory1.getEndDateLocalDate().compareTo(calendarHistory2.getEndDateLocalDate());
            }
        };
        Collections.sort(this.calendarHistoryList, orderByDate);
    }
    
    public CalendarHistory getCalendarHistory(final LocalDate dueRepaymentPeriodDate) {
        CalendarHistory calendarHistory = null;
        for (CalendarHistory history : this.calendarHistoryList) {
            if (history.getEndDateLocalDate().isAfter(dueRepaymentPeriodDate)) {
                calendarHistory = history;
               break;
            }
        }
        return calendarHistory;
    }
        
    public List<CalendarHistory> getCalendarHistoryList(){
        return this.calendarHistoryList;
    }
}
