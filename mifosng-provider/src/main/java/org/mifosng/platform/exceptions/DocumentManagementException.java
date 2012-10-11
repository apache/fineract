package org.mifosng.platform.exceptions;

import org.mifosng.platform.common.ApplicationConstants;

public class DocumentManagementException extends
		AbstractPlatformDomainRuleException {

	public DocumentManagementException(final String name) {
		super("error.msg.document.save", "Error while manipulating file "
				+ name + " due to a File system / Amazon S3 issue", name);
	}

	public DocumentManagementException(final String name, final Long fileSize) {
		super("error.msg.document.file.too.big",
				"Unable to save the document with name" + name
						+ " since its file Size of " + fileSize / (1024 * 1024)
						+ " MB exceeds the max permissable file size  of "
						+ ApplicationConstants.MAX_FILE_UPLOAD_SIZE_IN_MB
						+ " MB", name, fileSize);
	}

}
