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
package org.apache.fineract.portfolio.creditscorecard.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MLScorecardFields implements Serializable {

    @Column(name = "age")
    private Integer age;

    @Column(name = "sex")
    private String sex;

    @Column(name = "job")
    private String job;

    @Column(name = "housing")
    private String housing;

    @Column(name = "credit_amount", scale = 6, precision = 19)
    private BigDecimal creditAmount;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "purpose")
    private String purpose;

    public MLScorecardFields() {

    }

    public MLScorecardFields(Integer age, String sex, String job, String housing, BigDecimal creditAmount, Integer duration,
            String purpose) {
        this.age = age;
        this.sex = sex;
        this.job = job;
        this.housing = housing;
        this.creditAmount = creditAmount;
        this.duration = duration;
        this.purpose = purpose;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getHousing() {
        return housing;
    }

    public void setHousing(String housing) {
        this.housing = housing;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MLScorecardFields)) {
            return false;
        }
        MLScorecardFields that = (MLScorecardFields) o;
        return Objects.equals(age, that.age) && Objects.equals(sex, that.sex) && Objects.equals(job, that.job)
                && Objects.equals(housing, that.housing) && Objects.equals(creditAmount, that.creditAmount)
                && Objects.equals(duration, that.duration) && Objects.equals(purpose, that.purpose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, sex, job, housing, creditAmount, duration, purpose);
    }

    @Override
    public String toString() {
        return "MLScorecardFields{" + "age=" + age + ", sex='" + sex + '\'' + ", job='" + job + '\'' + ", housing='" + housing + '\''
                + ", creditAmount=" + creditAmount + ", duration=" + duration + ", purpose='" + purpose + '\'' + '}';
    }
}
