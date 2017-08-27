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
package org.apache.fineract.commands.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.fineract.commands.data.ProcessingResultLookup;
import org.apache.fineract.useradministration.data.AppUserData;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

/**
 * Created by sanyam on 27/7/17.
 */
final class MakercheckersApiResourceSwagger {
    private MakercheckersApiResourceSwagger() {
        // only to initialize swagger documentation
    }

    @ApiModel(value = "GetMakerCheckerResponse")
    public static final class GetMakerCheckerResponse{
        private GetMakerCheckerResponse() {

        }
        public Long id;
        public String actionName;
        public String entityName;
        public Long resourceId;
        public Long subresourceId;
        public String maker;
        public DateTime madeOnDate;
        public String checker;
        public DateTime checkedOnDate;
        public String processingResult;
        public String commandAsJson;
        public String officeName;
        public String groupLevelName;
        public String groupName;
        public String clientName;
        public String loanAccountNo;
        public String savingsAccountNo;
        public Long clientId;
        public Long loanId;
        public String url;


    }

    @ApiModel(value = "GetMakerCheckersSearchTemplateResponse")
    public static final class GetMakerCheckersSearchTemplateResponse{
        private GetMakerCheckersSearchTemplateResponse() {

        }
        public Collection<AppUserData> appUsers;
        public List<String> actionNames;
        public List<String> entityNames;
        public Collection<ProcessingResultLookup> processingResults;
    }

    @ApiModel(value = "PostMakerCheckersResponse")
    public static final class PostMakerCheckersResponse{
        private PostMakerCheckersResponse() {

        }
        @ApiModelProperty(example = "1")
        public Long auditId;

    }
}
