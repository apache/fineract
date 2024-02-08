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
package org.apache.fineract.template.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.apache.fineract.template.domain.TemplateMapper;

/**
 * Created by sanyam on 21/8/17.
 */
final class TemplatesApiResourcesSwagger {

    private TemplatesApiResourcesSwagger() {

    }

    @Schema(description = "GetTemplatesResponse")
    public static final class GetTemplatesResponse {

        private GetTemplatesResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Text")
        public String name;
        @Schema(example = "1")
        public Long entity;
        @Schema(example = "0")
        public Long type;
        @Schema(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @Schema(description = "GetTemplatesTemplateResponse")
    public static final class GetTemplatesTemplateResponse {

        private GetTemplatesTemplateResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Text")
        public String name;
        @Schema(example = "1")
        public Long entity;
        @Schema(example = "0")
        public Long type;
        @Schema(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @Schema(description = "PostTemplatesRequest")
    public static final class PostTemplatesRequest {

        private PostTemplatesRequest() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Text")
        public String name;
        @Schema(example = "1")
        public Long entity;
        @Schema(example = "0")
        public Long type;
        @Schema(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @Schema(description = "PostTemplatesResponse")
    public static final class PostTemplatesResponse {

        private PostTemplatesResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;

    }

    @Schema(description = "GetTemplatesTemplateIdResponse")
    public static final class GetTemplatesTemplateIdResponse {

        private GetTemplatesTemplateIdResponse() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Text")
        public String name;
        @Schema(example = "1")
        public Long entity;
        @Schema(example = "0")
        public Long type;
        @Schema(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @Schema(description = "PutTemplatesTemplateIdRequest")
    public static final class PutTemplatesTemplateIdRequest {

        private PutTemplatesTemplateIdRequest() {

        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Text")
        public String name;
        @Schema(example = "1")
        public Long entity;
        @Schema(example = "0")
        public Long type;
        @Schema(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @Schema(description = "PutTemplatesTemplateIdResponse")
    public static final class PutTemplatesTemplateIdResponse {

        private PutTemplatesTemplateIdResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;

    }

    @Schema(description = "DeleteTemplatesTemplateIdResponse")
    public static final class DeleteTemplatesTemplateIdResponse {

        private DeleteTemplatesTemplateIdResponse() {

        }

        @Schema(example = "1")
        public Long resourceId;

    }
}
