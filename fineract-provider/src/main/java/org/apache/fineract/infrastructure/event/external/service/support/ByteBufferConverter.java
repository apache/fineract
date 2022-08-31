package org.apache.fineract.infrastructure.event.external.service.support;

import java.nio.ByteBuffer;
import org.springframework.stereotype.Component;

@Component
public class ByteBufferConverter {

    public byte[] convert(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.position(buffer.position() - bytes.length);
        return bytes;
    }
}
