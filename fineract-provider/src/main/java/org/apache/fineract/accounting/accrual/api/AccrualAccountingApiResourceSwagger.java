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
package org.apache.fineract.accounting.accrual.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by sanyam on 24/7/17.
 */
final class AccrualAccountingApiResourceSwagger {
    private AccrualAccountingApiResourceSwagger() {
        // don't allow to instantiate; use only for live API documentation
    }

    @ApiModel(value = "runaccrualsRequest")
    public static final class PostRunaccrualsRequest {
        private PostRunaccrualsRequest() {
            // don't allow to instantiate; use only for live API documentation
        }

        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "04 June 2014", notes = "which specifies periodic accruals should happen till the given Date", required = true)
        public String tillDate;
    }

}
