package org.apache.fineract.portfolio.collectionsheet.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.portfolio.collectionsheet.data.GenerateCollectionSheetRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateCollectionSheetCommand extends Command<GenerateCollectionSheetRequest> {

    private static final long serialVersionUID = 1L;

}
