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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Column(name = "title")
    private String title;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "email", length = 50, unique = true)
    private String email;

    @Column(name = "mobile_number", length = 50, unique = true)
    private String mobileNumber;

    @Column(name = "alter_mobile_number")
    private String alterMobileNumber;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "street")
    private String streetNumberAndName;

    @Column(name = "city")
    private String city;

    @Column(name = "lga")
    private String lga;

    @ManyToOne
    @JoinColumn(name = "state_province_id")
    private CodeValue stateProvince;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CodeValue country;

    @Column(name = "date_of_birth", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "bvn")
    private String bvn;

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

    private ClientBusinessOwners(final Client client, final String firstName, final String title, final String lastName,
            final String email, final String mobileNumber, final String lga, final Boolean isActive, final Date dateOfBirth,
            final CodeValue stateProvince, final CodeValue country, final String bvn, final String username, final String alterMobileNumber,
            final String city, final String createdBy, final LocalDate createdOn, final String updatedBy, final LocalDate updatedOn,
            final String street) {

        this.client = client;
        this.firstName = firstName;
        this.title = title;
        this.lastName = lastName;
        this.email = email;
        this.isActive = isActive;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
        this.lga = lga;
        this.stateProvince = stateProvince;
        this.mobileNumber = mobileNumber;
        this.country = country;
        this.alterMobileNumber = alterMobileNumber;
        this.bvn = bvn;
        this.city = city;
        this.createdBy = createdBy;
        this.streetNumberAndName = street;
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

    public static ClientBusinessOwners fromJsonObject(final JsonObject jsonObject, final Client client, final CodeValue stateProvince,
            final CodeValue country) {
        String firstName = "";
        String title = "";
        String lastName = "";
        String email = "";
        Date dateOfBirth = null;
        String mobileNumber = "";
        String alterMobileNumber = "";
        String lga = null;
        Boolean isActive = false;
        String createdBy = "";
        String updatedBy = "";
        LocalDate updatedOnDate = null;
        LocalDate createdOnDate = null;
        String bvn = null;
        String username = null;

        if (jsonObject.get("firstName") != null) {
            firstName = jsonObject.get("firstName").getAsString();
        }

        if (jsonObject.get("title") != null) {
            title = jsonObject.get("title").getAsString();
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
        if (jsonObject.get("alterMobileNumber") != null) {
            alterMobileNumber = jsonObject.get("alterMobileNumber").getAsString();
        }

        if (jsonObject.get("lga") != null) {
            lga = jsonObject.get("lga").getAsString();
        }

        if (jsonObject.get("username") != null) {
            username = jsonObject.get("username").getAsString();
        }

        if (jsonObject.get("bvn") != null) {
            bvn = jsonObject.get("bvn").getAsString();
        }

        if (jsonObject.get("isActive") != null) {
            isActive = jsonObject.get("isActive").getAsBoolean();
        }

        if (jsonObject.get("dateOfBirth") != null) {

            DateFormat format = new SimpleDateFormat(jsonObject.get("dateFormat").getAsString());
            Date date;
            try {
                date = format.parse(jsonObject.get("dateOfBirth").getAsString());
                dateOfBirth = date;
            } catch (ParseException e) {
                System.out.println("Problem occurred in addClientFamilyMember function"+ e);
            }

            /*
             * this.fromApiJsonHelper.extractDateFormatParameter(member.get( "dateOfBirth").getAsJsonObject());
             */

        }
        String city = "";
        if (jsonObject.get("city") != null) {
            city = jsonObject.get("city").getAsString();
        }
        String streetNumberAndName = "";
        if (jsonObject.get("streetNumberAndName") != null) {
            streetNumberAndName = jsonObject.get("streetNumberAndName").getAsString();
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

        return new ClientBusinessOwners(client, firstName, title, lastName, email, mobileNumber, lga, isActive, dateOfBirth,
                stateProvince, country, bvn, username, alterMobileNumber, city, createdBy, createdOnDate, updatedBy, updatedOnDate,
                streetNumberAndName);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getAlterMobileNumber() {
        return alterMobileNumber;
    }

    public void setAlterMobileNumber(String alterMobileNumber) {
        this.alterMobileNumber = alterMobileNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getStreetNumberAndName() {
        return streetNumberAndName;
    }

    public void setStreetNumberAndName(String streetNumberAndName) {
        this.streetNumberAndName = streetNumberAndName;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
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

}
