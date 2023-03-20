package org.apache.fineract.infrastructure.documentmanagement.contentrepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class S3ContentRepositoryTest {

    final String testImage = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    @Test
    void saveImage() {
        // given
        final Base64EncodedImage base64EncodedImage = ContentRepositoryUtils.extractImageFromDataURL(testImage);
        S3ContentRepository s3ContentRepository = mock(S3ContentRepository.class);
        when(s3ContentRepository.getStorageType()).thenReturn(StorageType.FILE_SYSTEM);
        when(s3ContentRepository.saveImage(base64EncodedImage, "test", 2L, "test")).thenCallRealMethod();
        // when
        String imageLocation = s3ContentRepository.saveImage(base64EncodedImage, "test", 2L, "test");
        // then
        Assertions.assertEquals("images/test/2/test.png", imageLocation);
    }
}
