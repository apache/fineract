/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.util;

import org.mifosplatform.spm.data.LookupTableData;
import org.mifosplatform.spm.domain.LookupTable;
import org.mifosplatform.spm.domain.Survey;

public class LookupTableMapper {

    private LookupTableMapper() {
        super();
    }

    public static LookupTableData map(final LookupTable lookupTable) {
        final LookupTableData lookupTableData = new LookupTableData(
                lookupTable.getId(), lookupTable.getKey(), lookupTable.getDescription(), lookupTable.getValueFrom(),
                lookupTable.getValueFrom(), lookupTable.getScore()
        );
        return lookupTableData;
    }

    public static LookupTable map(final LookupTableData lookupTableData, final Survey survey) {
        final LookupTable lookupTable = new LookupTable();
        lookupTable.setSurvey(survey);
        lookupTable.setKey(lookupTableData.getKey());
        lookupTable.setDescription(lookupTableData.getDescription());
        lookupTable.setValueFrom(lookupTableData.getValueFrom());
        lookupTable.setValueTo(lookupTableData.getValueTo());
        lookupTable.setScore(lookupTableData.getScore());
        return lookupTable;
    }
}
