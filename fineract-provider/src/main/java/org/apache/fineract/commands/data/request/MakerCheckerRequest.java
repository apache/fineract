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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    @QueryParam("timeZone")
    private String timeZone;

    public boolean hasMakerDateTimeFrom() {
        return makerDateTimeFrom != null && !makerDateTimeFrom.isBlank();
    }

    public boolean hasMakerDateTimeTo() {
        return makerDateTimeTo != null && !makerDateTimeTo.isBlank();
    }

    public LocalDateTime getMakerDateTimeFromLocal() {
        return DateUtils.convertDateTimeStringToLocalDateTime(makerDateTimeFrom, dateFormat, locale, LocalTime.MIN);
    }

    public LocalDateTime getMakerDateTimeToLocal() {
        return DateUtils.convertDateTimeStringToLocalDateTime(makerDateTimeTo, dateFormat, locale, LocalTime.MAX);
    }

    public LocalDateTime getMakerDateTimeFromOffset() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime(makerDateTimeFrom, dateFormat, locale, LocalTime.MIN,
                timeZone);
        return result != null ? result.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    public LocalDateTime getMakerDateTimeToOffset() {
        OffsetDateTime result = DateUtils.convertDateTimeStringToOffsetDateTime(makerDateTimeTo, dateFormat, locale, LocalTime.MAX,
                timeZone);
        return result != null ? result.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }
}
