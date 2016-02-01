/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.data;

/**
 * Created by Cieyou on 3/11/14.
 */
public class PovertyLineData {

    final Long resourceId;
    final Long scoreFrom;
    final Long scoreTo;
    final Double povertyLine;

    public PovertyLineData(final Long resourceId,
            final Long scoreFrom,
            final Long scoreTo,
            final Double povertyLine){

        this.resourceId = resourceId;
        this.scoreTo = scoreTo;
        this.scoreFrom = scoreFrom;
        this.povertyLine = povertyLine;
    }
}
