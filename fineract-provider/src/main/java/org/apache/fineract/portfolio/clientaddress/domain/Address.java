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
package org.apache.fineract.portfolio.clientaddress.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonObject;

@Entity
@Table(name = "m_address")
public class Address extends AbstractPersistable<Long> {

	@OneToMany(mappedBy = "address", cascade = CascadeType.ALL)
	private List<ClientAddress> clientaddress = new ArrayList<>();

	private String street;

	private String address_line_1;

	private String address_line_2;

	private String address_line_3;

	private String town_village;

	private String city;

	private String county_district;

	@ManyToOne
	private CodeValue state_province;

	@ManyToOne
	private CodeValue country;

	private String postal_code;

	private BigDecimal latitude;

	private BigDecimal longitude;

	private String created_by;

	private Date created_on;

	private String updated_by;

	private Date updated_on;

	private Address(final String street, final String address_line_1, final String address_line_2,
			final String address_line_3, final String town_village, final String city, final String county_district,
			final CodeValue state_province, final CodeValue country, final String postal_code,
			final BigDecimal latitude, final BigDecimal longitude, final String created_by, final Date created_on,
			final String updated_by, final Date updated_on) {
		this.street = street;
		this.address_line_1 = address_line_1;
		this.address_line_2 = address_line_2;
		this.address_line_3 = address_line_3;
		this.town_village = town_village;
		this.city = city;
		this.county_district = county_district;
		this.state_province = state_province;
		this.country = country;
		this.postal_code = postal_code;
		this.latitude = latitude;
		this.longitude = longitude;
		this.created_by = created_by;
		this.created_on = created_on;
		this.updated_by = updated_by;
		this.updated_on = updated_on;

	}

	public Address() {

	}

	public static Address fromJson(final JsonCommand command, final CodeValue state_province, final CodeValue country) {

		final String street = command.stringValueOfParameterNamed("street");

		final String address_line_1 = command.stringValueOfParameterNamed("address_line_1");

		final String address_line_2 = command.stringValueOfParameterNamed("address_line_2");

		final String address_line_3 = command.stringValueOfParameterNamed("address_line_3");

		final String town_village = command.stringValueOfParameterNamed("town_village");

		final String city = command.stringValueOfParameterNamed("city");

		final String county_district = command.stringValueOfParameterNamed("county_district");

		final String postal_code = command.stringValueOfParameterNamed("postal_code");

		final BigDecimal latitude = command.bigDecimalValueOfParameterNamed("latitude");

		final BigDecimal longitude = command.bigDecimalValueOfParameterNamed("longitude");

		final String created_by = command.stringValueOfParameterNamed("created_by");

		final Date created_on = command.DateValueOfParameterNamed("created_on");

		final String updated_by = command.stringValueOfParameterNamed("updated_by");

		final Date updated_on = command.DateValueOfParameterNamed("updated_on");

		return new Address(street, address_line_1, address_line_2, address_line_3, town_village, city, county_district,
				state_province, country, postal_code, latitude, longitude, created_by, created_on, updated_by,
				updated_on);
	}

	public static Address fromJsonObject(final JsonObject jsonObject, final CodeValue state_province,
			final CodeValue country) {
		String street = "";
		String address_line_1 = "";
		String address_line_2 = "";
		String address_line_3 = "";
		String town_village = "";
		String city = "";
		String county_district = "";
		String postal_code = "";
		BigDecimal latitude = BigDecimal.ZERO;
		BigDecimal longitude = BigDecimal.ZERO;

		if (jsonObject.has("street")) {
			street = jsonObject.get("street").getAsString();

		}

		if (jsonObject.has("address_line_1")) {
			address_line_1 = jsonObject.get("address_line_1").getAsString();
		}
		if (jsonObject.has("address_line_2")) {

			address_line_2 = jsonObject.get("address_line_2").getAsString();
		}
		if (jsonObject.has("address_line_3")) {
			address_line_3 = jsonObject.get("address_line_3").getAsString();
		}
		if (jsonObject.has("town_village")) {
			town_village = jsonObject.get("town_village").getAsString();
		}
		if (jsonObject.has("city")) {
			city = jsonObject.get("city").getAsString();
		}
		if (jsonObject.has("county_district")) {
			county_district = jsonObject.get("county_district").getAsString();
		}
		if (jsonObject.has("postal_code")) {

			postal_code = jsonObject.get("postal_code").getAsString();
		}
		if (jsonObject.has("latitude")) {

			latitude = jsonObject.get("latitude").getAsBigDecimal();
		}
		if (jsonObject.has("longitude")) {

			longitude = jsonObject.get("longitude").getAsBigDecimal();
		}

		/*
		 * final String created_by=jsonObject.get("created_by").getAsString();
		 *
		 * final Date created_on=jsonObject.get("created_on").;
		 *
		 * final String updated_by=jsonObject.get("updated_by");
		 *
		 * final Date updated_on=jsonObject.get("updated_on");
		 */

		return new Address(street, address_line_1, address_line_2, address_line_3, town_village, city, county_district,
				state_province, country, postal_code, latitude, longitude, null, null, null, null);
	}

	public List<ClientAddress> getClientaddress() {
		return this.clientaddress;
	}

	public String getStreet() {
		return this.street;
	}

	public String getAddress_line_1() {
		return this.address_line_1;
	}

	public String getAddress_line_2() {
		return this.address_line_2;
	}

	public String getAddress_line_3() {
		return this.address_line_3;
	}

	public String getTown_village() {
		return this.town_village;
	}

	public String getCity() {
		return this.city;
	}

	public String getCounty_district() {
		return this.county_district;
	}

	public CodeValue getState_province() {
		return this.state_province;
	}

	public CodeValue getCountry() {
		return this.country;
	}

	public String getPostal_code() {
		return this.postal_code;
	}

	public BigDecimal getLatitude() {
		return this.latitude;
	}

	public BigDecimal getLongitude() {
		return this.longitude;
	}

	public String getCreated_by() {
		return this.created_by;
	}

	public Date getCreated_on() {
		return this.created_on;
	}

	public String getUpdated_by() {
		return this.updated_by;
	}

	public Date getUpdated_on() {
		return this.updated_on;
	}

	public void setClientaddress(final List<ClientAddress> clientaddress) {
		this.clientaddress = clientaddress;
	}

	public void setStreet(final String street) {
		this.street = street;
	}

	public void setAddress_line_1(final String address_line_1) {
		this.address_line_1 = address_line_1;
	}

	public void setAddress_line_2(final String address_line_2) {
		this.address_line_2 = address_line_2;
	}

	public void setAddress_line_3(final String address_line_3) {
		this.address_line_3 = address_line_3;
	}

	public void setTown_village(final String town_village) {
		this.town_village = town_village;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public void setCounty_district(final String county_district) {
		this.county_district = county_district;
	}

	public void setState_province(final CodeValue state_province) {
		this.state_province = state_province;
	}

	public void setCountry(final CodeValue country) {
		this.country = country;
	}

	public void setPostal_code(final String postal_code) {
		this.postal_code = postal_code;
	}

	public void setLatitude(final BigDecimal latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(final BigDecimal longitude) {
		this.longitude = longitude;
	}

	public void setCreated_by(final String created_by) {
		this.created_by = created_by;
	}

	public void setCreated_on(final Date created_on) {
		this.created_on = created_on;
	}

	public void setUpdated_by(final String updated_by) {
		this.updated_by = updated_by;
	}

	public void setUpdated_on(final Date updated_on) {
		this.updated_on = updated_on;
	}

}
