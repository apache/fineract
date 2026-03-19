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
package org.apache.fineract.organisation.staff.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StaffDateMapper {

    public static final String DEFAULT_DATE_FORMAT = "dd MMMM yyyy";

    public String map(LocalDate date, String format) {
        if (date == null) {
            return null;
        }

        return DateTimeFormatter.ofPattern(Optional.ofNullable(format).orElse(DEFAULT_DATE_FORMAT)).format(date);
    }

    public LocalDate map(String date, String format) {
        if (StringUtils.isEmpty(date)) {
            return null;
        }

        return LocalDate.parse(date, DateTimeFormatter.ofPattern(Optional.ofNullable(format).orElse(DEFAULT_DATE_FORMAT)));
    }
}
