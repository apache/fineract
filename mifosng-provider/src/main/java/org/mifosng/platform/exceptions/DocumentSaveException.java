package org.mifosng.platform.exceptions;

public class DocumentSaveException extends
		AbstractPlatformResourceNotFoundException {

	public DocumentSaveException(final String name) {
		super("error.msg.document.id.save",
				"Unable to save the document with name" + name
						+ " due to a File system / Amazon S3 issue", name);
	}
}
