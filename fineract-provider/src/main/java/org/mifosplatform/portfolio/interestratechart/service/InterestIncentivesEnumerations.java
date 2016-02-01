/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveAttributeName;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveEntityType;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveType;

public class InterestIncentivesEnumerations {

    public static EnumOptionData attributeName(final Integer attributeName) {
        return attributeName(InterestIncentiveAttributeName.fromInt(attributeName));
    }

    public static EnumOptionData attributeName(final InterestIncentiveAttributeName type) {
        EnumOptionData nameOptionData = new EnumOptionData(InterestIncentiveAttributeName.INVALID.getValue().longValue(),
                InterestIncentiveAttributeName.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case AGE:
                nameOptionData = new EnumOptionData(InterestIncentiveAttributeName.AGE.getValue().longValue(),
                        InterestIncentiveAttributeName.AGE.getCode(), "age");
            break;
            case GENDER:
                nameOptionData = new EnumOptionData(InterestIncentiveAttributeName.GENDER.getValue().longValue(),
                        InterestIncentiveAttributeName.GENDER.getCode(), "Gender");
            break;
            case CLIENT_TYPE:
                nameOptionData = new EnumOptionData(InterestIncentiveAttributeName.CLIENT_TYPE.getValue().longValue(),
                        InterestIncentiveAttributeName.CLIENT_TYPE.getCode(), "Client Type");
            break;
            case CLIENT_CLASSIFICATION:
                nameOptionData = new EnumOptionData(InterestIncentiveAttributeName.CLIENT_CLASSIFICATION.getValue().longValue(),
                        InterestIncentiveAttributeName.CLIENT_CLASSIFICATION.getCode(), "Client Classification");
            break;
        }

        return nameOptionData;
    }

    public static List<EnumOptionData> attributeName(final InterestIncentiveAttributeName[] attributeNames) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final InterestIncentiveAttributeName attributeName : attributeNames) {
            if (!attributeName.isInvalid()) {
                optionDatas.add(attributeName(attributeName));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData entityType(final Integer entityType) {
        return entityType(InterestIncentiveEntityType.fromInt(entityType));
    }

    public static EnumOptionData entityType(final InterestIncentiveEntityType type) {
        EnumOptionData nameOptionData = new EnumOptionData(InterestIncentiveEntityType.INVALID.getValue().longValue(),
                InterestIncentiveEntityType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case ACCOUNT:
                nameOptionData = new EnumOptionData(InterestIncentiveEntityType.ACCOUNT.getValue().longValue(),
                        InterestIncentiveEntityType.ACCOUNT.getCode(), "account");
            break;
            case CUSTOMER:
                nameOptionData = new EnumOptionData(InterestIncentiveEntityType.CUSTOMER.getValue().longValue(),
                        InterestIncentiveEntityType.CUSTOMER.getCode(), "Customer");
            break;
        }

        return nameOptionData;
    }

    public static List<EnumOptionData> entityType(final InterestIncentiveEntityType[] entityTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final InterestIncentiveEntityType entityType : entityTypes) {
            if (!entityType.isInvalid()) {
                optionDatas.add(entityType(entityType));
            }
        }
        return optionDatas;
    }

    public static EnumOptionData incentiveType(final Integer incentiveType) {
        return incentiveType(InterestIncentiveType.fromInt(incentiveType));
    }

    public static EnumOptionData incentiveType(final InterestIncentiveType type) {
        EnumOptionData nameOptionData = new EnumOptionData(InterestIncentiveType.INVALID.getValue().longValue(),
                InterestIncentiveType.INVALID.getCode(), "Invalid");

        switch (type) {
            case INVALID:
            break;
            case FIXED:
                nameOptionData = new EnumOptionData(InterestIncentiveType.FIXED.getValue().longValue(),
                        InterestIncentiveType.FIXED.getCode(), "Fixed");
            break;
            case INCENTIVE:
                nameOptionData = new EnumOptionData(InterestIncentiveType.INCENTIVE.getValue().longValue(),
                        InterestIncentiveType.INCENTIVE.getCode(), "Incentive");
            break;
        }

        return nameOptionData;
    }

    public static List<EnumOptionData> incentiveType(final InterestIncentiveType[] incentiveTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final InterestIncentiveType incentiveType : incentiveTypes) {
            if (!incentiveType.isInvalid()) {
                optionDatas.add(incentiveType(incentiveType));
            }
        }
        return optionDatas;
    }

}