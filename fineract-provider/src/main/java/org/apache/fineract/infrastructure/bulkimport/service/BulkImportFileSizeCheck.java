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
package org.apache.fineract.infrastructure.bulkimport.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Enumeration;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Rejects an .xlsx upload that POI cannot open. POI inflates each part of the archive into one byte array and refuses
 * any part above {@code ZipArchiveFakeEntry.getMaxEntrySize()} with a {@code RecordFormatException}. The sizes are read
 * from the archive's central directory, so nothing is inflated.
 */
final class BulkImportFileSizeCheck {

    private BulkImportFileSizeCheck() {}

    static void requireReadablePartSizes(final byte[] bytes, final long maxPartSize) {
        if (!isZipArchive(bytes)) {
            return;
        }
        long largestPartSize = 0;
        try (ZipFile zip = ZipFile.builder().setSeekableByteChannel(new SeekableInMemoryByteChannel(bytes)).get()) {
            for (Enumeration<ZipArchiveEntry> entries = zip.getEntries(); entries.hasMoreElements();) {
                largestPartSize = Math.max(largestPartSize, entries.nextElement().getSize());
            }
        } catch (IOException e) {
            return;
        }
        if (largestPartSize > maxPartSize) {
            final long minimumFiles = (largestPartSize + maxPartSize - 1) / maxPartSize;
            throw new PlatformApiDataValidationException("error.msg.bulk.import.file.too.large",
                    "The file is too large to import: one of its sheets holds " + megabytes(largestPartSize)
                            + " MB of data once unpacked, above the " + megabytes(maxPartSize) + " MB limit. Split the rows into at least "
                            + minimumFiles + " files and upload them one at a time.",
                    "file", largestPartSize, maxPartSize, minimumFiles);
        }
    }

    private static boolean isZipArchive(final byte[] bytes) {
        return bytes.length >= 4 && bytes[0] == 'P' && bytes[1] == 'K' && bytes[2] == 3 && bytes[3] == 4;
    }

    private static String megabytes(final long bytes) {
        return BigDecimal.valueOf(bytes).movePointLeft(6).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
