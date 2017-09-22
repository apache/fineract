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
package org.apache.fineract.spm.domain;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "m_surveys")
public class Survey extends AbstractPersistableCustom<Long> {

    @OneToMany(mappedBy = "survey", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    @OrderBy("sequenceNo")
    private List<Component> components;

    @OneToMany(mappedBy = "survey", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    @OrderBy("sequenceNo")
    private List<Question> questions;

    @Column(name = "a_key", length = 32)
    private String key;

    @Column(name = "a_name", length = 255)
    private String name;

    @Column(name = "description", length = 4096)
    private String description;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "valid_from")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date validFrom;

    @Column(name = "valid_to")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date validTo;

    public Survey() {
        super();
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        if(this.questions != null){
            this.questions.clear();;
        }else{
            this.questions = new ArrayList<>();
        }
        
        this.questions.addAll(questions);
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
