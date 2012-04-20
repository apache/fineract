package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NoteDataList {

	private Collection<NoteData> notes = new ArrayList<NoteData>();

	public NoteDataList() {
		//
	}

	public NoteDataList(final Collection<NoteData> notes) {
		this.notes = notes;
	}

	public Collection<NoteData> getNotes() {
		return notes;
	}

	public void setNotes(Collection<NoteData> notes) {
		this.notes = notes;
	}
}