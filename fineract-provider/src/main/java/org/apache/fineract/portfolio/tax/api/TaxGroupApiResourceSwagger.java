/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.tax.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/14/17.
 */
final class TaxGroupApiResourceSwagger {
    private TaxGroupApiResourceSwagger() {
    }

    @ApiModel(value = "GetTaxesGroupResponse")
    public final static class GetTaxesGroupResponse {
        private GetTaxesGroupResponse() {
        }

        final class GetTaxesGroupTaxAssociations {
            private GetTaxesGroupTaxAssociations() {
            }

            final class GetTaxesGroupTaxComponent {
                private GetTaxesGroupTaxComponent() {
                }

                @ApiModelProperty(example = "7")
                public Integer id;
                @ApiModelProperty(example = "tax component 2")
                public String name;
            }

            @ApiModelProperty(example = "7")
            public Integer id;
            public GetTaxesGroupTaxComponent taxComponent;
            @ApiModelProperty(example = "[2016, 4, 11]")
            public LocalDate startDate;
        }

        @ApiModelProperty(example = "7")
        public Integer id;
        @ApiModelProperty(example = "tax group 1")
        public String name;
        public Set<GetTaxesGroupTaxAssociations> taxAssociations;
    }

    @ApiModel(value = "PostTaxesGroupRequest")
    public final static class PostTaxesGroupRequest {
        private PostTaxesGroupRequest() {
        }

        final class PostTaxesGroupTaxComponents {
            private PostTaxesGroupTaxComponents() {
            }

            @ApiModelProperty(example = "7")
            public Integer taxComponentId;
            @ApiModelProperty(example = "11 April 2016")
            public String startDate;
        }

        @ApiModelProperty(example = "tax group 1")
        public String name;
        @ApiModelProperty(example = "en")
        public String locale;
        public Set<PostTaxesGroupTaxComponents> taxComponents;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @ApiModel(value = "PostTaxesGroupResponse")
    public static final class PostTaxesGroupResponse {
        private PostTaxesGroupResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer resourceId;
    }

    @ApiModel(value = "PutTaxesGroupTaxGroupIdRequest")
    public final static class PutTaxesGroupTaxGroupIdRequest {
        private PutTaxesGroupTaxGroupIdRequest() {
        }

        final class PutTaxesGroupTaxComponents {
            private PutTaxesGroupTaxComponents() {
            }

            @ApiModelProperty(example = "7")
            public Integer id;
            @ApiModelProperty(example = "7")
            public Integer taxComponentId;
            @ApiModelProperty(example = "22 April 2016")
            public String endDate;
        }

        @ApiModelProperty(example = "tax group 2")
        public String name;
        @ApiModelProperty(example = "en")
        public String locale;
        public Set<PutTaxesGroupTaxComponents> taxComponents;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @ApiModel(value = "PutTaxesGroupTaxGroupIdResponse")
    public final static class PutTaxesGroupTaxGroupIdResponse {
        private PutTaxesGroupTaxGroupIdResponse() {
        }

        final class PutTaxesGroupChanges {
            private PutTaxesGroupChanges() {
            }

            final class PutTaxesGroupModifiedComponents {
                private PutTaxesGroupModifiedComponents() {
                }

                @ApiModelProperty(example = "Apr 22, 2016 12:00:00 AM")
                public String endDate;
                @ApiModelProperty(example = "7")
                public Integer taxComponentId;
            }

            @ApiModelProperty(example = "[6]")
            public List<Integer> addComponents;
            public Set<PutTaxesGroupModifiedComponents> modifiedComponents;
            @ApiModelProperty(example = "tax group 2")
            public String name;
        }

        @ApiModelProperty(example = "7")
        public Integer resourceId;
        public PutTaxesGroupChanges changes;
    }
}
