/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.group.domain.GroupTypes;

public class GroupTypeEnumerations {

    public static EnumOptionData groupType(final Integer id) {
        return groupType(GroupTypes.fromInt(id));
    }

    public static EnumOptionData groupType(final GroupTypes type) {
        EnumOptionData optionData = null;
        switch (type) {
            case CENTER:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Individual loan");
            break;
            case GROUP:
                optionData = new EnumOptionData(type.getId().longValue(), type.getCode(), "Group loan");
            break;
            default:
                optionData = new EnumOptionData(GroupTypes.INVALID.getId().longValue(), GroupTypes.INVALID.getCode() , "Invalid");
            break;

        }
        return optionData;
    }

}
