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

package org.apache.fineract.portfolio.client.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

public final class ClientBusinessOwnerData implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Long id;

    private final Long clientId;

    private final String firstName;

    private final String title;

    private final String lastName;

    private BigDecimal ownership;

    private final LocalDate dateOfBirth;

    private final String mobileNumber;

    private final String alterMobileNumber;

    private String email;

    private String streetNumberAndName;

    private final Boolean isActive;

    private String username;

    private String city;

    private String lga;

    private String bvn;

    private String createdBy;

    private Date createdOn;

    private String updatedBy;

    private Date updatedOn;

    private Long stateProvinceId;

    private Long countryId;

    private String countryName;

    private String stateName;
    private final Long imageId;
    private final Boolean imagePresent;

    // template holder
    private final Collection<CodeValueData> countryIdOptions;
    private final Collection<CodeValueData> stateProvinceIdOptions;

    private ClientBusinessOwnerData(final Long id, final Long clientId, final String firstName, final String title,
            final String lastName, final BigDecimal ownership, final String username, final String mobileNumber, final String alterMobileNumber, final Boolean isActive,
            final String city, final Long stateProvinceId, final String stateName, final Long countryId, final String countryName,
            final LocalDate dateOfBirth, final String createdBy, final Date createdOn, final String updatedBy, final Date updatedOn,
            String email, String streetNumberAndName, String bvn, String lga, final Collection<CodeValueData> countryIdOptions,
            final Collection<CodeValueData> stateProvinceIdOptions, final Long imageId) {
        this.id = id;
        this.clientId = clientId;
        this.firstName = firstName;
        this.title = title;
        this.lastName = lastName;
        this.ownership = ownership;
        this.username = username;
        this.email = email;
        this.isActive = isActive;
        this.city = city;
        this.stateProvinceId = stateProvinceId;
        this.stateName = stateName;
        this.countryId = countryId;
        this.dateOfBirth = dateOfBirth;
        this.countryName = countryName;
        this.createdBy = createdBy;
        this.mobileNumber = mobileNumber;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
        this.streetNumberAndName = streetNumberAndName;
        this.bvn = bvn;
        this.lga = lga;
        this.alterMobileNumber = alterMobileNumber;
        this.stateProvinceIdOptions = stateProvinceIdOptions;
        this.countryIdOptions = countryIdOptions;
        this.imageId = imageId;
        if (imageId != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }

    }

    public static ClientBusinessOwnerData template(final Collection<CodeValueData> countryIdOptions,
            final Collection<CodeValueData> stateProvinceIdOptions) {
        final Long id = null;
        final Long clientId = null;
        final String firstName = null;
        final String title = null;
        final String lastName = null;
        final BigDecimal ownership = null;
        final LocalDate dateOfBirth = null;
        final String mobileNumber = null;
        final String alterMobileNumber = null;
        String email = null;
        String streetNumberAndName = null;
        final Boolean isActive = null;
        String username = null;
        String city = null;
        String lga = null;
        String bvn = null;
        String createdBy = null;
        Date createdOn = null;
        String updatedBy = null;
        Date updatedOn = null;
        Long stateProvinceId = null;
        Long countryId = null;
        String countryName = null;
        String stateName = null;

        return new ClientBusinessOwnerData(id, clientId, firstName, title, lastName, ownership, username, mobileNumber, alterMobileNumber,
                isActive, city, stateProvinceId, stateName, countryId, countryName, dateOfBirth, createdBy, createdOn, updatedBy, updatedOn,
                email, streetNumberAndName, bvn, lga, countryIdOptions, stateProvinceIdOptions, null);
    }

    public static ClientBusinessOwnerData instance(final Long id, final Long clientId, final String firstName, final String title,
            final String lastName, final BigDecimal ownership, final String username, final String mobileNumber, final String alterMobileNumber, final Boolean isActive,
            final String city, final Long stateProvinceId, final String stateName, final Long countryId, final String countryName,
            final LocalDate dateOfBirth, final String createdBy, final Date createdOn, final String updatedBy, final Date updatedOn,
            String email, String streetNumberAndName, String bvn, String lga, final Collection<CodeValueData> countryIdOptions,
            final Collection<CodeValueData> stateProvinceIdOptions, final Long imageId) {
        return new ClientBusinessOwnerData(id, clientId, firstName, title, lastName, ownership, username, mobileNumber, alterMobileNumber,
                isActive, city, stateProvinceId, stateName, countryId, countryName, dateOfBirth, createdBy, createdOn, updatedBy, updatedOn,
                email, streetNumberAndName, bvn, lga, countryIdOptions, stateProvinceIdOptions, imageId);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreetNumberAndName() {
        return streetNumberAndName;
    }

    public void setStreetNumberAndName(String streetNumberAndName) {
        this.streetNumberAndName = streetNumberAndName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLga() {
        return lga;
    }

    public void setLga(String lga) {
        this.lga = lga;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getStateProvinceId() {
        return stateProvinceId;
    }

    public void setStateProvinceId(Long stateProvinceId) {
        this.stateProvinceId = stateProvinceId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getTitle() {
        return title;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getAlterMobileNumber() {
        return alterMobileNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public BigDecimal getOwnership() {
        return ownership;
    }
}
