package org.mifosng.platform.client.domain;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.client.domain.Note.NoteType;

public class NoteEnumerations {

	public static EnumOptionData noteType(final Integer id) {
		return noteType(Note.NoteType.parse(id));
	}

	public static EnumOptionData noteType(NoteType type) {
		EnumOptionData optionData = null;
		switch (type) {
		case CLIENT:
			optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Client note");
			break;
		case LOAN:
			optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Loan note");
			break;
		case LOAN_TRANSACTION:
			optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Loan transaction note");
			break;
		}
		return optionData;
	}
}
