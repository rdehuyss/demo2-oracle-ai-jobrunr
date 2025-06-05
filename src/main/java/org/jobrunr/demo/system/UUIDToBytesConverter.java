package org.jobrunr.demo.system;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Converts a UUID to a 16-byte array (big-endian) for insertion into RAW(16).
 */
@WritingConverter
public class UUIDToBytesConverter implements Converter<UUID, byte[]> {
    @Override
    public byte[] convert(UUID source) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(source.getMostSignificantBits());
        bb.putLong(source.getLeastSignificantBits());
        return bb.array();
    }
}
