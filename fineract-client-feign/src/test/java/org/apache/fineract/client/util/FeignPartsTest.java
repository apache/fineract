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
package org.apache.fineract.client.util;

import static org.assertj.core.api.Assertions.assertThat;

import feign.Request;
import feign.Response;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FeignPartsTest {

    @TempDir
    File tempDir;

    @Test
    void testMediaTypeForJpeg() {
        String mediaType = FeignParts.mediaType("photo.jpg");

        assertThat(mediaType).isEqualTo("image/jpeg");
    }

    @Test
    void testMediaTypeForJpegUppercase() {
        String mediaType = FeignParts.mediaType("photo.JPEG");

        assertThat(mediaType).isEqualTo("image/jpeg");
    }

    @Test
    void testMediaTypeForPng() {
        String mediaType = FeignParts.mediaType("image.png");

        assertThat(mediaType).isEqualTo("image/png");
    }

    @Test
    void testMediaTypeForPdf() {
        String mediaType = FeignParts.mediaType("document.pdf");

        assertThat(mediaType).isEqualTo("application/pdf");
    }

    @Test
    void testMediaTypeForDocx() {
        String mediaType = FeignParts.mediaType("report.docx");

        assertThat(mediaType).isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Test
    void testMediaTypeForDoc() {
        String mediaType = FeignParts.mediaType("report.doc");

        assertThat(mediaType).isEqualTo("application/msword");
    }

    @Test
    void testMediaTypeForXlsx() {
        String mediaType = FeignParts.mediaType("spreadsheet.xlsx");

        assertThat(mediaType).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    void testMediaTypeForXls() {
        String mediaType = FeignParts.mediaType("spreadsheet.xls");

        assertThat(mediaType).isEqualTo("application/vnd.ms-excel");
    }

    @Test
    void testMediaTypeForTiff() {
        String mediaType = FeignParts.mediaType("scan.tiff");

        assertThat(mediaType).isEqualTo("image/tiff");
    }

    @Test
    void testMediaTypeForGif() {
        String mediaType = FeignParts.mediaType("animation.gif");

        assertThat(mediaType).isEqualTo("image/gif");
    }

    @Test
    void testMediaTypeForOdt() {
        String mediaType = FeignParts.mediaType("document.odt");

        assertThat(mediaType).isEqualTo("application/vnd.oasis.opendocument.text");
    }

    @Test
    void testMediaTypeForOds() {
        String mediaType = FeignParts.mediaType("spreadsheet.ods");

        assertThat(mediaType).isEqualTo("application/vnd.oasis.opendocument.spreadsheet");
    }

    @Test
    void testMediaTypeForTxt() {
        String mediaType = FeignParts.mediaType("readme.txt");

        assertThat(mediaType).isEqualTo("text/plain");
    }

    @Test
    void testMediaTypeForUnknownExtension() {
        String mediaType = FeignParts.mediaType("file.xyz");

        assertThat(mediaType).isEqualTo("application/octet-stream");
    }

    @Test
    void testMediaTypeCaseInsensitive() {
        assertThat(FeignParts.mediaType("FILE.JPG")).isEqualTo("image/jpeg");
        assertThat(FeignParts.mediaType("file.PDF")).isEqualTo("application/pdf");
        assertThat(FeignParts.mediaType("Document.DOCX"))
                .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Test
    void testMediaTypeForNullFilename() {
        String mediaType = FeignParts.mediaType(null);

        assertThat(mediaType).isNull();
    }

    @Test
    void testMediaTypeForFilenameWithoutExtension() {
        String mediaType = FeignParts.mediaType("filename");

        assertThat(mediaType).isNull();
    }

    @Test
    void testFileNameExtractionFromContentDisposition() {
        Response response = Response.builder().status(200)
                .request(Request.create(Request.HttpMethod.GET, "/test", Map.of(), null, null, null))
                .headers(Map.of("Content-Disposition", List.of("attachment; filename=\"test.pdf\""))).body(new byte[0]).build();

        Optional<String> filename = FeignParts.fileName(response);

        assertThat(filename).isPresent();
        assertThat(filename.get()).isEqualTo("test.pdf");
    }

    @Test
    void testFileNameExtractionWithComplexFilename() {
        Response response = Response.builder().status(200)
                .request(Request.create(Request.HttpMethod.GET, "/test", Map.of(), null, null, null))
                .headers(Map.of("Content-Disposition", List.of("attachment; filename=\"report-2024-01-15.xlsx\""))).body(new byte[0])
                .build();

        Optional<String> filename = FeignParts.fileName(response);

        assertThat(filename).isPresent();
        assertThat(filename.get()).isEqualTo("report-2024-01-15.xlsx");
    }

    @Test
    void testFileNameWithoutContentDisposition() {
        Response response = Response.builder().status(200)
                .request(Request.create(Request.HttpMethod.GET, "/test", Map.of(), null, null, null)).headers(Map.of()).body(new byte[0])
                .build();

        Optional<String> filename = FeignParts.fileName(response);

        assertThat(filename).isEmpty();
    }

    @Test
    void testFileNameWithNullHeaders() {
        Response response = Response.builder().status(200)
                .request(Request.create(Request.HttpMethod.GET, "/test", Map.of(), null, null, null)).headers(null).body(new byte[0])
                .build();

        Optional<String> filename = FeignParts.fileName(response);

        assertThat(filename).isEmpty();
    }

    @Test
    void testFileNameWithEmptyContentDisposition() {
        Response response = Response.builder().status(200)
                .request(Request.create(Request.HttpMethod.GET, "/test", Map.of(), null, null, null))
                .headers(Map.of("Content-Disposition", Collections.emptyList())).body(new byte[0]).build();

        Optional<String> filename = FeignParts.fileName(response);

        assertThat(filename).isEmpty();
    }

    @Test
    void testFileNameWithInvalidContentDisposition() {
        Response response = Response.builder().status(200)
                .request(Request.create(Request.HttpMethod.GET, "/test", Map.of(), null, null, null))
                .headers(Map.of("Content-Disposition", List.of("inline"))).body(new byte[0]).build();

        Optional<String> filename = FeignParts.fileName(response);

        assertThat(filename).isEmpty();
    }

    @Test
    void testProbeContentTypeForJpegFile() throws IOException {
        File jpegFile = new File(tempDir, "test.jpg");
        Files.write(jpegFile.toPath(), new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF });

        String contentType = FeignParts.probeContentType(jpegFile);

        assertThat(contentType).isIn("image/jpeg", "image/jpg");
    }

    @Test
    void testProbeContentTypeForPdfFile() throws IOException {
        File pdfFile = new File(tempDir, "test.pdf");
        Files.write(pdfFile.toPath(), "%PDF-1.4".getBytes(StandardCharsets.UTF_8));

        String contentType = FeignParts.probeContentType(pdfFile);

        assertThat(contentType).isEqualTo("application/pdf");
    }

    @Test
    void testProbeContentTypeFallsBackToMediaType() throws IOException {
        File unknownFile = new File(tempDir, "test.docx");
        Files.write(unknownFile.toPath(), "test content".getBytes(StandardCharsets.UTF_8));

        String contentType = FeignParts.probeContentType(unknownFile);

        assertThat(contentType).isNotNull();
        assertThat(contentType).isNotEqualTo("application/octet-stream");
    }

    @Test
    void testProbeContentTypeForUnknownFile() throws IOException {
        File unknownFile = new File(tempDir, "test.xyz");
        Files.write(unknownFile.toPath(), "unknown content".getBytes(StandardCharsets.UTF_8));

        String contentType = FeignParts.probeContentType(unknownFile);

        assertThat(contentType).isNotNull();
    }

    @Test
    void testMediaTypeForAllSupportedImageFormats() {
        assertThat(FeignParts.mediaType("image.jpg")).isEqualTo("image/jpeg");
        assertThat(FeignParts.mediaType("image.jpeg")).isEqualTo("image/jpeg");
        assertThat(FeignParts.mediaType("image.png")).isEqualTo("image/png");
        assertThat(FeignParts.mediaType("image.gif")).isEqualTo("image/gif");
        assertThat(FeignParts.mediaType("image.tif")).isEqualTo("image/tiff");
        assertThat(FeignParts.mediaType("image.tiff")).isEqualTo("image/tiff");
    }

    @Test
    void testMediaTypeForAllSupportedDocumentFormats() {
        assertThat(FeignParts.mediaType("doc.pdf")).isEqualTo("application/pdf");
        assertThat(FeignParts.mediaType("doc.doc")).isEqualTo("application/msword");
        assertThat(FeignParts.mediaType("doc.docx")).isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertThat(FeignParts.mediaType("doc.odt")).isEqualTo("application/vnd.oasis.opendocument.text");
        assertThat(FeignParts.mediaType("doc.txt")).isEqualTo("text/plain");
    }

    @Test
    void testMediaTypeForAllSupportedSpreadsheetFormats() {
        assertThat(FeignParts.mediaType("sheet.xls")).isEqualTo("application/vnd.ms-excel");
        assertThat(FeignParts.mediaType("sheet.xlsx")).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(FeignParts.mediaType("sheet.ods")).isEqualTo("application/vnd.oasis.opendocument.spreadsheet");
    }
}
