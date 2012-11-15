package org.mifosng.platform.common;

/**
 * @deprecated - KW - in general would like to avoid the whole throw it in common approach and dont want to see
 *             - this file attract more application constants.
 */
@Deprecated
public class ApplicationConstants {

	/**
	 * The intention of m_code table usage is not to use the id but the unique name of the code.
	 * 
	 * This will stop reserved magic numbers spreading through application for one and it 
	 * more natural to use the code name like you would use a table name in a query.
	 */
	@Deprecated
	public static final Long CLIENT_IDENTITY_CODE_ID = 9L;

	// TODO:Vishwas Need to move these settings to the Database
	public static final Integer MAX_FILE_UPLOAD_SIZE_IN_MB = 5;

	// TODO:Vishwas Need to move these settings to the Database
	public static final Integer MAX_IMAGE_UPLOAD_SIZE_IN_MB = 1;

	/*** Entities for document Management **/
	public static enum DOCUMENT_MANAGEMENT_ENTITY {
		CLIENTS, CLIENT_IDENTIFIERS, STAFF, LOANS, SAVINGS;
		@Override
		public String toString() {
			return name().toString().toLowerCase();
		}
	}

	/*** Entities for image Management **/
	public static enum IMAGE_MANAGEMENT_ENTITY {
		CLIENTS, STAFF;
		@Override
		public String toString() {
			return name().toString().toLowerCase();
		}
	}

}
