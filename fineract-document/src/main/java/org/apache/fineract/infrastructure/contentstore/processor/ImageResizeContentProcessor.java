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
package org.apache.fineract.infrastructure.contentstore.processor;

import static java.util.Objects.requireNonNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.contentstore.exception.ContentProcessorException;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class ImageResizeContentProcessor implements ContentProcessor {

    private static final String IMAGE_RESIZE_PREFIX = "image.resize.";

    public static final String IMAGE_RESIZE_PARAM_MAX_WIDTH = IMAGE_RESIZE_PREFIX + "max-width";
    public static final String IMAGE_RESIZE_PARAM_MAX_HEIGHT = IMAGE_RESIZE_PREFIX + "max-height";
    public static final String IMAGE_RESIZE_PARAM_FORMAT = IMAGE_RESIZE_PREFIX + "format";

    private final ContentPipe pipe;

    private static final Pattern VALID_IMAGE_FORMATS = Pattern.compile("^(gif|jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE);

    @Override
    public ContentProcessorContext process(final ContentProcessorContext ctx) {
        final Integer maxWidth = ctx.getParameter(IMAGE_RESIZE_PARAM_MAX_WIDTH);
        final Integer maxHeight = ctx.getParameter(IMAGE_RESIZE_PARAM_MAX_HEIGHT);
        final String format = ctx.getParameter(IMAGE_RESIZE_PARAM_FORMAT, "jpg");

        requireNonNull(maxWidth, "Missing max width parameter");
        requireNonNull(maxHeight, "Missing max height parameter");

        if (!VALID_IMAGE_FORMATS.matcher(format).matches()) {
            throw new ContentProcessorException(String.format("Wrong image format parameter: %s", format));
        }

        final var pipedInputStream = pipe.pipe(ctx.getInputStream(), (in, out) -> {
            final var image = resize(in, maxWidth, maxHeight);

            // compresses the image into the specified format (e.g., "gif", "jpeg", "png")
            ImageIO.write(image, format, out);
        });

        return ctx.clone(pipedInputStream);
    }

    private BufferedImage resize(InputStream is, int targetWidth, int targetHeight) throws IOException {
        final var imageIs = ImageIO.createImageInputStream(StreamUtils.nonClosing(is));

        final var readers = ImageIO.getImageReaders(imageIs);

        if (!readers.hasNext()) {
            throw new ContentProcessorException("No image reader found for format");
        }

        final var reader = readers.next();

        try {
            reader.setInput(imageIs, true, true);

            // get original dimensions to calculate subsampling ratio
            int originalWidth = reader.getWidth(0);
            int originalHeight = reader.getHeight(0);

            // calculate subsampling ratio
            // subsampling is an integer ratio of source size to output size
            int subsampleX = Math.max(1, originalWidth / targetWidth);
            int subsampleY = Math.max(1, originalHeight / targetHeight);
            int subsampling = Math.max(subsampleX, subsampleY);

            final var param = reader.getDefaultReadParam();
            param.setSourceSubsampling(subsampling, subsampling, 0, 0);

            // image will be close to the target size, but not exact,
            // as subsampling only works with integer ratios. Further quality scaling
            // can be done on the smaller BufferedImage if exact dimensions are needed.

            // read the image using the parameters
            return reader.read(0, param);
        } finally {
            reader.dispose();
        }
    }
}
