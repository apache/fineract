/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanproduct.domain.LendingStrategy;

public class LendingStrategyEnumerations {

    public static EnumOptionData lendingStrategy(final Integer id) {
        return lendingStrategy(LendingStrategy.fromInt(id));
    }

    public static EnumOptionData lendingStrategy(final LendingStrategy type) {
        EnumOptionData optionData = null;
        switch (type) {
            case INDIVIDUAL_LOAN:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Individual loan");
            break;
            case GROUP_LOAN:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Group loan");
            break;
            case JOINT_LIABILITY_LOAN:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Joint liability loan");
            break;
            case LINKED_LOAN:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Linked loan");
            break;

            default:
                optionData = new EnumOptionData(LendingStrategy.INVALID.getId().longValue(), LendingStrategy.INVALID.getCode(), "Invalid");
            break;

        }
        return optionData;
    }

}
