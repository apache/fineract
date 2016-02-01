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
package org.apache.fineract.portfolio.client.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class ClientEnumerations {

    public static EnumOptionData status(final Integer statusId) {
        return status(ClientStatus.fromInt(statusId));
    }

    public static EnumOptionData status(final ClientStatus status) {
        EnumOptionData optionData = new EnumOptionData(ClientStatus.INVALID.getValue().longValue(), ClientStatus.INVALID.getCode(),
                "Invalid");
        switch (status) {
            case INVALID:
                optionData = new EnumOptionData(ClientStatus.INVALID.getValue().longValue(), ClientStatus.INVALID.getCode(), "Invalid");
            break;
            case PENDING:
                optionData = new EnumOptionData(ClientStatus.PENDING.getValue().longValue(), ClientStatus.PENDING.getCode(), "Pending");
            break;
            case ACTIVE:
                optionData = new EnumOptionData(ClientStatus.ACTIVE.getValue().longValue(), ClientStatus.ACTIVE.getCode(), "Active");
            break;
            case CLOSED:
                optionData = new EnumOptionData(ClientStatus.CLOSED.getValue().longValue(), ClientStatus.CLOSED.getCode(), "Closed");
            break;
            case REJECTED:
                optionData = new EnumOptionData(ClientStatus.REJECTED.getValue().longValue(), ClientStatus.REJECTED.getCode(), "Rejected");
            break;
            case WITHDRAWN:
                optionData = new EnumOptionData(ClientStatus.WITHDRAWN.getValue().longValue(), ClientStatus.WITHDRAWN.getCode(),
                        "Withdrawn");
            break;
            case TRANSFER_IN_PROGRESS:
                optionData = new EnumOptionData(ClientStatus.TRANSFER_IN_PROGRESS.getValue().longValue(),
                        ClientStatus.TRANSFER_IN_PROGRESS.getCode(), "Transfer in progress");
            break;
            case TRANSFER_ON_HOLD:
                optionData = new EnumOptionData(ClientStatus.TRANSFER_ON_HOLD.getValue().longValue(),
                        ClientStatus.TRANSFER_ON_HOLD.getCode(), "Transfer on hold");
            break;
            default:
            break;
        }

        return optionData;
    }
    
    public static EnumOptionData legalForm(final Integer statusId) {
        return legalForm(LegalForm.fromInt(statusId));
    }
    
    public static EnumOptionData legalForm(final LegalForm legalForm) {
    	final EnumOptionData optionData = new EnumOptionData(legalForm.getValue().longValue(), legalForm.getCode(),
                legalForm.toString());
        return optionData;
    }
    
    public static List<EnumOptionData> legalForm(final LegalForm[] legalForms) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final LegalForm legalForm : legalForms) {
            optionDatas.add(legalForm(legalForm));
        }
        return optionDatas;
    }

    public static EnumOptionData clientTransactionType(final int id) {
        return clientTransactionType(ClientTransactionType.fromInt(id));
    }

    public static EnumOptionData clientTransactionType(final ClientTransactionType clientTransactionType) {
        final EnumOptionData optionData = new EnumOptionData(clientTransactionType.getValue().longValue(), clientTransactionType.getCode(),
                clientTransactionType.toString());
        return optionData;
    }

    public static List<EnumOptionData> clientTransactionType(final ClientTransactionType[] clientTransactionTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final ClientTransactionType clientTransaction : clientTransactionTypes) {
            optionDatas.add(clientTransactionType(clientTransaction));
        }
        return optionDatas;
    }

}