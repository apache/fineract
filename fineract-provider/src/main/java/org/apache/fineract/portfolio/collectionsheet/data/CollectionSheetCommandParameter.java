package org.apache.fineract.portfolio.collectionsheet.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum CollectionSheetCommandParameter {

    GENERATE_COLLECTION_SHEET("generateCollectionSheet"), SAVE_COLLECTION_SHEET("saveCollectionSheet");

    private final String value;
}
