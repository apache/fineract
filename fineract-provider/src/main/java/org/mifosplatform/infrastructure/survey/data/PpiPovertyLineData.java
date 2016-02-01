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
public class PpiPovertyLineData {

    final String ppi;
    final List<LikeliHoodPovertyLineData> likeliHoodPovertyLineData;


    public PpiPovertyLineData(final List<LikeliHoodPovertyLineData> likeliHoodPovertyLineData,
                       final String ppi){

        this.likeliHoodPovertyLineData = likeliHoodPovertyLineData;
        this.ppi = ppi;

    }


}
