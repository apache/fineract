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
package org.apache.fineract.spm.util;

import org.apache.fineract.spm.data.LookupTableData;
import org.apache.fineract.spm.data.LookupTableEntry;
import org.apache.fineract.spm.domain.LookupTable;
import org.apache.fineract.spm.domain.Survey;

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
