/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.survey.data.LikeliHoodPovertyLineData;
import org.mifosplatform.infrastructure.survey.data.PpiPovertyLineData;

public interface PovertyLineService {

    PpiPovertyLineData retrieveAll(final String ppiName);

    LikeliHoodPovertyLineData retrieveForLikelihood(final String ppiName, final Long likelihood);

}
