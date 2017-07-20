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
package org.apache.fineract.organisation.holiday.data;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.joda.time.LocalDate;

public class HolidayData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final LocalDate fromDate;
    @SuppressWarnings("unused")
    private final LocalDate toDate;
    @SuppressWarnings("unused")
    private final LocalDate repaymentsRescheduledTo;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final EnumOptionData status;
    private final Integer reschedulingType;

    public HolidayData(final Long id, final String name, final String description, final LocalDate fromDate, final LocalDate toDate,
            final LocalDate repaymentsRescheduledTo, final EnumOptionData status, final Integer reschedulingType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.repaymentsRescheduledTo = repaymentsRescheduledTo;
        this.officeId = null;
        this.status = status;
        this.reschedulingType = reschedulingType;
    }
}
