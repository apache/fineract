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
package org.apache.fineract.portfolio.address.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;

public class AddressData {
	private final Long client_id;

	private final String addressType;

	private final Long addressId;

	private final Long addressTypeId;

	private final Boolean isActive;

	private final String street;

	private final String addressLine1;

	private final String addressLine2;

	private final String addressLine3;

	private final String townVillage;

	private final String city;

	private final String countyDistrict;

	private final Long stateProvinceId;

	private final String countryName;

	private final String stateName;

	private final Long countryId;

	private final String postalCode;

	private final BigDecimal latitude;

	private final BigDecimal longitude;

	private final String createdBy;

	private final Date createdOn;

	private final String updatedBy;

	private final Date updatedOn;

	// template holder
	private final Collection<CodeValueData> countryIdOptions;
	private final Collection<CodeValueData> stateProvinceIdOptions;
	private final Collection<CodeValueData> addressTypeIdOptions;

	public AddressData(Long addressTypeId,String street, String addressLine1, String addressLine2, String addressLine3,
			String city,String postalCode, Boolean isActive,Long stateProvinceId,Long countryId) {

		this.addressTypeId = addressTypeId;
		this.isActive = isActive;
		this.street = street;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.countryId = countryId;
		this.postalCode = postalCode;
		this.stateProvinceId = stateProvinceId;
		this.city = city;
		this.townVillage = null;
		this.client_id = null;
		this.addressType = null;
		this.addressId = null;
		this.countyDistrict = null;
		this.countryName = null;
		this.stateName = null;
		this.latitude = null;
		this.longitude = null;
		this.createdBy = null;
		this.createdOn = null;
		this.updatedBy = null;
		this.updatedOn = null;
		this.countryIdOptions = null;
		this.stateProvinceIdOptions = null;
		this.addressTypeIdOptions = null;
	}


	private AddressData(final String addressType, final Long client_id, final Long addressId, final Long addressTypeId,
			final Boolean is_active, final String street, final String addressLine1, final String addressLine2,
			final String addressLine3, final String townVillage, final String city, final String countyDistrict,
			final Long stateProvinceId, final Long countryId, final String stateName, final String countryName,
			final String postalCode, final BigDecimal latitude, final BigDecimal longitude, final String createdBy,
			final Date createdOn, final String updatedBy, final Date updatedOn,
			final Collection<CodeValueData> countryIdOptions, final Collection<CodeValueData> stateProvinceIdOptions,
			final Collection<CodeValueData> addressTypeIdOptions) {
		this.addressType = addressType;
		this.client_id = client_id;
		this.addressId = addressId;
		this.addressTypeId = addressTypeId;
		this.isActive = is_active;
		this.street = street;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.townVillage = townVillage;
		this.city = city;
		this.countyDistrict = countyDistrict;
		this.stateProvinceId = stateProvinceId;
		this.countryId = countryId;
		this.stateName = stateName;
		this.countryName = countryName;
		this.postalCode = postalCode;
		this.latitude = latitude;
		this.longitude = longitude;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
		this.updatedBy = updatedBy;
		this.updatedOn = updatedOn;
		this.countryIdOptions = countryIdOptions;
		this.stateProvinceIdOptions = stateProvinceIdOptions;
		this.addressTypeIdOptions = addressTypeIdOptions;
	}

	public static AddressData instance(final String addressType, final Long client_id, final Long addressId,
			final Long addressTypeId, final Boolean is_active, final String street, final String addressLine1,
			final String addressLine2, final String addressLine3, final String townVillage, final String city,
			final String countyDistrict, final Long stateProvinceId, final Long countryId, final String stateName,
			final String countryName, final String postalCode, final BigDecimal latitude, final BigDecimal longitude,
			final String createdBy, final Date createdOn, final String updatedBy, final Date updatedOn) {

		return new AddressData(addressType, client_id, addressId, addressTypeId, is_active, street, addressLine1,
				addressLine2, addressLine3, townVillage, city, countyDistrict, stateProvinceId, countryId,
				stateName, countryName, postalCode, latitude, longitude, createdBy, createdOn, updatedBy,
				updatedOn, null, null, null);
	}

	public static AddressData instance1(final Long addressId, final String street, final String addressLine1,
			final String addressLine2, final String addressLine3, final String townVillage, final String city,
			final String countyDistrict, final Long stateProvinceId, final Long countryId, final String postalCode,
			final BigDecimal latitude, final BigDecimal longitude, final String createdBy, final Date createdOn,
			final String updatedBy, final Date updatedOn) {
		return new AddressData(null, null, addressId, null, false, street, addressLine1, addressLine2,
				addressLine3, townVillage, city, countyDistrict, stateProvinceId, countryId, null, null,
				postalCode, latitude, longitude, createdBy, createdOn, updatedBy, updatedOn, null, null, null);
	}

	public static AddressData template(final Collection<CodeValueData> countryIdOptions,
			final Collection<CodeValueData> stateProvinceIdOptions,
			final Collection<CodeValueData> addressTypeIdOptions) {
		final Long client_idtemp = null;

		final Long addressIdtemp = null;

		final Long addressTypeIdtemp = null;

		final Boolean is_activetemp = null;

		final String streettemp = null;

		final String addressLine1temp = null;

		final String addressLine2temp = null;

		final String addressLine3temp = null;

		final String townVillagetemp = null;

		final String citytemp = null;

		final String countyDistricttemp = null;

		final Long stateProvinceIdtemp = null;

		final Long countryIdtemp = null;

		final String postalCodetemp = null;

		final BigDecimal latitudetemp = null;

		final BigDecimal longitudetemp = null;

		final String createdBytemp = null;

		final Date createdOntemp = null;

		final String updatedBytemp = null;

		final Date updatedOntemp = null;

		return new AddressData(null, client_idtemp, addressIdtemp, addressTypeIdtemp, is_activetemp, streettemp,
				addressLine1temp, addressLine2temp, addressLine3temp, townVillagetemp, citytemp,
				countyDistricttemp, stateProvinceIdtemp, countryIdtemp, null, null, postalCodetemp, latitudetemp,
				longitudetemp, createdBytemp, createdOntemp, updatedBytemp, updatedOntemp, countryIdOptions,
				stateProvinceIdOptions, addressTypeIdOptions);
	}

}
