package org.apache.fineract.portfolio.collectionsheet.data;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@FieldNameConstants
public class SaveCollectionSheetRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private CollectionSheetRequest request;
}
