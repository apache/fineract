package org.mifosng.platform.api.data;

import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.DateTime;

@JsonFilter("myFilter")
public class NoteData {

	private Long id;
	private Long clientId;
	private Long loanId;
	private Long loanTransactionId;
	private EnumOptionData noteType;
	
	private String note;
	
	private Long createdById;
	private String createdByUsername;
	private DateTime createdOn;
	private Long updatedById;
	private String updatedByUsername;
	private DateTime updatedOn;
	
	public NoteData() {
		//
	}

	public NoteData(Long id, Long clientId, Long loanId,
			Long transactionId, EnumOptionData noteType, String note, 
			DateTime createdDate, Long createdById, String createdByUsername, 
			DateTime lastModifiedDate, Long lastModifiedById, String updatedByUsername) {
		this.id = id;
		this.clientId = clientId;
		this.loanId = loanId;
		this.loanTransactionId = transactionId;
		this.noteType = noteType;
		this.note = note;
		this.createdOn = createdDate;
		this.createdById = createdById;
		this.createdByUsername = createdByUsername;
		this.updatedOn = lastModifiedDate;
		this.updatedById = lastModifiedById;
		this.updatedByUsername = updatedByUsername;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getLoanId() {
		return loanId;
	}

	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}

	public Long getLoanTransactionId() {
		return loanTransactionId;
	}

	public void setLoanTransactionId(Long loanTransactionId) {
		this.loanTransactionId = loanTransactionId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Long getCreatedById() {
		return createdById;
	}

	public void setCreatedById(Long createdById) {
		this.createdById = createdById;
	}

	public String getCreatedByUsername() {
		return createdByUsername;
	}

	public void setCreatedByUsername(String createdByUsername) {
		this.createdByUsername = createdByUsername;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public Long getUpdatedById() {
		return updatedById;
	}

	public void setUpdatedById(Long updatedById) {
		this.updatedById = updatedById;
	}

	public String getUpdatedByUsername() {
		return updatedByUsername;
	}

	public void setUpdatedByUsername(String updatedByUsername) {
		this.updatedByUsername = updatedByUsername;
	}

	public DateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(DateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	public EnumOptionData getNoteType() {
		return noteType;
	}

	public void setNoteType(EnumOptionData noteType) {
		this.noteType = noteType;
	}
}