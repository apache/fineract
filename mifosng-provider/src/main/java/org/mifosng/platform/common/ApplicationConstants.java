package org.mifosng.platform.common;

public class ApplicationConstants {

	public static final Long CLIENT_IDENTITY_CODE_ID = 9L;

	// TODO:Vishwas Need to move these settings to a database flag
	public static final Integer MAX_FILE_UPLOAD_SIZE_IN_MB = 5;

	/*** Entities for document Management **/
	public static enum DOCUMENT_MANAGEMENT_ENTITIES {
		CLIENTs, STAFF, LOANS;
		@Override
		public String toString() {
			return name().toString().toLowerCase();
		}
	}

}
