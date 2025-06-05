package org.jobrunr.demo.system;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Converts a 16-byte array (as stored in RAW(16)) back to a UUID.
 */
@ReadingConverter
public class BytesToUUIDConverter implements Converter<byte[], UUID> {
    @Override
    public UUID convert(byte[] source) {
        if (source == null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(source);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }
}
