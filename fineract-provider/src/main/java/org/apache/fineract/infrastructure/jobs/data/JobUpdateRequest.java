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

package org.apache.fineract.infrastructure.jobs.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.quartz.CronExpression;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class JobUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long jobId;

    private String displayName;
    private String cronExpression;
    private Boolean active;

    @JsonIgnore
    @AssertTrue(message = "{org.apache.fineract.infrastructure.jobs.update.at-least-one-field}")
    public boolean isAtLeastOneFieldPresent() {
        return displayName != null || cronExpression != null || active != null;
    }

    @JsonIgnore
    @AssertTrue(message = "{org.apache.fineract.infrastructure.jobs.cron-expression.invalid}")
    public boolean isCronExpressionValid() {
        return cronExpression == null || CronExpression.isValidExpression(cronExpression.trim());
    }
}
