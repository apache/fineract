/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.data;

import java.util.List;

public class LookupTableData {

    private String key;
    private String description;
    private List<LookupTableEntry> entries;

    public LookupTableData() {
        super();
    }

    public LookupTableData(final String key, final String description,
                           final List<LookupTableEntry> entries) {
        super();
        this.key = key;
        this.description = description;
        this.entries = entries;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LookupTableEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LookupTableEntry> entries) {
        this.entries = entries;
    }
}
