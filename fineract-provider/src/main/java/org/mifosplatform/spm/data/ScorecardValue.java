/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.data;

public class ScorecardValue {

    private Long questionId;
    private Long responseId;
    private Integer value;

    public ScorecardValue() {
        super();
    }

    public ScorecardValue(final Long questionId, final Long responseId, final Integer value) {
        super();
        this.questionId = questionId;
        this.responseId = responseId;
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

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
