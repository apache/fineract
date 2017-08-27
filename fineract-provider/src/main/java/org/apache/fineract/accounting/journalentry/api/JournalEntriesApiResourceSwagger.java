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
package org.apache.fineract.accounting.journalentry.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.LocalDate;

/**
 * Created by sanyam on 25/7/17.
 */
final class JournalEntriesApiResourceSwagger {
    private JournalEntriesApiResourceSwagger(){}

    @ApiModel(value = "PostJournalEntriesResponse")
    public static final class PostJournalEntriesResponse {

        private PostJournalEntriesResponse(){

        }
        @ApiModelProperty(value = "1")
        public Long officeId;
        @ApiModelProperty(value = "RS9MCISID4WK1ZM")
        public String transactionId;

    }

    @ApiModel(value = "PostJournalEntriesTransactionIdRequest")
    public static final class PostJournalEntriesTransactionIdRequest {
        private PostJournalEntriesTransactionIdRequest() {

        }
        @ApiModelProperty(value = "1")
        public Long officeId;
    }

    @ApiModel(value = "PostJournalEntriesTransactionIdResponse")
    public static final class PostJournalEntriesTransactionIdResponse {
        private PostJournalEntriesTransactionIdResponse() {

        }
        @ApiModelProperty(value = "1")
        public Long officeId;
    }
}
