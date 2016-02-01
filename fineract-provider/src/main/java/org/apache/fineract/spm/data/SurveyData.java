/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.spm.data;

import java.util.Date;
import java.util.List;

public class SurveyData {

    private Long id;
    private List<ComponentData> componentDatas;
    private List<QuestionData> questionDatas;
    private String key;
    private String name;
    private String description;
    private String countryCode;
    private Date validFrom;
    private Date validTo;

    public SurveyData() {
        super();
    }

    public SurveyData(final Long id, final List<ComponentData> componentDatas, final List<QuestionData> questionDatas,
                      final String key, final String name, final String description, final String countryCode,
                      final Date validFrom, final Date validTo) {
        super();
        this.id = id;
        this.componentDatas = componentDatas;
        this.questionDatas = questionDatas;
        this.key = key;
        this.name = name;
        this.description = description;
        this.countryCode = countryCode;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ComponentData> getComponentDatas() {
        return componentDatas;
    }

    public void setComponentDatas(List<ComponentData> componentDatas) {
        this.componentDatas = componentDatas;
    }

    public List<QuestionData> getQuestionDatas() {
        return questionDatas;
    }

    public void setQuestionDatas(List<QuestionData> questionDatas) {
        this.questionDatas = questionDatas;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }
}
