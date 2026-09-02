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
package org.apache.fineract.commands.data.request;

import jakarta.ws.rs.QueryParam;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Setter
@Getter
@NoArgsConstructor
public class MakerCheckerRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @QueryParam("actionName")
    private String actionName;
    @QueryParam("entityName")
    private String entityName;
    @QueryParam("resourceId")
    private Long resourceId;
    @QueryParam("makerId")
    private Long makerId;
    @QueryParam("username")
    private String username;
    @QueryParam("makerDateTimeFrom")
    private String makerDateTimeFrom;
    @QueryParam("makerDateTimeTo")
    private String makerDateTimeTo;
    @QueryParam("clientId")
    private Long clientId;
    @QueryParam("loanid")
    private Long loanId;
    @QueryParam("officeId")
    private Long officeId;
    @QueryParam("groupId")
    private Long groupId;
    @QueryParam("savingsAccountId")
    private Long savingsAccountId;
    @QueryParam("dateFormat")
    private String dateFormat;
    @QueryParam("locale")
    private String locale;

    public OffsetDateTime getMakerDateTimeFrom() {
        OffsetDateTime parsed = tryParseDayMonthYear(makerDateTimeFrom, LocalTime.MIN);
        return parsed != null ? parsed
                : DateUtils.convertDateTimeStringToOffsetDateTime(makerDateTimeFrom, dateFormat, locale, LocalTime.MIN);
    }

    public OffsetDateTime getMakerDateTimeTo() {
        OffsetDateTime parsed = tryParseDayMonthYear(makerDateTimeTo, LocalTime.MAX);
        return parsed != null ? parsed
                : DateUtils.convertDateTimeStringToOffsetDateTime(makerDateTimeTo, dateFormat, locale, LocalTime.MAX);
    }

    private static final DateTimeFormatter DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private static OffsetDateTime tryParseDayMonthYear(String value, LocalTime time) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value.trim(), DAY_MONTH_YEAR);
            return date.atTime(time).atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
