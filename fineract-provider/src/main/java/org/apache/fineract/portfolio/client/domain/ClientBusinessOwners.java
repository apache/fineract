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

package org.apache.fineract.portfolio.client.domain;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.documentmanagement.domain.Image;

@Entity
@Table(name = "m_business_owners")
public class ClientBusinessOwners extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "ownership", scale = 2, precision = 5, nullable = true)
    private BigDecimal ownership;

    @ManyToOne
    @JoinColumn(name = "title_id")
    private CodeValue title;

    @Column(name = "bvn")
    private String bvn;

    @Column(name = "nin")
    private String nin;

    @Column(name = "middlename")
    private String middleName;

    @Column(name = "mobile_number", length = 50, unique = true)
    private String mobileNumber;

    @Column(name = "email", length = 50, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private CodeValue type;

    @Column(name = "business_owner_number")
    private String businessOwnerNumber;

    @Column(name = "alter_mobile_number")
    private String alterMobileNumber;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "street")
    private String streetNumberAndName;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "address3")
    private String address3;

    @Column(name = "landmark")
    private String landmark;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private CodeValue city;

    @Column(name = "lga")
    private String lga;

    @ManyToOne
    @JoinColumn(name = "state_province_id")
    private CodeValue stateProvince;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CodeValue country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "date_of_birth", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on")
    private Date createdOn;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_on")
    private Date updatedOn;

    @OneToOne(optional = true)
    @JoinColumn(name = "image_id", nullable = true)
    private Image image;

    @Column(name = "is_active")
    private Boolean isActive;

    private ClientBusinessOwners(final Client client, final String firstName, final CodeValue title, final String lastName,
            final BigDecimal ownership, final String email, final String mobileNumber, final String landmark, final CodeValue type,
            final CodeValue city, final CodeValue stateProvince, final CodeValue country, final String bvn, final String nin,
            final String businessOwnerNumber, final String createdBy, final LocalDate createdOn, final String updatedBy,
            final LocalDate updatedOn, final String street, final String address1, final String address2, final String address3,
            final String postalCode, final Boolean isActive) {

        this.client = client;
        this.firstName = firstName;
        this.title = title;
        this.lastName = lastName;
        this.ownership = ownership;
        this.email = email;
        this.landmark = landmark;
        this.type = type;
        this.city = city;
        this.stateProvince = stateProvince;
        this.mobileNumber = mobileNumber;
        this.country = country;
        this.businessOwnerNumber = businessOwnerNumber;
        this.bvn = bvn;
        this.nin = nin;
        this.createdBy = createdBy;
        this.streetNumberAndName = street;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.postalCode = postalCode;
        this.isActive = isActive;

        this.updatedBy = updatedBy;
        if (createdOn != null) {
            this.createdOn = Date.from(createdOn.atStartOfDay(ZoneId.systemDefault()).toInstant());

        }
        if (updatedOn != null) {
            this.updatedOn = Date.from(updatedOn.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
    }

    public ClientBusinessOwners() {

    }

    public static ClientBusinessOwners fromJsonObject(final JsonObject jsonObject, final Client client, final CodeValue title,
            final CodeValue type, final CodeValue city, final CodeValue stateProvince, final CodeValue country) {
        String firstName = "";
        String lastName = "";
        BigDecimal ownership = null;
        String email = "";
        String mobileNumber = "";
        String businessOwnerNumber = "";
        String landmark = null;
        String createdBy = "";
        String updatedBy = "";
        LocalDate updatedOnDate = null;
        LocalDate createdOnDate = null;
        String bvn = null;
        String nin = null;
        Boolean isActive = false;

        if (jsonObject.get("firstName") != null) {
            firstName = jsonObject.get("firstName").getAsString();
        }

        if (jsonObject.get("lastName") != null) {
            lastName = jsonObject.get("lastName").getAsString();
        }

        if (jsonObject.get("email") != null) {
            email = jsonObject.get("email").getAsString();
        }

        if (jsonObject.get("mobileNumber") != null) {
            mobileNumber = jsonObject.get("mobileNumber").getAsString();
        }
        if (jsonObject.get("businessOwnerNumber") != null) {
            businessOwnerNumber = jsonObject.get("businessOwnerNumber").getAsString();
        }

        if (jsonObject.get("landmark") != null) {
            landmark = jsonObject.get("landmark").getAsString();
        }

        if (jsonObject.get("bvn") != null) {
            bvn = jsonObject.get("bvn").getAsString();
        }

        if (jsonObject.get("nin") != null) {
            nin = jsonObject.get("nin").getAsString();
        }

        String streetNumberAndName = "";
        if (jsonObject.get("streetNumberAndName") != null) {
            streetNumberAndName = jsonObject.get("streetNumberAndName").getAsString();
        }

        String address1 = "";
        if (jsonObject.get("address1") != null) {
            address1 = jsonObject.get("address1").getAsString();
        }

        String address2 = "";
        if (jsonObject.get("address2") != null) {
            address2 = jsonObject.get("address2").getAsString();
        }

        String address3 = "";
        if (jsonObject.get("address3") != null) {
            address3 = jsonObject.get("address3").getAsString();
        }

        String postalCode = "";
        if (jsonObject.get("postalCode") != null) {
            postalCode = jsonObject.get("postalCode").getAsString();
        }

        if (jsonObject.has("createdBy")) {
            createdBy = jsonObject.get("createdBy").getAsString();
        }
        if (jsonObject.has("createdOn")) {
            String createdOn = jsonObject.get("createdOn").getAsString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            createdOnDate = LocalDate.parse(createdOn, formatter);

        }
        if (jsonObject.has("updatedBy")) {
            updatedBy = jsonObject.get("updatedBy").getAsString();
        }
        if (jsonObject.has("updatedOn")) {
            String updatedOn = jsonObject.get("updatedOn").getAsString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            updatedOnDate = LocalDate.parse(updatedOn, formatter);
        }

        if (jsonObject.get("ownership") != null) {
            ownership = jsonObject.get("ownership").getAsBigDecimal();
        }
        if (jsonObject.get("isActive") != null) {
            isActive = jsonObject.get("isActive").getAsBoolean();
        }

        return new ClientBusinessOwners(client, firstName, title, lastName, ownership, email, mobileNumber, landmark, type, city,
                stateProvince, country, bvn, nin, businessOwnerNumber, createdBy, createdOnDate, updatedBy, updatedOnDate,
                streetNumberAndName, address1, address2, address3, postalCode, isActive);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public CodeValue getTitle() {
        return title;
    }

    public void setTitle(CodeValue title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getBusinessOwnerNumber() {
        return businessOwnerNumber;
    }

    public void setBusinessOwnerNumber(String businessOwnerNumber) {
        this.businessOwnerNumber = businessOwnerNumber;
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

    public CodeValue getType() {
        return type;
    }

    public void setType(CodeValue type) {
        this.type = type;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public CodeValue getCity() {
        return city;
    }

    public void setCity(CodeValue city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public CodeValue getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(CodeValue stateProvince) {
        this.stateProvince = stateProvince;
    }

    public CodeValue getCountry() {
        return country;
    }

    public void setCountry(CodeValue country) {
        this.country = country;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getNin() {
        return nin;
    }

    public void setNin(String nin) {
        this.nin = nin;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(LocalDate createdOn) {
        this.createdOn = Date.from(createdOn.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedOn() {
        return this.updatedOn;
    }

    public void setUpdatedOn(LocalDate updatedOn) {
        this.updatedOn = Date.from(updatedOn.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public BigDecimal getOwnership() {
        return ownership;
    }

    public void setOwnership(BigDecimal ownership) {
        this.ownership = ownership;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setAlterMobileNumber(String alterMobileNumber) {
        this.alterMobileNumber = alterMobileNumber;
    }

    public void setLga(String lga) {
        this.lga = lga;
    }
}
