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
package org.apache.fineract.portfolio.tax.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/14/17.
 */
final class TaxGroupApiResourceSwagger {

    private TaxGroupApiResourceSwagger() {}

    @Schema(description = "GetTaxesGroupResponse")
    public static final class GetTaxesGroupResponse {

        private GetTaxesGroupResponse() {}

        static final class GetTaxesGroupTaxAssociations {

            private GetTaxesGroupTaxAssociations() {}

            static final class GetTaxesGroupTaxComponent {

                private GetTaxesGroupTaxComponent() {}

                @Schema(example = "7")
                public Long id;
                @Schema(example = "tax component 2")
                public String name;
            }

            @Schema(example = "7")
            public Long id;
            public GetTaxesGroupTaxComponent taxComponent;
            @Schema(example = "[2016, 4, 11]")
            public LocalDate startDate;
        }

        @Schema(example = "7")
        public Long id;
        @Schema(example = "tax group 1")
        public String name;
        public Set<GetTaxesGroupTaxAssociations> taxAssociations;
    }

    @Schema(description = "PostTaxesGroupRequest")
    public static final class PostTaxesGroupRequest {

        private PostTaxesGroupRequest() {}

        static final class PostTaxesGroupTaxComponents {

            private PostTaxesGroupTaxComponents() {}

            @Schema(example = "7")
            public Long taxComponentId;
            @Schema(example = "11 April 2016")
            public String startDate;
        }

        @Schema(example = "tax group 1")
        public String name;
        @Schema(example = "en")
        public String locale;
        public Set<PostTaxesGroupTaxComponents> taxComponents;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @Schema(description = "PostTaxesGroupResponse")
    public static final class PostTaxesGroupResponse {

        private PostTaxesGroupResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutTaxesGroupTaxGroupIdRequest")
    public static final class PutTaxesGroupTaxGroupIdRequest {

        private PutTaxesGroupTaxGroupIdRequest() {}

        static final class PutTaxesGroupTaxComponents {

            private PutTaxesGroupTaxComponents() {}

            @Schema(example = "7")
            public Long id;
            @Schema(example = "7")
            public Long taxComponentId;
            @Schema(example = "22 April 2016")
            public String endDate;
        }

        @Schema(example = "tax group 2")
        public String name;
        @Schema(example = "en")
        public String locale;
        public Set<PutTaxesGroupTaxComponents> taxComponents;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
    }

    @Schema(description = "PutTaxesGroupTaxGroupIdResponse")
    public static final class PutTaxesGroupTaxGroupIdResponse {

        private PutTaxesGroupTaxGroupIdResponse() {}

        static final class PutTaxesGroupChanges {

            private PutTaxesGroupChanges() {}

            static final class PutTaxesGroupModifiedComponents {

                private PutTaxesGroupModifiedComponents() {}

                @Schema(example = "Apr 22, 2016 12:00:00 AM")
                public String endDate;
                @Schema(example = "7")
                public Integer taxComponentId;
            }

            @Schema(example = "[6]")
            public List<Integer> addComponents;
            public Set<PutTaxesGroupModifiedComponents> modifiedComponents;
            @Schema(example = "tax group 2")
            public String name;
        }

        @Schema(example = "7")
        public Integer resourceId;
        public PutTaxesGroupChanges changes;
    }
}
