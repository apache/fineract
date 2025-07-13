package org.apache.fineract.portfolio.collectionsheet.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.collectionsheet.data.SaveCollectionSheetRequest;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetWritePlatformService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveCollectionSheetCommandHandler implements CommandHandler<SaveCollectionSheetRequest, CommandProcessingResult> {

    private final CollectionSheetWritePlatformService collectionSheetWritePlatformService;

    @Transactional
    @Override
    public CommandProcessingResult handle(Command<SaveCollectionSheetRequest> command) {
        return collectionSheetWritePlatformService.saveIndividualCollectionSheet(command);
    }
}
