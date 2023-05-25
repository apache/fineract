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
package org.apache.fineract.portfolio.collateralmanagement.api;

public enum CollateralManagementJsonInputParams {

    NAME("name"), QUALITY("quality"), BASE_PRICE("basePrice"), UNIT_TYPE("unitType"), PCT_TO_BASE("pctToBase"), CURRENCY(
            "currency"), COLLATERAL_PRODUCT_READ_PERMISSION("COLLATERAL_PRODUCT"), CLIENT_COLLATERAL_PRODUCT_READ_PERMISSION(
                    "CLIENT_COLLATERAL_PRODUCT"), QUANTITY("quantity"), TOTAL_COLLATERAL_VALUE("totalCollateralValue");

    private final String value;

    CollateralManagementJsonInputParams(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name().replaceAll("_", " ");
    }

    public String getValue() {
        return this.value;
    }

}
