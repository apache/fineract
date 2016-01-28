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
package org.apache.fineract.portfolio.group.domain;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class GroupingTypeEnumerations {

    public static EnumOptionData status(final Integer statusId) {
        return status(GroupingTypeStatus.fromInt(statusId));
    }

    public static EnumOptionData status(final GroupingTypeStatus status) {
        EnumOptionData optionData = new EnumOptionData(GroupingTypeStatus.INVALID.getValue().longValue(),
                GroupingTypeStatus.INVALID.getCode(), "Invalid");
        switch (status) {
            case INVALID:
                optionData = new EnumOptionData(GroupingTypeStatus.INVALID.getValue().longValue(), GroupingTypeStatus.INVALID.getCode(),
                        "Invalid");
            break;
            case PENDING:
                optionData = new EnumOptionData(GroupingTypeStatus.PENDING.getValue().longValue(), GroupingTypeStatus.PENDING.getCode(),
                        "Pending");
            break;
            case ACTIVE:
                optionData = new EnumOptionData(GroupingTypeStatus.ACTIVE.getValue().longValue(), GroupingTypeStatus.ACTIVE.getCode(),
                        "Active");
            break;
            case CLOSED:
                optionData = new EnumOptionData(GroupingTypeStatus.CLOSED.getValue().longValue(), GroupingTypeStatus.CLOSED.getCode(),
                        "Closed");
            break;
            case TRANSFER_IN_PROGRESS:
                optionData = new EnumOptionData(GroupingTypeStatus.TRANSFER_IN_PROGRESS.getValue().longValue(),
                        GroupingTypeStatus.TRANSFER_IN_PROGRESS.getCode(), "Transfer in progress");
            break;
            case TRANSFER_ON_HOLD:
                optionData = new EnumOptionData(GroupingTypeStatus.TRANSFER_ON_HOLD.getValue().longValue(),
                        GroupingTypeStatus.TRANSFER_ON_HOLD.getCode(), "Transfer on hold");
            break;
        }

        return optionData;
    }
}