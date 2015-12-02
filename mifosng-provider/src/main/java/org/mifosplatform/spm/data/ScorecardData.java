/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.data;

import java.util.Date;

public class ScorecardData {

    private Long questionId;
    private Long responseId;
    private Long staffId;
    private Long clientId;
    private Date createdOn;
    private Integer value;

    public ScorecardData() {
        super();
    }

    public ScorecardData(final Long questionId, final Long responseId, final Long staffId, final Long clientId,
                         final Date createdOn, final Integer value) {
        super();
        this.questionId = questionId;
        this.responseId = responseId;
        this.staffId = staffId;
        this.clientId = clientId;
        this.createdOn = createdOn;
        this.value = value;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getResponseId() {
        return responseId;
    }

    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
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

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
