/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.data;

import java.util.Date;
import java.util.List;

public class ScorecardData {

    private Long userId;
    private Long clientId;
    private Date createdOn;
    private List<ScorecardValue> scorecardValues;

    public ScorecardData() {
        super();
    }

    public ScorecardData(final Long userId, final Long clientId, final Date createdOn,
                         final List<ScorecardValue> scorecardValues) {
        super();
        this.userId = userId;
        this.clientId = clientId;
        this.createdOn = createdOn;
        this.scorecardValues = scorecardValues;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public List<ScorecardValue> getScorecardValues() {
        return scorecardValues;
    }

    public void setScorecardValues(List<ScorecardValue> scorecardValues) {
        this.scorecardValues = scorecardValues;
    }
}
