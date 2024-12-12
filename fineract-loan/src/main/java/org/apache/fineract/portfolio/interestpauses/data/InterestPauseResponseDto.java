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
package org.apache.fineract.portfolio.interestpauses.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Response DTO for Interest Pause periods")
public class InterestPauseResponseDto {

    @Schema(example = "1", description = "ID of the loan term variation")
    private Long id;

    @Schema(example = "2024-01-01", description = "Start date of the interest pause period")
    private LocalDate startDate;

    @Schema(example = "2024-01-11", description = "End date of the interest pause period")
    private LocalDate endDate;

    @Schema(example = "yyyy-MM-dd", description = "Date format used to interpret start and end dates")
    private String dateFormat;

    @Schema(example = "en", description = "Locale used for date formatting")
    private String locale;
}
