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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.template.domain.TemplateMapper;

import java.util.List;

/**
 * Created by sanyam on 21/8/17.
 */
final class TemplatesApiResourcesSwagger {
    private TemplatesApiResourcesSwagger() {

    }

    @ApiModel(value = "GetTemplatesResponse")
    public static final class GetTemplatesResponse {
        private GetTemplatesResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Text")
        public String name;
        @ApiModelProperty(example = "1")
        public Long entity;
        @ApiModelProperty(example = "0")
        public Long type;
        @ApiModelProperty(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @ApiModel(value = "GetTemplatesTemplateResponse")
    public static final class GetTemplatesTemplateResponse {
        private GetTemplatesTemplateResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Text")
        public String name;
        @ApiModelProperty(example = "1")
        public Long entity;
        @ApiModelProperty(example = "0")
        public Long type;
        @ApiModelProperty(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @ApiModel(value = "PostTemplatesRequest")
    public static final class PostTemplatesRequest {
        private PostTemplatesRequest() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Text")
        public String name;
        @ApiModelProperty(example = "1")
        public Long entity;
        @ApiModelProperty(example = "0")
        public Long type;
        @ApiModelProperty(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @ApiModel(value = "PostTemplatesResponse")
    public static final class PostTemplatesResponse {
        private PostTemplatesResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;

    }

    @ApiModel(value = "GetTemplatesTemplateIdResponse")
    public static final class GetTemplatesTemplateIdResponse {
        private GetTemplatesTemplateIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Text")
        public String name;
        @ApiModelProperty(example = "1")
        public Long entity;
        @ApiModelProperty(example = "0")
        public Long type;
        @ApiModelProperty(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @ApiModel(value = "PutTemplatesTemplateIdRequest")
    public static final class PutTemplatesTemplateIdRequest {
        private PutTemplatesTemplateIdRequest() {

        }
        @ApiModelProperty(example = "1")
        public Long id;
        @ApiModelProperty(example = "Text")
        public String name;
        @ApiModelProperty(example = "1")
        public Long entity;
        @ApiModelProperty(example = "0")
        public Long type;
        @ApiModelProperty(example = "This is a loan for {{loan.clientName}}")
        public String text;
        public List<TemplateMapper> mappers;

    }

    @ApiModel(value = "PutTemplatesTemplateIdResponse")
    public static final class PutTemplatesTemplateIdResponse {
        private PutTemplatesTemplateIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;

    }

    @ApiModel(value = "DeleteTemplatesTemplateIdResponse")
    public static final class DeleteTemplatesTemplateIdResponse {
        private DeleteTemplatesTemplateIdResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;

    }
}
