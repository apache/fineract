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
package org.apache.fineract.portfolio.address.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by Chirag Gupta on 12/01/17.
 */
@SuppressWarnings({ "MemberName" })
final class EntityFieldConfigurationApiResourcesSwagger {

    private EntityFieldConfigurationApiResourcesSwagger() {}

    @Schema(description = "GetFieldConfigurationEntityResponse")
    public static final class GetFieldConfigurationEntityResponse {

        private GetFieldConfigurationEntityResponse() {}

        @Schema(example = "1")
        public Integer fieldConfigurationId;
        @Schema(example = "ADDRESS")
        public String entity;
        @Schema(example = "CLIENT")
        public String subentity;
        @Schema(example = "addressType")
        public String field;
        @Schema(example = "true")
        public String is_enabled;
        @Schema(example = "false")
        public String is_mandatory;
        @Schema(example = " ")
        public String validation_regex;
    }
}
