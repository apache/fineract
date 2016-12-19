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
package org.apache.fineract.infrastructure.dataqueries.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public enum StatusEnum {

    CREATE("create", 100), APPROVE("approve", 200), ACTIVATE("activate", 300), WITHDRAWN("withdraw", 400), REJECTED("reject", 500), CLOSE(
            "close", 600), WRITE_OFF("write off", 601), RESCHEDULE("reschedule", 602), OVERPAY("overpay", 700), DISBURSE("disburse", 800);

    private String name;

    public Integer getCode() {
        return code;
    }

    private Integer code;

    private StatusEnum(String name, Integer code) {

        this.name = name;
        this.code = code;

    }

    public static List<DatatableCheckStatusData> getStatusList() {

        List<DatatableCheckStatusData> data = new ArrayList<DatatableCheckStatusData>();

        for (StatusEnum status : StatusEnum.values()) {
            data.add(new DatatableCheckStatusData(status.name, status.code));
        }

        return data;

    }

    public static StatusEnum fromInt(final Integer code) {
        StatusEnum ret = null;
        switch (code) {
            case 100:
                ret = StatusEnum.CREATE;
            break;
            case 200:
                ret = StatusEnum.APPROVE;
            break;
            case 300:
                ret = StatusEnum.ACTIVATE;
            break;
            case 400:
                ret = StatusEnum.WITHDRAWN;
            break;
            case 500:
                ret = StatusEnum.REJECTED;
            break;
            case 600:
                ret = StatusEnum.CLOSE;
            break;
            case 601:
                ret = StatusEnum.WRITE_OFF;
            break;
            case 602:
                ret = StatusEnum.RESCHEDULE;
            break;
            case 700:
                ret = StatusEnum.OVERPAY;
            break;
            case 800:
                ret = StatusEnum.DISBURSE;
            break;
            default:
            break;
        }
        return ret;
    }

    public static EnumOptionData statusTypeEnum(final Integer id) {
        return statusType(StatusEnum.fromInt(id));
    }

    public static EnumOptionData statusType(final StatusEnum statusType) {
        final EnumOptionData optionData = new EnumOptionData(statusType.getCode().longValue(), statusType.name(), statusType.name());
        return optionData;
    }

}
