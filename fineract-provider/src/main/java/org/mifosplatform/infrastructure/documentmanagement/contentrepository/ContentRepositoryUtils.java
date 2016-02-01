/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.contentrepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.exception.ImageDataURLNotValidException;
import org.mifosplatform.infrastructure.core.exception.ImageUploadException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.documentmanagement.exception.ContentManagementException;

public class ContentRepositoryUtils {

    public static Random random = new Random();

    public static enum IMAGE_MIME_TYPE {
        GIF("image/gif"), JPEG("image/jpeg"), PNG("image/png");

        private final String value;

        private IMAGE_MIME_TYPE(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static IMAGE_MIME_TYPE fromFileExtension(IMAGE_FILE_EXTENSION fileExtension) {
            switch (fileExtension) {
                case GIF:
                    return IMAGE_MIME_TYPE.GIF;
                case JPG:
                case JPEG:
                    return IMAGE_MIME_TYPE.JPEG;
                case PNG:
                    return IMAGE_MIME_TYPE.PNG;
                default:
                    throw new IllegalArgumentException();
            }
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

        public String getValueWithoutDot() {
            return this.value.substring(1);
        }

        public IMAGE_FILE_EXTENSION getFileExtension() {
            switch (this) {
                case GIF:
                    return IMAGE_FILE_EXTENSION.GIF;
                case JPEG:
                    return IMAGE_FILE_EXTENSION.JPEG;
                case PNG:
                    return IMAGE_FILE_EXTENSION.PNG;
                default:
                    throw new IllegalArgumentException();
            }
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
    public static void validateImageMimeType(final String mimeType) {
        if (!(mimeType.equalsIgnoreCase(IMAGE_MIME_TYPE.GIF.getValue()) || mimeType.equalsIgnoreCase(IMAGE_MIME_TYPE.JPEG.getValue()) || mimeType
                .equalsIgnoreCase(IMAGE_MIME_TYPE.PNG.getValue()))) { throw new ImageUploadException(); }
    }

    /**
     * Extracts Image from a Data URL
     * 
     * @param mimeType
     */
    public static Base64EncodedImage extractImageFromDataURL(final String dataURL) {
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

    public static void validateFileSizeWithinPermissibleRange(final Long fileSize, final String name) {
        /**
         * Using Content-Length gives me size of the entire request, which is
         * good enough for now for a fast fail as the length of the rest of the
         * content i.e name and description while compared to the uploaded file
         * size is negligible
         **/
        if (fileSize != null && ((fileSize / (1024 * 1024)) > ContentRepository.MAX_FILE_UPLOAD_SIZE_IN_MB)) { throw new ContentManagementException(
                name, fileSize, ContentRepository.MAX_FILE_UPLOAD_SIZE_IN_MB); }
    }

    public static void validateClientImageNotEmpty(final String imageFileName) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        if (imageFileName == null) {
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.clientImage.cannot.be.blank");
            final StringBuilder defaultEnglishMessage = new StringBuilder("The parameter image cannot be blank.");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(),
                    defaultEnglishMessage.toString(), "image");
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    /**
     * Generate a random String
     * 
     * @return
     */
    public static String generateRandomString() {
        final String characters = "abcdefghijklmnopqrstuvwxyz123456789";
        final int length = generateRandomNumber();
        final char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

    /**
     * Generate a random number between 5 to 16
     * 
     * @return
     */
    public static int generateRandomNumber() {
        final Random randomGenerator = new Random();
        return randomGenerator.nextInt(11) + 5;
    }
}