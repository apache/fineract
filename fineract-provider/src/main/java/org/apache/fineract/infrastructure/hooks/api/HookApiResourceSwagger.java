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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.infrastructure.hooks.data.Event;
import org.apache.fineract.infrastructure.hooks.data.Field;
import org.apache.fineract.infrastructure.hooks.data.Grouping;
import org.apache.fineract.infrastructure.hooks.data.HookTemplateData;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by sanyam on 11/8/17.
 */

final class HookApiResourceSwagger {
    private HookApiResourceSwagger() {

    }

    @ApiModel(value = "PostHookRequest")
    public static final class PostHookRequest {
        private PostHookRequest () {

        }

        @ApiModelProperty(example = "Web")
        public String name;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        @ApiModelProperty(example = "Kremlin")
        public String displayName;
        @ApiModelProperty(example = "1")
        public Long templateId;
        public List<Event> events;
        public List<Field> config;
    }

    @ApiModel(value = "PostHookResponse")
    public static final class PostHookResponse {
        private PostHookResponse() {

        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
    }

    @ApiModel(value = "GetHookResponse")
    public static final class GetHookResponse {
        private GetHookResponse() {

        }

        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Web")
        public String name;
        @ApiModelProperty(example = "Kremlin")
        public String displayName;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        @ApiModelProperty(example = "[2014, 9, 16]")
        public LocalDate createdAt;
        @ApiModelProperty(example = "[2014, 9, 16]")
        public LocalDate updatedAt;
        @ApiModelProperty(example = "1")
        public Long templateId;
        @ApiModelProperty(example = "My UGD")
        public String templateName;
        public List<Event> events;
        public List<Field> config;
    }

    @ApiModel(value = "GetHookTemplateResponse")
    public static final class GetHookTemplateResponse {
        private GetHookTemplateResponse() {

        }

        public List<HookTemplateData> templates;
        public List<Grouping> groupings;
    }

    @ApiModel(value = "DeleteHookResponse")
    public static final class DeleteHookResponse {
        private DeleteHookResponse() {

        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
    }

    @ApiModel(value = "PutHookRequest")
    public static final class PutHookRequest {
        private PutHookRequest () {

        }

        @ApiModelProperty(example = "Web")
        public String name;
        @ApiModelProperty(example = "true")
        public Boolean isActive;
        @ApiModelProperty(example = "Kremlin")
        public String displayName;
        @ApiModelProperty(example = "1")
        public Long templateId;
        public List<Event> events;
        public List<Field> config;
    }

    @ApiModel(value = "PutHookResponse")
    public static final class PutHookResponse {
        private PutHookResponse () {

        }
        final class PutHookResponseChangesSwagger {
            private PutHookResponseChangesSwagger() {}
                @ApiModelProperty(example = "Kremlin")
                public String displayName;
                @ApiModelProperty(example = "1")
                public List<Event> events;
                public List<Field> config;
        }
        @ApiModelProperty(example = "4")
        public Long resourceId;
        public PutHookResponseChangesSwagger changes;
    }
}
