package org.mifosng.platform.common;

public class ApplicationConstants {

	public static final Long CLIENT_IDENTITY_CODE_ID = 9L;

	/*** Entities for document Management **/
	public static enum DOCUMENT_MANAGEMENT_ENTITIES {
		CLIENTs, STAFF, LOANS;
		@Override
		public String toString() {
			return name().toString().toLowerCase();
		}
	}

}
