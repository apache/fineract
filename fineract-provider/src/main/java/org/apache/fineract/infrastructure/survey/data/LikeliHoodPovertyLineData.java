/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.data;

import java.util.List;

/**
 * Created by Cieyou on 3/11/14.
 */
public class LikeliHoodPovertyLineData {

    final long resourceId;
    final String likeliHoodName;
    final String likeliHoodCode;
    final long enabled;
    List<PovertyLineData> povertyLineData;

    public LikeliHoodPovertyLineData(final long resourceId, final List<PovertyLineData> povertyLineData, final String likeliHoodName,
            final String likeliHoodCode, final long enabled) {
        this.resourceId = resourceId;
        this.povertyLineData = povertyLineData;
        this.likeliHoodName = likeliHoodName;
        this.likeliHoodCode = likeliHoodCode;
        this.enabled = enabled;

    }

    public void addPovertyLine(PovertyLineData povertyLineData) {
        this.povertyLineData.add(povertyLineData);
    }

}
