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
package org.apache.fineract.infrastructure.core.jackson3;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.jersey.serializer.MaskedValueSerializer;

/**
 * Stands in for a command's result on the replay path.
 *
 * <p>
 * Deliberately a top-level class rather than a nested one, because {@code JsonCommandMapper} records the result's type
 * with {@code getCanonicalName()} and reads it back with {@code Class.forName}. Those two agree for a top-level class
 * and disagree for a nested one, whose canonical name uses a dot where the binary name uses a dollar — so a nested
 * stand-in would exercise a path real command results never take.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandResponseSample {

    private String text;
    private Long resourceId;
    private Integer count;
    private BigDecimal amount;
    private BigDecimal precise;
    private Status status;
    private LocalDate localDate;
    private LocalDateTime localDateTime;
    private OffsetDateTime offsetDateTime;
    private LocalTime localTime;
    private Date date;
    private ExternalId externalId;
    @JsonSerialize(using = MaskedValueSerializer.class)
    private String secret;
    private List<LocalDate> dates;

    public static CommandResponseSample populated() {
        return new CommandResponseSample("hello", Long.MAX_VALUE, 7, new BigDecimal("1.50"),
                new BigDecimal("12345678901234567890.12345678901"), Status.ACTIVE, LocalDate.of(2026, 10, 5),
                LocalDateTime.parse("2026-10-05T11:22:33.123456789"), OffsetDateTime.parse("2026-10-05T11:22:33.123456789Z"),
                LocalTime.parse("11:22:33.123456789"), Date.from(java.time.Instant.parse("2026-10-05T11:22:33Z")), new ExternalId("ext-1"),
                "secret-value", List.of(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 3)));
    }

    public enum Status {
        ACTIVE, CLOSED
    }
}
