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
package org.apache.fineract.infrastructure.instancemode.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

public class InstanceModeApiResourceSwagger {

    @ToString
    @Schema(description = "ChangeInstanceModeRequest")
    @Getter
    public static final class ChangeInstanceModeRequest {

        @Schema(required = true, example = "true")
        public boolean readEnabled;
        @Schema(required = true, example = "true")
        public boolean writeEnabled;
        @Schema(required = true, example = "true")
        public boolean batchWorkerEnabled;
        @Schema(required = true, example = "true")
        public boolean batchManagerEnabled;
    }
}
