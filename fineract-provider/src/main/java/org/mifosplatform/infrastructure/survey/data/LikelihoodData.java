/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.data;

/**
 * Created by Cieyou on 3/12/14.
 */
public class LikelihoodData {

    final long resourceId;
    final String likeliHoodName;
    final String likeliHoodCode;
    final long enabled;

    public LikelihoodData(final long resourceId, final String likeliHoodName, final String likeliHoodCode, final long enabled) {
        this.resourceId = resourceId;
        this.likeliHoodName = likeliHoodName;
        this.likeliHoodCode = likeliHoodCode;
        this.enabled = enabled;

    }

}
