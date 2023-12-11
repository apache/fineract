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
package org.apache.fineract.portfolio.interestratechart.incentive;

import java.util.Arrays;

public enum InterestIncentiveAttributeName {

    INVALID(1, "InterestIncentiveAttributeName.invalid"), //
    GENDER(2, "InterestIncentiveAttributeName.gender"), //
    AGE(3, "InterestIncentiveAttributeName.age"), //
    CLIENT_TYPE(4, "InterestIncentiveAttributeName.clientType"), //
    CLIENT_CLASSIFICATION(5, "InterestIncentiveAttributeName.clientClassification"); //

    private final Integer value;
    private final String code;

    public static InterestIncentiveAttributeName fromInt(final Integer value) {
        switch (value) {
            case 2:
                return GENDER;
            case 3:
                return AGE;
            case 4:
                return CLIENT_TYPE;
            case 5:
                return CLIENT_CLASSIFICATION;
            default:
                return INVALID;
        }
    }

    InterestIncentiveAttributeName(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    @Override
    public String toString() {
        return name().toString().replaceAll("_", " ");
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isGender() {
        return GENDER.equals(this);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isAge() {
        return AGE.equals(this);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isClientType() {
        return CLIENT_TYPE.equals(this);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isClientClassification() {
        return CLIENT_CLASSIFICATION.equals(this);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public boolean isInvalid() {
        return INVALID.equals(this);
    }

    // TODO: why not just use the enum values... just more boilerplate code here!!
    public static boolean isCodeValueAttribute(InterestIncentiveAttributeName attributeName) {
        switch (attributeName) {
            case GENDER:
            case CLIENT_TYPE:
            case CLIENT_CLASSIFICATION:
                return true;
            default:
                return false;
        }
    }

    // TODO: do we really need this?!?
    public static Object[] integerValues() {
        return Arrays.stream(values()).filter(value -> !INVALID.equals(value)).map(value -> value.value).toList().toArray();
    }
}
