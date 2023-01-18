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
package org.apache.fineract.infrastructure.event.external.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationItemData;

final class ExternalEventConfigurationApiResourceSwagger {

    private ExternalEventConfigurationApiResourceSwagger() {}

    @Schema(description = "GetExternalEventConfigurationsResponse")
    public static final class GetExternalEventConfigurationsResponse {

        private GetExternalEventConfigurationsResponse() {}

        public List<ExternalEventConfigurationItemData> externalEventConfiguration;
    }

    @Schema(description = "PutExternalEventConfigurationsRequest")
    public static final class PutExternalEventConfigurationsRequest {

        private PutExternalEventConfigurationsRequest() {}

        @Schema(example = "\"CentersCreateBusinessEvent\":true,\n" + "\"ClientActivateBusinessEvent\":true")
        public Map<String, Boolean> externalEventConfigurations;

    }

}
