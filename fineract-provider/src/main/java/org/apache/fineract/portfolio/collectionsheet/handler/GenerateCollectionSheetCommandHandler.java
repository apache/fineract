package org.apache.fineract.portfolio.collectionsheet.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.portfolio.collectionsheet.data.GenerateCollectionSheetRequest;
import org.apache.fineract.portfolio.collectionsheet.data.IndividualCollectionSheetData;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateCollectionSheetCommandHandler
        implements CommandHandler<GenerateCollectionSheetRequest, IndividualCollectionSheetData> {

    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;

    @Transactional
    @Override
    public IndividualCollectionSheetData handle(Command<GenerateCollectionSheetRequest> command) {
        return this.collectionSheetReadPlatformService.generateIndividualCollectionSheet(command.getPayload().getRequest());
    }
}
