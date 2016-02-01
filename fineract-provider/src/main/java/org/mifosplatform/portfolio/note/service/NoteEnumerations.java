/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.note.domain.NoteType;

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
