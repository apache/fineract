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

import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.joda.time.LocalDate;

public class ClientFamilyMembersData {

	private final Long id;

	private final Long clientId;

	private final String firstName;

	private final String middleName;

	private final String lastName;

	private final String qualification;

	private final Long relationshipId;

	private final String relationship;

	private final Long maritalStatusId;

	private final String maritalStatus;

	private final Long genderId;

	private final String gender;

	private final LocalDate dateOfBirth;

	private final Long professionId;

	private final String profession;
	
	private final String mobileNumber;
	
	private final Long age;
	
	private final Boolean isDependent;

	// template holder
	private final Collection<CodeValueData> relationshipIdOptions;
	private final Collection<CodeValueData> genderIdOptions;
	private final Collection<CodeValueData> maritalStatusIdOptions;
	private final Collection<CodeValueData> professionIdOptions;

	private ClientFamilyMembersData(final Long id, final Long clientId, final String firstName, final String middleName,
			final String lastName, final String qualification,final String mobileNumber,final Long age,final Boolean isDependent, final String relationship, final Long relationshipId,
			final String maritalStatus, final Long maritalStatusId, final String gender, final Long genderId,
			final LocalDate dateOfBirth, final String profession, final Long professionId,
			final Collection<CodeValueData> relationshipIdOptions,final Collection<CodeValueData> genderIdOptions,final Collection<CodeValueData> maritalStatusIdOptions,
			final Collection<CodeValueData> professionIdOptions) {
		this.id = id;
		this.clientId = clientId;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.qualification = qualification;
		this.relationship = relationship;
		this.relationshipId = relationshipId;
		this.maritalStatus = maritalStatus;
		this.maritalStatusId = maritalStatusId;
		this.gender = gender;
		this.genderId = genderId;
		this.dateOfBirth = dateOfBirth;
		this.profession = profession;
		this.professionId = professionId;
		this.mobileNumber=mobileNumber;
		this.age=age;
		this.isDependent=isDependent;
		this.relationshipIdOptions=relationshipIdOptions;
		this.genderIdOptions=genderIdOptions;
		this.maritalStatusIdOptions=maritalStatusIdOptions;
		this.professionIdOptions=professionIdOptions;
		
	}

	public static ClientFamilyMembersData instance(final Long id, final Long clientId, final String firstName,
			final String middleName, final String lastName, final String qualification,final String mobileNumber,final Long age,final Boolean isDependent, final String relationship,
			final Long relationshipId, final String maritalStatus, final Long maritalStatusId, final String gender,
			final Long genderId, final LocalDate dateOfBirth, final String profession, final Long professionId
			) {
		return new ClientFamilyMembersData(id, clientId, firstName, middleName, lastName, qualification,mobileNumber,age,isDependent, relationship,
				relationshipId, maritalStatus, maritalStatusId, gender, genderId, dateOfBirth, profession,
				professionId,null,null,null,null);
	}
	
	
	public static ClientFamilyMembersData templateInstance(final Collection<CodeValueData> relationshipIdOptions,
			final Collection<CodeValueData> genderIdOptions,final Collection<CodeValueData> maritalStatusIdOptions,
			final Collection<CodeValueData> professionIdOptions) {
		
		
		return new ClientFamilyMembersData(null, null, null, null, null, null,null,
				null, null, null, null, null, null, null,
				null,null,null,null,relationshipIdOptions,genderIdOptions,maritalStatusIdOptions,professionIdOptions);
	}

	public Long getId() {
		return this.id;
	}

	public Long getClientId() {
		return this.clientId;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getMiddleName() {
		return this.middleName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public String getQualification() {
		return this.qualification;
	}

	public Long getRelationshipId() {
		return this.relationshipId;
	}

	public String getRelationship() {
		return this.relationship;
	}

	public Long getMaritalStatusId() {
		return this.maritalStatusId;
	}

	public String getMaritalStatus() {
		return this.maritalStatus;
	}

	public Long getGenderId() {
		return this.genderId;
	}

	public String getGender() {
		return this.gender;
	}

	public LocalDate getDateOfBirth() {
		return this.dateOfBirth;
	}

	public Long getProfessionId() {
		return this.professionId;
	}

	public String getProfession() {
		return this.profession;
	}

	public String getMobileNumber() {
		return this.mobileNumber;
	}

	public Long getAge() {
		return this.age;
	}

	public Boolean getIsDependent() {
		return this.isDependent;
	}
	
	

}
