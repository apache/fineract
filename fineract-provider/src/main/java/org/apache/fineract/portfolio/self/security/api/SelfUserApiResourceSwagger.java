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

package org.apache.fineract.portfolio.self.security.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Chirag Gupta on 12/20/17.
 */
final class SelfUserApiResourceSwagger {
    private SelfUserApiResourceSwagger() {
    }

    @ApiModel(value = "PutSelfUserRequest")
    public final static class PutSelfUserRequest {
        private PutSelfUserRequest() {
        }

        @ApiModelProperty(example = "Abcd1234")
        public String password;
        @ApiModelProperty(example = "Abcd1234")
        public String repeatPassword;
    }

    @ApiModel(value = "PutSelfUserResponse")
    public final static class PutSelfUserResponse {
        private PutSelfUserResponse() {
        }

        final class PutSelfUserChanges {
            private PutSelfUserChanges() {
            }

            @ApiModelProperty(example = "6a72a630795be86fe926ce540fc45b6b922fe5ba130f185fe806a26b5e5efcdd")
            public String passwordEncoded;
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "6")
        public Integer resourceId;
        public PutSelfUserChanges changes;
    }
}
