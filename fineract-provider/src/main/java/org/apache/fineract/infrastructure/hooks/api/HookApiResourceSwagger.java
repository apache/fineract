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
package org.apache.fineract.infrastructure.hooks.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.hooks.data.Event;
import org.apache.fineract.infrastructure.hooks.data.Field;
import org.apache.fineract.infrastructure.hooks.data.Grouping;
import org.apache.fineract.infrastructure.hooks.data.HookTemplateData;

/**
 * Created by sanyam on 11/8/17.
 */

final class HookApiResourceSwagger {

    private HookApiResourceSwagger() {

    }

    @Schema(description = "PostHookRequest")
    public static final class PostHookRequest {

        private PostHookRequest() {

        }

        @Schema(example = "Web")
        public String name;
        @Schema(example = "true")
        public Boolean isActive;
        @Schema(example = "Kremlin")
        public String displayName;
        @Schema(example = "1")
        public Long templateId;
        public List<Event> events;
        public List<Field> config;
    }

    @Schema(description = "PostHookResponse")
    public static final class PostHookResponse {

        private PostHookResponse() {

        }

        @Schema(example = "4")
        public Long resourceId;
    }

    @Schema(description = "GetHookResponse")
    public static final class GetHookResponse {

        private GetHookResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Web")
        public String name;
        @Schema(example = "Kremlin")
        public String displayName;
        @Schema(example = "true")
        public Boolean isActive;
        @Schema(example = "[2014, 9, 16]")
        public LocalDate createdAt;
        @Schema(example = "[2014, 9, 16]")
        public LocalDate updatedAt;
        @Schema(example = "1")
        public Long templateId;
        @Schema(example = "My UGD")
        public String templateName;
        public List<Event> events;
        public List<Field> config;
    }

    @Schema(description = "GetHookTemplateResponse")
    public static final class GetHookTemplateResponse {

        private GetHookTemplateResponse() {

        }

        public List<HookTemplateData> templates;
        public List<Grouping> groupings;
    }

    @Schema(description = "DeleteHookResponse")
    public static final class DeleteHookResponse {

        private DeleteHookResponse() {

        }

        @Schema(example = "4")
        public Long resourceId;
    }

    @Schema(description = "PutHookRequest")
    public static final class PutHookRequest {

        private PutHookRequest() {

        }

        @Schema(example = "Web")
        public String name;
        @Schema(example = "true")
        public Boolean isActive;
        @Schema(example = "Kremlin")
        public String displayName;
        @Schema(example = "1")
        public Long templateId;
        public List<Event> events;
        public List<Field> config;
    }

    @Schema(description = "PutHookResponse")
    public static final class PutHookResponse {

        private PutHookResponse() {

        }

        static final class PutHookResponseChangesSwagger {

            private PutHookResponseChangesSwagger() {}

            @Schema(example = "Kremlin")
            public String displayName;
            @Schema(example = "1")
            public List<Event> events;
            public List<Field> config;
        }

        @Schema(example = "4")
        public Long resourceId;
        public PutHookResponseChangesSwagger changes;
    }
}
