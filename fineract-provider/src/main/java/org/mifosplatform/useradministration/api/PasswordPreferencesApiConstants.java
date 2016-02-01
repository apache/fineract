/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PasswordPreferencesApiConstants {

    public static final String RESOURCE_NAME = "passwordpreferences";
    public static final String ENTITY_NAME = "PASSWORD_PREFERENCES";

    // response parameters
    public static final String DESCRIPTION = "description";

    public static final String ACTIVE = "active";

    public static final String ID_PARAM_NAME = "id";

    // request parameters
    public static final String VALIDATION_POLICY_ID = "validationPolicyId";

    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(VALIDATION_POLICY_ID));

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ID_PARAM_NAME, ACTIVE, DESCRIPTION));

}
