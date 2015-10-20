/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface ProvisioningEntriesApiConstants {

    public final static String JSON_DATE_PARAM = "date" ;
    
    public final static String JSON_DATEFORMAT_PARAM = "dateFormat" ;
    
    public final static String JSON_LOCALE_PARAM = "locale" ;
    
    public final static String JSON_CREATEJOURNALENTRIES_PARAM = "createjournalentries" ;
    
    Set<String> supportedParameters = new HashSet<>(Arrays.asList(JSON_DATE_PARAM, JSON_DATEFORMAT_PARAM,JSON_LOCALE_PARAM,
            JSON_CREATEJOURNALENTRIES_PARAM));
    
    Set<String> PROVISIONING_ENTRY_PARAMETERS = new HashSet<>(Arrays.asList("provisioningentry", "entries"));

    Set<String> ALL_PROVISIONING_ENTRIES = new HashSet<>(Arrays.asList("provisioningentry"));

}
