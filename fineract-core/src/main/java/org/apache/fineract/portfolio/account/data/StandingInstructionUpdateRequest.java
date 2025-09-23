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
package org.apache.fineract.portfolio.account.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.portfolio.account.mapper.StandingInstructionUpdateRequestMapper;
import org.apache.fineract.validation.constraints.Locale;

@Getter
@Setter
@NoArgsConstructor
public class StandingInstructionUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String amount;
    private String validTill;
    private String dateFormat;
    private String recurrenceOnMonthDay;
    private String validFrom;
    @Size(max = 50, message = "{org.apache.fineract.portfolio.account.data.locale.size}")
    @Locale
    private String locale;
    private String priority;
    private String recurrenceType;
    private String recurrenceInterval;
    private String monthDayFormat;
    private String instructionType;
    private String recurrenceFrequency;
    private String status;

    @JsonIgnore
    private Long standingInstructionId;

    @JsonIgnore
    private String commandParam;

    public static StandingInstructionUpdateRequest withCommandParamAndStandingInstructionId(String commandParam, Long standingInstructionId,
            StandingInstructionUpdateRequest request) {
        final StandingInstructionUpdateRequest result = StandingInstructionUpdateRequestMapper.INSTANCE.copy(request);

        result.setCommandParam(commandParam);
        result.setStandingInstructionId(standingInstructionId);

        return result;
    }
}
