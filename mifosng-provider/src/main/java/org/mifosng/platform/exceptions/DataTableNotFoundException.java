package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class DataTableNotFoundException extends AbstractPlatformResourceNotFoundException {

	public DataTableNotFoundException(final String datatable, Long id) {
		super("error.msg.datatable.data.not.found", "Data Not Found for Data Table: ",
				datatable + "     Id:" + id);
	}
	
	public DataTableNotFoundException(final String datatable) {
		super("error.msg.datatable.not.found", "Data Table Not Found",
				datatable);
	}
}