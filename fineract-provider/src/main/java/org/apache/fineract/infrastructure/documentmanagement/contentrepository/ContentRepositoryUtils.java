/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.documentmanagement.contentrepository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.core.exception.ImageDataURLNotValidException;
import org.apache.fineract.infrastructure.core.exception.ImageUploadException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;

public final class ContentRepositoryUtils {

    private static final SecureRandom random = new SecureRandom();

    private ContentRepositoryUtils() {}

    public enum ImageMIMEtype {

        GIF("image/gif"), JPEG("image/jpeg"), PNG("image/png");

        private final String value;

        ImageMIMEtype(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        @SuppressWarnings("UnnecessaryDefaultInEnumSwitch")
        public static ImageMIMEtype fromFileExtension(ImageFileExtension fileExtension) {
            switch (fileExtension) {
                case GIF:
                    return ImageMIMEtype.GIF;
                case JPG:
                case JPEG:
                    return ImageMIMEtype.JPEG;
                case PNG:
                    return ImageMIMEtype.PNG;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public enum ImageFileExtension {

        GIF(".gif"), JPEG(".jpeg"), JPG(".jpg"), PNG(".png");

        private final String value;

        ImageFileExtension(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public String getValueWithoutDot() {
            return this.value.substring(1);
        }

        public ImageFileExtension getFileExtension() {
            switch (this) {
                case GIF:
                    return ImageFileExtension.GIF;
                case JPEG:
                    return ImageFileExtension.JPEG;
                case PNG:
                    return ImageFileExtension.PNG;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public enum ImageDataURIsuffix {

        GIF("data:" + ImageMIMEtype.GIF.getValue() + ";base64,"), JPEG("data:" + ImageMIMEtype.JPEG.getValue() + ";base64,"), PNG(
                "data:" + ImageMIMEtype.PNG.getValue() + ";base64,");

        private final String value;

        ImageDataURIsuffix(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static ImageFileExtension imageExtensionFromFileName(String fileName) {
        if (StringUtils.endsWith(fileName.toLowerCase(), ContentRepositoryUtils.ImageFileExtension.GIF.getValue())) {
            return ContentRepositoryUtils.ImageFileExtension.GIF;
        } else if (StringUtils.endsWith(fileName, ContentRepositoryUtils.ImageFileExtension.PNG.getValue())) {
            return ContentRepositoryUtils.ImageFileExtension.PNG;
        } else {
            return ContentRepositoryUtils.ImageFileExtension.JPEG;
        }
    }

    /**
     * Validates that passed in Mime type maps to known image mime types
     *
     * @param mimeType
     */
    public static void validateImageMimeType(final String mimeType) {
        if ((!mimeType.equalsIgnoreCase(ImageMIMEtype.GIF.getValue()) && !mimeType.equalsIgnoreCase(ImageMIMEtype.JPEG.getValue())
                && !mimeType.equalsIgnoreCase(ImageMIMEtype.PNG.getValue()))) {
            throw new ImageUploadException(mimeType);
        }
    }

    /**
     * Extracts Image from a Data URL
     *
     * @param dataURL
     *            mimeType
     */
    public static Base64EncodedImage extractImageFromDataURL(final String dataURL) {
        String fileExtension = "";
        String base64EncodedString = null;
        if (StringUtils.startsWith(dataURL, ImageDataURIsuffix.GIF.getValue())) {
            base64EncodedString = dataURL.replaceAll(ImageDataURIsuffix.GIF.getValue(), "");
            fileExtension = ImageFileExtension.GIF.getValue();
        } else if (StringUtils.startsWith(dataURL, ImageDataURIsuffix.PNG.getValue())) {
            base64EncodedString = dataURL.replaceAll(ImageDataURIsuffix.PNG.getValue(), "");
            fileExtension = ImageFileExtension.PNG.getValue();
        } else if (StringUtils.startsWith(dataURL, ImageDataURIsuffix.JPEG.getValue())) {
            base64EncodedString = dataURL.replaceAll(ImageDataURIsuffix.JPEG.getValue(), "");
            fileExtension = ImageFileExtension.JPEG.getValue();
        } else {
            throw new ImageDataURLNotValidException();
        }

        return new Base64EncodedImage(base64EncodedString, fileExtension);
    }

    public static void validateFileSizeWithinPermissibleRange(final Long fileSize, final String name) {
        /**
         * Using Content-Length gives me size of the entire request, which is good enough for now for a fast fail as the
         * length of the rest of the content i.e name and description while compared to the uploaded file size is
         * negligible
         **/
        if (fileSize != null && ((fileSize / (1024 * 1024)) > ContentRepository.MAX_FILE_UPLOAD_SIZE_IN_MB)) {
            throw new ContentManagementException(name, fileSize, ContentRepository.MAX_FILE_UPLOAD_SIZE_IN_MB);
        }
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
     * Generate a random String.
     */

    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public static String generateRandomString() {
        final String characters = "abcdefghijklmnopqrstuvwxyz123456789";
        // length is a random number between 5 to 16
        final int length = random.nextInt(11) + 5;
        final char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }
}
