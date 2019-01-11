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
package org.apache.fineract.organisation.workingdays.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

import java.util.Collection;

/**
 * Created by sanyam on 19/8/17.
 */

final class WorkingDaysApiResourceSwagger {
    private WorkingDaysApiResourceSwagger() {

    }

    @ApiModel(value = "GetWorkingDaysResponse")
    public static final class GetWorkingDaysResponse {
        private GetWorkingDaysResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR")
        public String recurrence;
        public EnumOptionData repaymentRescheduleType;
        @ApiModelProperty(example = "true")
        public Boolean extendTermForDailyRepayments;
    }

    @ApiModel(value = "GetWorkingDaysTemplateResponse")
    public static final class GetWorkingDaysTemplateResponse {
        private GetWorkingDaysTemplateResponse() {

        }
        public Collection<EnumOptionData> repaymentRescheduleOptions;
    }

    @ApiModel(value = "PutWorkingDaysRequest")
    public static final class PutWorkingDaysRequest {
        private PutWorkingDaysRequest() {

        }
        @ApiModelProperty(example = "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR")
        public String recurrence;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "4")
        public EnumOptionData repaymentRescheduleType;
        @ApiModelProperty(example = "true")
        public Boolean extendTermForDailyRepayments;
    }

    @ApiModel(value = "PutWorkingDaysResponse")
    public static final class PutWorkingDaysResponse {
        private PutWorkingDaysResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }
}
