/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.data;

public class ResponseData {

    private Long id;
    private String text;
    private Integer value;
    private Integer sequenceNo;

    public ResponseData() {
        super();
    }

    public ResponseData(final Long id, final String text, final Integer value,
                        final Integer sequenceNo) {
        super();
        this.id = id;
        this.text = text;
        this.value = value;
        this.sequenceNo = sequenceNo;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(Integer sequenceNo) {
        this.sequenceNo = sequenceNo;
    }
}
