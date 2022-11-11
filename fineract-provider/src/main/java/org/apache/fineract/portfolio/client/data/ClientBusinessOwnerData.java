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

    private final String middleName;

    private final LocalDate dateOfBirth;

    private final Long titleId;

    private final String titleName;

    private final String lastName;

    private BigDecimal ownership;

    private final String mobileNumber;

    private final String businessOwnerNumber;

    private String email;

    private Long typeId;

    private String typeName;

    private Long cityId;

    private String cityName;

    private String streetNumberAndName;

    private String address1;

    private String address2;

    private String address3;

    private String postalCode;

    private String landmark;

    private String bvn;

    private String nin;

    private String username;

    private String city;

    private String lga;

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

    private final Boolean isActive;

    // template holder
    private final Collection<CodeValueData> countryIdOptions;
    private final Collection<CodeValueData> stateProvinceIdOptions;
    private final Collection<CodeValueData> cityIdOptions;
    private final Collection<CodeValueData> titleIdOptions;
    private final Collection<CodeValueData> typeIdOptions;

    private ClientBusinessOwnerData(final Long id, final Long clientId, final String firstName, final String middleName,
            final String titleName, final Long titleId, final String lastName, final BigDecimal ownership, final Long typeId,
            final String typeName, final Long cityId, final String cityName, final String mobileNumber, final String businessOwnerNumber,
            final Long stateProvinceId, final String stateName, final Long countryId, final String countryName, final LocalDate dateOfBirth,
            final String createdBy, final Date createdOn, final String updatedBy, final Date updatedOn, final String email,
            final String streetNumberAndName, final String address1, final String address2, final String address3, final String postalCode,
            String bvn, String nin, String landmark, final Collection<CodeValueData> countryIdOptions,
            final Collection<CodeValueData> stateProvinceIdOptions, final Collection<CodeValueData> cityIdOptions,
            final Collection<CodeValueData> titleIdOptions, final Collection<CodeValueData> typeIdOptions, final Long imageId,
            final Boolean isActive) {
        this.id = id;
        this.clientId = clientId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.titleName = titleName;
        this.titleId = titleId;
        this.lastName = lastName;
        this.ownership = ownership;
        this.email = email;
        this.stateProvinceId = stateProvinceId;
        this.stateName = stateName;
        this.typeId = typeId;
        this.typeName = typeName;
        this.cityId = cityId;
        this.cityName = cityName;
        this.countryId = countryId;
        this.countryName = countryName;
        this.dateOfBirth = dateOfBirth;
        this.createdBy = createdBy;
        this.mobileNumber = mobileNumber;
        this.createdOn = createdOn;
        this.updatedBy = updatedBy;
        this.updatedOn = updatedOn;
        this.streetNumberAndName = streetNumberAndName;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.postalCode = postalCode;
        this.bvn = bvn;
        this.nin = nin;
        this.landmark = landmark;
        this.businessOwnerNumber = businessOwnerNumber;
        this.stateProvinceIdOptions = stateProvinceIdOptions;
        this.countryIdOptions = countryIdOptions;
        this.cityIdOptions = cityIdOptions;
        this.titleIdOptions = titleIdOptions;
        this.typeIdOptions = typeIdOptions;
        this.imageId = imageId;
        this.isActive = isActive;
        if (imageId != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }

    }

    public static ClientBusinessOwnerData template(final Collection<CodeValueData> countryIdOptions,
            final Collection<CodeValueData> stateProvinceIdOptions, final Collection<CodeValueData> cityIdOptions,
            final Collection<CodeValueData> titleIdOptions, final Collection<CodeValueData> typeIdOptions) {
        final Long id = null;
        final Long clientId = null;
        final String firstName = null;
        final String middleName = null;
        final String titleName = null;
        final String lastName = null;
        final BigDecimal ownership = null;
        final String mobileNumber = null;
        final String businessOwnerNumber = null;
        String email = null;
        String streetNumberAndName = null;
        String address1 = null;
        String address2 = null;
        String address3 = null;
        String postalCode = null;
        String landmark = null;
        String bvn = null;
        String nin = null;
        LocalDate dateOfBirth = null;
        String createdBy = null;
        Date createdOn = null;
        String updatedBy = null;
        Date updatedOn = null;
        Long stateProvinceId = null;
        Long countryId = null;
        String countryName = null;
        String stateName = null;
        Long titleId = null;
        Long cityId = null;
        String cityName = null;
        Long typeId = null;
        String typeName = null;
        final Boolean isActive = null;

        return new ClientBusinessOwnerData(id, clientId, firstName, middleName, titleName, titleId, lastName, ownership, typeId, typeName,
                cityId, cityName, mobileNumber, businessOwnerNumber, stateProvinceId, stateName, countryId, countryName, dateOfBirth,
                createdBy, createdOn, updatedBy, updatedOn, email, streetNumberAndName, address1, address2, address3, postalCode, bvn, nin,
                landmark, countryIdOptions, stateProvinceIdOptions, cityIdOptions, titleIdOptions, typeIdOptions, null, isActive);
    }

    public static ClientBusinessOwnerData instance(final Long id, final Long clientId, final String firstName, final String middleName,
            final String titleName, final Long titleId, final String lastName, final BigDecimal ownership, final Long typeId,
            final String typeName, final Long cityId, final String cityName, final String mobileNumber, final String businessOwnerNumber,
            final Long stateProvinceId, final String stateName, final Long countryId, final String countryName, final LocalDate dateOfBirth,
            final String createdBy, final Date createdOn, final String updatedBy, final Date updatedOn, String email,
            String streetNumberAndName, String address1, String address2, String address3, String postalCode, String bvn, String nin,
            String landmark, final Collection<CodeValueData> countryIdOptions, final Collection<CodeValueData> stateProvinceIdOptions,
            final Collection<CodeValueData> cityIdOptions, final Collection<CodeValueData> titleIdOptions,
            final Collection<CodeValueData> typeIdOptions, final Long imageId, final Boolean isActive) {
        return new ClientBusinessOwnerData(id, clientId, firstName, middleName, titleName, titleId, lastName, ownership, typeId, typeName,
                cityId, cityName, mobileNumber, businessOwnerNumber, stateProvinceId, stateName, countryId, countryName, dateOfBirth,
                createdBy, createdOn, updatedBy, updatedOn, email, streetNumberAndName, address1, address2, address3, postalCode, bvn, nin,
                landmark, countryIdOptions, stateProvinceIdOptions, cityIdOptions, titleIdOptions, typeIdOptions, imageId, isActive);
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

    public String getlandmark() {
        return landmark;
    }

    public void setlandmark(String landmark) {
        this.landmark = landmark;
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

    public String getTitleName() {
        return titleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getBusinessOwnerNumber() {
        return businessOwnerNumber;
    }

    public BigDecimal getOwnership() {
        return ownership;
    }

    public long getTitleId() {
        return titleId;
    }

    public String getLandmark() {
        return landmark;
    }

    public String getNin() {
        return nin;
    }

    public Long getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public Long getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getAddress3() {
        return address3;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getMiddleName() {
        return middleName;
    }

}
