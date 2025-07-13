package org.apache.fineract.portfolio.collectionsheet.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.portfolio.collectionsheet.data.SaveCollectionSheetRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class SaveCollectionSheetCommand extends Command<SaveCollectionSheetRequest> {

    private static final long serialVersionUID = 1L;

}
