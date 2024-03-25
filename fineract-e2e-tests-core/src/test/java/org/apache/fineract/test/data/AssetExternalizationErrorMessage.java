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
package org.apache.fineract.test.data;

public enum AssetExternalizationErrorMessage {

    LOAN_NOT_ACTIVE("Loan is not in active status"), ASSET_OWNED_CANNOT_BE_SOLD(
            "This loan cannot be sold, because it is owned by an external asset owner"), ASSET_NOT_OWNED_CANNOT_BE_BOUGHT(
                    "This loan cannot be bought back, it is not owned by an external asset owner"), BUYBACK_ALREADY_IN_PROGRESS_CANNOT_BE_BOUGHT(
                            "This loan cannot be bought back, external asset owner buyback transfer is already in progress"), ALREADY_PENDING(
                                    "External asset owner transfer is already in PENDING state for this loan"), SETTLEMENT_DATE_IN_THE_PAST(
                                            "Settlement date cannot be in the past"), INVALID_REQUEST(
                                                    "The request was invalid. This typically will happen due to validation errors which are provided.");

    public final String value;

    AssetExternalizationErrorMessage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
