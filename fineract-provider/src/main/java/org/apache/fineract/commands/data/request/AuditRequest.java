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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Setter
@Getter
@NoArgsConstructor
public class AuditRequest implements Serializable {

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
    @QueryParam("checkerId")
    private Long checkerId;
    @QueryParam("checkerDateTimeFrom")
    private String checkerDateTimeFrom;
    @QueryParam("checkerDateTimeTo")
    private String checkerDateTimeTo;
    @QueryParam("status")
    private String status;
    @QueryParam("clientId")
    private Long clientId;
    @QueryParam("loanId")
    private Long loanId;
    @QueryParam("officeId")
    private Long officeId;
    @QueryParam("groupId")
    private Long groupId;
    @QueryParam("savingsAccountId")
    private Long savingsAccountId;
    @QueryParam("processingResult")
    private String processingResult;
    @QueryParam("dateFormat")
    private String dateFormat;
    @QueryParam("locale")
    private String locale;

    public LocalDateTime getMakerDateTimeFrom() {
        return DateUtils.convertDateTimeStringToLocalDateTime(makerDateTimeFrom, dateFormat, locale, LocalTime.MIN);
    }

    public LocalDateTime getMakerDateTimeTo() {
        return DateUtils.convertDateTimeStringToLocalDateTime(makerDateTimeTo, dateFormat, locale, LocalTime.MAX);
    }

    public LocalDateTime getCheckerDateTimeFrom() {
        return DateUtils.convertDateTimeStringToLocalDateTime(checkerDateTimeFrom, dateFormat, locale, LocalTime.MIN);
    }

    public LocalDateTime getCheckerDateTimeTo() {
        return DateUtils.convertDateTimeStringToLocalDateTime(checkerDateTimeTo, dateFormat, locale, LocalTime.MAX);
    }
}
