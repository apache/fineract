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
package org.apache.fineract.portfolio.address.service;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.domain.Address;
import org.apache.fineract.portfolio.address.domain.AddressRepository;
import org.apache.fineract.portfolio.address.serialization.AddressCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientAddress;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class AddressWritePlatformServiceImpl implements AddressWritePlatformService {
	private final PlatformSecurityContext context;
	private final CodeValueRepository codeValueRepository;
	private final ClientAddressRepository clientAddressRepository;
	private final ClientRepositoryWrapper clientRepositoryWrapper;
	private final AddressRepository addressRepository;
	private final ClientAddressRepositoryWrapper clientAddressRepositoryWrapper;
	private final AddressCommandFromApiJsonDeserializer fromApiJsonDeserializer;

	@Autowired
	public AddressWritePlatformServiceImpl(final PlatformSecurityContext context,
			final CodeValueRepository codeValueRepository, final ClientAddressRepository clientAddressRepository,
			final ClientRepositoryWrapper clientRepositoryWrapper, final AddressRepository addressRepository,
			final ClientAddressRepositoryWrapper clientAddressRepositoryWrapper,
			final AddressCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
		this.context = context;
		this.codeValueRepository = codeValueRepository;
		this.clientAddressRepository = clientAddressRepository;
		this.clientRepositoryWrapper = clientRepositoryWrapper;
		this.addressRepository = addressRepository;
		this.clientAddressRepositoryWrapper = clientAddressRepositoryWrapper;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;

	}

	@Override
	public CommandProcessingResult addClientAddress(final Long clientId, final Long addressTypeId,
			final JsonCommand command) {
		CodeValue stateIdobj = null;
		CodeValue countryIdObj = null;
		long stateId;
		long countryId;

		this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForCreate(command.json(), true);

		if (command.longValueOfParameterNamed("stateProvinceId") != null) {
			stateId = command.longValueOfParameterNamed("stateProvinceId");
			stateIdobj = this.codeValueRepository.getOne(stateId);
		}

		if (command.longValueOfParameterNamed("countryId") != null) {
			countryId = command.longValueOfParameterNamed("countryId");
			countryIdObj = this.codeValueRepository.getOne(countryId);
		}

		final CodeValue addressTypeIdObj = this.codeValueRepository.getOne(addressTypeId);

		final Address add = Address.fromJson(command, stateIdobj, countryIdObj);
		this.addressRepository.save(add);
		final Long addressid = add.getId();
		final Address addobj = this.addressRepository.getOne(addressid);

		final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
		final boolean isActive = command.booleanPrimitiveValueOfParameterNamed("isActive");

		final ClientAddress clientAddressobj = ClientAddress.fromJson(isActive, client, addobj, addressTypeIdObj);
		this.clientAddressRepository.save(clientAddressobj);

		return new CommandProcessingResultBuilder().withCommandId(command.commandId())
				.withEntityId(clientAddressobj.getId()).build();
	}

	// following method is used for adding multiple addresses while creating new
	// client

	@Override
	public CommandProcessingResult addNewClientAddress(final Client client, final JsonCommand command) {
		CodeValue stateIdobj = null;
		CodeValue countryIdObj = null;
		long stateId;
		long countryId;
		ClientAddress clientAddressobj = new ClientAddress();
		final JsonArray addressArray = command.arrayOfParameterNamed("address");
		
		if(addressArray != null){
			for (int i = 0; i < addressArray.size(); i++) {
				final JsonObject jsonObject = addressArray.get(i).getAsJsonObject();

				// validate every address
				this.fromApiJsonDeserializer.validateForCreate(jsonObject.toString(), true);

				if (jsonObject.get("stateProvinceId") != null) {
					stateId = jsonObject.get("stateProvinceId").getAsLong();
					stateIdobj = this.codeValueRepository.getOne(stateId);
				}

				if (jsonObject.get("countryId") != null) {
					countryId = jsonObject.get("countryId").getAsLong();
					countryIdObj = this.codeValueRepository.getOne(countryId);
				}

				final long addressTypeId = jsonObject.get("addressTypeId").getAsLong();
				final CodeValue addressTypeIdObj = this.codeValueRepository.getOne(addressTypeId);

				final Address add = Address.fromJsonObject(jsonObject, stateIdobj, countryIdObj);
				this.addressRepository.save(add);
				final Long addressid = add.getId();
				final Address addobj = this.addressRepository.getOne(addressid);

				//final boolean isActive = jsonObject.get("isActive").getAsBoolean();
				boolean isActive=false;
				if(jsonObject.get("isActive")!= null)
				{
					isActive= jsonObject.get("isActive").getAsBoolean();
				}
				

				clientAddressobj = ClientAddress.fromJson(isActive, client, addobj, addressTypeIdObj);
				this.clientAddressRepository.save(clientAddressobj);

			}
		}

		return new CommandProcessingResultBuilder().withCommandId(command.commandId())
				.withEntityId(clientAddressobj.getId()).build();
	}

	@Override
	public CommandProcessingResult updateClientAddress(final Long clientId, final JsonCommand command) {
		this.context.authenticatedUser();

		long stateId;

		long countryId;

		CodeValue stateIdobj;

		CodeValue countryIdObj;

		boolean is_address_update = false;

		this.fromApiJsonDeserializer.validateForUpdate(command.json());

		final long addressId = command.longValueOfParameterNamed("addressId");

		final ClientAddress clientAddressObj = this.clientAddressRepositoryWrapper
				.findOneByClientIdAndAddressId(clientId, addressId);

		final Address addobj = this.addressRepository.getOne(addressId);

		if (!(command.stringValueOfParameterNamed("street").isEmpty())) {

			is_address_update = true;
			final String street = command.stringValueOfParameterNamed("street");
			addobj.setStreet(street);
		}

		if (!(command.stringValueOfParameterNamed("addressLine1").isEmpty())) {

			is_address_update = true;
			final String addressLine1 = command.stringValueOfParameterNamed("addressLine1");
			addobj.setAddressLine1(addressLine1);

		}

		if (!(command.stringValueOfParameterNamed("addressLine2").isEmpty())) {

			is_address_update = true;
			final String addressLine2 = command.stringValueOfParameterNamed("addressLine2");
			addobj.setAddressLine2(addressLine2);

		}

		if (!(command.stringValueOfParameterNamed("addressLine3").isEmpty())) {
			is_address_update = true;
			final String addressLine3 = command.stringValueOfParameterNamed("addressLine3");
			addobj.setAddressLine3(addressLine3);

		}

		if (!(command.stringValueOfParameterNamed("townVillage").isEmpty())) {

			is_address_update = true;
			final String townVillage = command.stringValueOfParameterNamed("townVillage");
			addobj.setTownVillage(townVillage);
		}

		if (!(command.stringValueOfParameterNamed("city").isEmpty())) {
			is_address_update = true;
			final String city = command.stringValueOfParameterNamed("city");
			addobj.setCity(city);
		}

		if (!(command.stringValueOfParameterNamed("countyDistrict").isEmpty())) {
			is_address_update = true;
			final String countyDistrict = command.stringValueOfParameterNamed("countyDistrict");
			addobj.setCountyDistrict(countyDistrict);
		}

		if ((command.longValueOfParameterNamed("stateProvinceId") != null)) {
			if ((command.longValueOfParameterNamed("stateProvinceId") != 0)) {
				is_address_update = true;
				stateId = command.longValueOfParameterNamed("stateProvinceId");
				stateIdobj = this.codeValueRepository.getOne(stateId);
				addobj.setStateProvince(stateIdobj);
			}

		}
		if ((command.longValueOfParameterNamed("countryId") != null)) {
			if ((command.longValueOfParameterNamed("countryId") != 0)) {
				is_address_update = true;
				countryId = command.longValueOfParameterNamed("countryId");
				countryIdObj = this.codeValueRepository.getOne(countryId);
				addobj.setCountry(countryIdObj);
			}

		}

		if (!(command.stringValueOfParameterNamed("postalCode").isEmpty())) {
			is_address_update = true;
			final String postalCode = command.stringValueOfParameterNamed("postalCode");
			addobj.setPostalCode(postalCode);
		}

		if (command.bigDecimalValueOfParameterNamed("latitude") != null) {

			is_address_update = true;
			final BigDecimal latitude = command.bigDecimalValueOfParameterNamed("latitude");

			addobj.setLatitude(latitude);
		}
		if (command.bigDecimalValueOfParameterNamed("longitude") != null) {
			is_address_update = true;
			final BigDecimal longitude = command.bigDecimalValueOfParameterNamed("longitude");
			addobj.setLongitude(longitude);

		}

		if (is_address_update) {

			this.addressRepository.save(addobj);

		}

		final Boolean testActive = command.booleanPrimitiveValueOfParameterNamed("isActive");
		if (testActive != null) {

			final boolean active = command.booleanPrimitiveValueOfParameterNamed("isActive");
			clientAddressObj.setIs_active(active);

		}

		return new CommandProcessingResultBuilder().withCommandId(command.commandId())
				.withEntityId(clientAddressObj.getId()).build();
	}
}
