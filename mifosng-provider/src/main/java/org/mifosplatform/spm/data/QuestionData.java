/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.data;

import java.util.List;

public class QuestionData {

    private List<ResponseData> responseDatas;
    private String componentKey;
    private String key;
    private String text;
    private String description;
    private Integer sequenceNo;

    public QuestionData() {
        super();
    }

    public QuestionData(final List<ResponseData> responseDatas, final String componentKey, final String key,
                        final String text, final String description, final Integer sequenceNo) {
        super();
        this.responseDatas = responseDatas;
        this.componentKey = componentKey;
        this.key = key;
        this.text = text;
        this.description = description;
        this.sequenceNo = sequenceNo;
    }

    public List<ResponseData> getResponseDatas() {
        return responseDatas;
    }

    public void setResponseDatas(List<ResponseData> responseDatas) {
        this.responseDatas = responseDatas;
    }

    public String getComponentKey() {
        return componentKey;
    }

    public void setComponentKey(String componentKey) {
        this.componentKey = componentKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(Integer sequenceNo) {
        this.sequenceNo = sequenceNo;
    }
}
