/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.infrastructure.survey.data.LikelihoodStatus;

public class LikelihoodApiConstants {

    public static final String ACTIVE = "active";

    public static final String LIKELIHOOD_RESOURCE_NAME = "likelihood";

    public static final Set<String> UPDATE_LIKELIHOOD_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ACTIVE));

    public static final Set<Long> VALID_LIKELIHOOD_ENABLED_VALUES = new HashSet<>(Arrays.asList(LikelihoodStatus.DISABLED,
            LikelihoodStatus.ENABLED));
}
