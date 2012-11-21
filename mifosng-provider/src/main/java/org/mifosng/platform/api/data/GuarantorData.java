package org.mifosng.platform.api.data;

import org.joda.time.LocalDate;

public class GuarantorData {
	private String firstname;
	private String lastname;
	private boolean externalGuarantor;

	/*** Fields for current customers serving as guarantors **/
	private Long existingClientId;
	private String externalId;
	private String officeName;
	private LocalDate joinedDate;

	/*** Fields for external persons serving as guarantors ***/
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private String zip;
	private String country;
	private String mobileNumber;
	private String housePhoneNumber;
	private String comment;
	private LocalDate dob;

	public GuarantorData(final Long existingClientId, final String firstname,
			final String lastname, final String externalId,
			final String officeName, final LocalDate joinedDate) {
		this.existingClientId = existingClientId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.externalId = externalId;
		this.officeName = officeName;
		this.joinedDate = joinedDate;
		this.externalGuarantor = false;
	}

	public GuarantorData(final String firstname, final String lastname,
			final LocalDate dob, final String addressLine1,
			final String addressLine2, final String city, final String state,
			final String zip, final String country, final String mobileNumber,
			final String housePhoneNumber, final String comment) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.dob = dob;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.country = country;
		this.mobileNumber = mobileNumber;
		this.housePhoneNumber = housePhoneNumber;
		this.comment = comment;
		this.externalGuarantor = true;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Long getExistingClientId() {
		return existingClientId;
	}

	public void setExistingClientId(Long existingClientId) {
		this.existingClientId = existingClientId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getOfficeName() {
		return officeName;
	}

	public void setOfficeName(String officeName) {
		this.officeName = officeName;
	}

	public LocalDate getJoinedDate() {
		return joinedDate;
	}

	public void setJoinedDate(LocalDate joinedDate) {
		this.joinedDate = joinedDate;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getHousePhoneNumber() {
		return housePhoneNumber;
	}

	public void setHousePhoneNumber(String housePhoneNumber) {
		this.housePhoneNumber = housePhoneNumber;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean getExternalGuarantor() {
		return externalGuarantor;
	}

	public void setExternalGuarantor(boolean externalGuarantor) {
		this.externalGuarantor = externalGuarantor;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

}