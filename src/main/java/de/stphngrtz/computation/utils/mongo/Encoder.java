package de.stphngrtz.computation.utils.mongo;

import org.bson.BsonWriter;
import org.bson.codecs.EncoderContext;

public interface Encoder<T> {

    /**
     * Encode an instance of the type parameter {@code T} into a BSON value.
     *
     * @param writer         the BSON writer to encode into
     * @param value          the value to encode
     * @param encoderContext the encoder context
     */
    void encode(BsonWriter writer, T value, EncoderContext encoderContext);
}
