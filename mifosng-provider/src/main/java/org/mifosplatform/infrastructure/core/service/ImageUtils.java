package org.mifosplatform.infrastructure.core.service;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.exception.ImageDataURLNotValidException;
import org.mifosplatform.infrastructure.core.exception.ImageUploadException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    public static enum IMAGE_MIME_TYPE {
        GIF("image/gif"), JPEG("image/jpeg"), PNG("image/png");

        private final String value;

        private IMAGE_MIME_TYPE(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum IMAGE_FILE_EXTENSION {
        GIF(".gif"), JPEG(".jpeg"), JPG(".jpg"), PNG(".png");

        private final String value;

        private IMAGE_FILE_EXTENSION(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum IMAGE_DATA_URI_SUFFIX {
        GIF("data:" + IMAGE_MIME_TYPE.GIF.getValue() + ";base64,"), JPEG("data:" + IMAGE_MIME_TYPE.JPEG.getValue() + ";base64,"), PNG(
                "data:" + IMAGE_MIME_TYPE.PNG.getValue() + ";base64,");

        private final String value;

        private IMAGE_DATA_URI_SUFFIX(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
    /**
     * Validates that passed in Mime type maps to known image mime types
     *
     * @param mimeType
     */
    public static void validateImageMimeType(String mimeType) {
        if (!(mimeType.equalsIgnoreCase(IMAGE_MIME_TYPE.GIF.getValue()) || mimeType.equalsIgnoreCase(IMAGE_MIME_TYPE.JPEG.getValue()) || mimeType
                .equalsIgnoreCase(IMAGE_MIME_TYPE.PNG.getValue()))) {
            throw new ImageUploadException();
        }
    }

    /**
     * Extracts Image from a Data URL
     *
     * @param mimeType
     */
    public static Base64EncodedImage extractImageFromDataURL(String dataURL) {
        String fileExtension = "";
        String base64EncodedString = null;
        if (StringUtils.startsWith(dataURL, IMAGE_DATA_URI_SUFFIX.GIF.getValue())) {
            base64EncodedString = dataURL.replaceAll(IMAGE_DATA_URI_SUFFIX.GIF.getValue(), "");
            fileExtension = IMAGE_FILE_EXTENSION.GIF.getValue();
        } else if (StringUtils.startsWith(dataURL, IMAGE_DATA_URI_SUFFIX.PNG.getValue())) {
            base64EncodedString = dataURL.replaceAll(IMAGE_DATA_URI_SUFFIX.PNG.getValue(), "");
            fileExtension = IMAGE_FILE_EXTENSION.PNG.getValue();
        } else if (StringUtils.startsWith(dataURL, IMAGE_DATA_URI_SUFFIX.JPEG.getValue())) {
            base64EncodedString = dataURL.replaceAll(IMAGE_DATA_URI_SUFFIX.JPEG.getValue(), "");
            fileExtension = IMAGE_FILE_EXTENSION.JPEG.getValue();
        } else {
            throw new ImageDataURLNotValidException();
        }

        return new Base64EncodedImage(base64EncodedString, fileExtension);
    }

    public static void validateClientImageNotEmpty(String imageFileName) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        if (imageFileName == null) {
            StringBuilder validationErrorCode = new StringBuilder("validation.msg.clientImage.cannot.be.blank");
            StringBuilder defaultEnglishMessage = new StringBuilder("The parameter image cannot be blank.");
            ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                    "image");
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}