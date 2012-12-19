package org.mifosplatform.portfolio.client.domain;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.client.domain.Note.NoteType;

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
		case DEPOSIT:
			optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Deposit transaction note");
			break;
		case SAVING:
			optionData = new EnumOptionData(type.getValue().longValue(), type.getCode(), "Saving transaction note");
			break;
		default:
			break;
		
		}
		return optionData;
	}
}
