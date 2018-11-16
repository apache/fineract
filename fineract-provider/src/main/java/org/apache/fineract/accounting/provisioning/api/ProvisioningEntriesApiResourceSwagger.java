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
package org.apache.fineract.accounting.provisioning.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by sanyam on 25/7/17.
 */
final class ProvisioningEntriesApiResourceSwagger {
    private ProvisioningEntriesApiResourceSwagger() {
        // only for Swagger Documentation
    }

    @ApiModel(value = "PostProvisioningEntriesRequest")
    public static final class PostProvisioningEntriesRequest {
        private PostProvisioningEntriesRequest() {

        }
        @ApiModelProperty(example = "16 October 2015")
        public String date;

        @ApiModelProperty(example = "en")
        public String locale;

        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;

        @ApiModelProperty(example = "true")
        public String createjournalentries;

        public String provisioningentry;

        public String entries;
    }

    @ApiModel(value = "PostProvisioningEntriesResponse")
    public static final class PostProvisioningEntriesResponse{
        private PostProvisioningEntriesResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }

    @ApiModel(value = "PutProvisioningEntriesRequest")
    public static final class PutProvisioningEntriesRequest{
        private PutProvisioningEntriesRequest() {

        }
        @ApiModelProperty(example = "recreateprovisioningentry")
        public String command;
    }

    @ApiModel(value = "PutProvisioningEntriesResponse")
    public static final class PutProvisioningEntriesResponse{
        private PutProvisioningEntriesResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long resourceId;
    }
}
