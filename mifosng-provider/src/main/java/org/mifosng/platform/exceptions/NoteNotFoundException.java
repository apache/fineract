package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when note resources are not found.
 */
public class NoteNotFoundException extends AbstractPlatformResourceNotFoundException {

	public NoteNotFoundException(Long id) {
		super("error.msg.note.id.invalid", "Note with identifier " + id + " does not exist", id);
	}
}