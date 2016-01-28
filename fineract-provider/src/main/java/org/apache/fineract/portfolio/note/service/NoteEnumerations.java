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
package org.apache.fineract.portfolio.note.service;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.note.domain.NoteType;

public class NoteEnumerations {

    public static EnumOptionData noteType(final Integer id) {
        return noteType(NoteType.fromInt(id));
    }

    public static EnumOptionData noteType(final NoteType type) {
        EnumOptionData optionData = null;
        switch (type) {
            case CLIENT:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Client note");
            break;
            case LOAN:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Loan note");
            break;
            case LOAN_TRANSACTION:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Loan transaction note");
            break;
            case SAVING_ACCOUNT:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Saving account note");
            break;
            case GROUP:
                optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Group note");
            break;
            default:
            break;

        }
        return optionData;
    }

}
