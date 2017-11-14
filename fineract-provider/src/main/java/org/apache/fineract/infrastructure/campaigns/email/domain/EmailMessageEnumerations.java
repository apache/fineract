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
package org.apache.fineract.infrastructure.campaigns.email.domain;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class EmailMessageEnumerations {

    public static EnumOptionData status(final Integer statusId) {
        return status(EmailMessageStatusType.fromInt(statusId));
    }

    public static EnumOptionData status(final EmailMessageStatusType status) {
        EnumOptionData optionData = new EnumOptionData(EmailMessageStatusType.INVALID.getValue().longValue(),
                EmailMessageStatusType.INVALID.getCode(), "Invalid");
        switch (status) {
            case INVALID:
                optionData = new EnumOptionData(EmailMessageStatusType.INVALID.getValue().longValue(),
                        EmailMessageStatusType.INVALID.getCode(), "Invalid");
            break;
            case PENDING:
                optionData = new EnumOptionData(EmailMessageStatusType.PENDING.getValue().longValue(),
                        EmailMessageStatusType.PENDING.getCode(), "Pending");
            break;
            case SENT:
                optionData = new EnumOptionData(EmailMessageStatusType.SENT.getValue().longValue(), EmailMessageStatusType.SENT.getCode(),
                        "Sent");
            break;
            case DELIVERED:
                optionData = new EnumOptionData(EmailMessageStatusType.DELIVERED.getValue().longValue(),
                        EmailMessageStatusType.DELIVERED.getCode(), "Delivered");
            break;
            case FAILED:
                optionData = new EnumOptionData(EmailMessageStatusType.FAILED.getValue().longValue(), EmailMessageStatusType.FAILED.getCode(),
                        "Failed");
            break;

        }

        return optionData;
    }
}