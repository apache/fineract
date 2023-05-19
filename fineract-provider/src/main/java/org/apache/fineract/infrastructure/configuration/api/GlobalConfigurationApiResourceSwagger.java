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
package org.apache.fineract.infrastructure.configuration.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;

/**
 * Created by sanyam on 30/7/17.
 */
final class GlobalConfigurationApiResourceSwagger {

    private GlobalConfigurationApiResourceSwagger() {

    }

    @Schema(description = "GetGlobalConfigurationsResponse")
    public static final class GetGlobalConfigurationsResponse {

        private GetGlobalConfigurationsResponse() {}

        public List<GlobalConfigurationPropertyData> globalConfiguration;
    }

    @Schema(description = "PutGlobalConfigurationsRequest")
    public static final class PutGlobalConfigurationsRequest {

        private PutGlobalConfigurationsRequest() {}

        @Schema(example = "true")
        public boolean enabled;
        @Schema(example = "2")
        public Long value;
        @Schema(example = "20 September 2011")
        public String dateValue;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "random text")
        public String stringValue;
    }

    @Schema(description = "PutGlobalConfigurationsResponse")
    public static final class PutGlobalConfigurationsResponse {

        private PutGlobalConfigurationsResponse() {}

        static final class PutGlobalConfigurationsResponsechangesSwagger {

            private PutGlobalConfigurationsResponsechangesSwagger() {}

            @Schema(example = "true")
            public boolean enabled;
            @Schema(example = "2")
            public Long value;
            @Schema(example = "20 September 2011")
            public String dateValue;
            @Schema(example = "random text")
            public String stringValue;
        }

        @Schema(example = "4")
        public Long resourceId;
        public PutGlobalConfigurationsResponsechangesSwagger changes;
    }

}
