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
package org.apache.fineract.cob.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

final class ConfigureBusinessStepResourceSwagger {

    private ConfigureBusinessStepResourceSwagger() {

    }

    @Schema(description = "GetBusinessJobConfigResponse")
    public static final class GetBusinessJobConfigResponse {

        private GetBusinessJobConfigResponse() {}

        public List<String> businessJobs;

    }

    @Schema(description = "GetBusinessStepConfigResponse")
    public static final class GetBusinessStepConfigResponse {

        private GetBusinessStepConfigResponse() {}

        @Schema(example = "CLOSE_OF_BUSINESS")
        public String jobName;
        public List<BusinessStep> businessSteps;

    }

    @Schema(description = "BusinessStep")
    public static final class BusinessStep {

        private BusinessStep() {}

        @Schema(example = "APPLY_PENALTY_FOR_OVERDUE_LOANS")
        public String stepName;
        @Schema(example = "1")
        public Long order;

    }

    @Schema(description = "UpdateBusinessStepConfigRequest")
    public static final class UpdateBusinessStepConfigRequest {

        private UpdateBusinessStepConfigRequest() {}

        public List<BusinessStep> businessSteps;

    }
}
