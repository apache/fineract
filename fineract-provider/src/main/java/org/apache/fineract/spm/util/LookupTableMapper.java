/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.util;

import org.mifosplatform.spm.data.LookupTableData;
import org.mifosplatform.spm.data.LookupTableEntry;
import org.mifosplatform.spm.domain.LookupTable;
import org.mifosplatform.spm.domain.Survey;

import java.util.*;

public class LookupTableMapper {

    private LookupTableMapper() {
        super();
    }

    public static List<LookupTableData> map(final List<LookupTable> lookupTables) {

        final Map<String, LookupTableData> lookupTableDataMap = new HashMap<>();
        LookupTableData lookupTableData = null;
        if (lookupTables != null && !lookupTables.isEmpty()) {
            for (LookupTable lookupTable : lookupTables) {
                if ((lookupTableData = lookupTableDataMap.get(lookupTable.getKey())) == null) {
                    lookupTableData = new LookupTableData();
                    lookupTableDataMap.put(lookupTable.getKey(), lookupTableData);
                    lookupTableData.setKey(lookupTable.getKey());
                    lookupTableData.setDescription(lookupTable.getDescription());
                    lookupTableData.setEntries(new ArrayList<LookupTableEntry>());
                }
                lookupTableData.getEntries().add(new LookupTableEntry(lookupTable.getValueFrom(),
                        lookupTable.getValueTo(), lookupTable.getScore()));
            }
            return new ArrayList<>(lookupTableDataMap.values());
        }

        return Collections.EMPTY_LIST;
    }

    public static List<LookupTable> map(final LookupTableData lookupTableData, final Survey survey) {
        final List<LookupTable> lookupTables = new ArrayList<>();

        final List<LookupTableEntry> entries = lookupTableData.getEntries();

        if (entries != null) {
            for (LookupTableEntry entry : entries) {
                final LookupTable lookupTable = new LookupTable();
                lookupTables.add(lookupTable);
                lookupTable.setSurvey(survey);
                lookupTable.setKey(lookupTableData.getKey());
                lookupTable.setDescription(lookupTableData.getDescription());
                lookupTable.setValueFrom(entry.getValueFrom());
                lookupTable.setValueTo(entry.getValueTo());
                lookupTable.setScore(entry.getScore());
            }
        }

        return lookupTables;
    }
}
