package de.stphngrtz.computation.utils.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public abstract class ToStringCodec<T> implements Codec<T> {

    abstract String toString(T value);

    abstract T fromString(String value);

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        String value = reader.readString();
        return "".equals(value) ? null : fromString(value);
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        writer.writeString(value == null ? "" : toString(value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getEncoderClass() {
        return (Class<T>) this.getClass().getTypeParameters()[0].getClass();
    }
}
