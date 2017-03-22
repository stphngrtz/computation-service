package de.stphngrtz.computation.utils.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.math.BigDecimal;
import java.util.Objects;

public class JavaCodecProvider implements CodecProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (Objects.equals(clazz, BigDecimal.class))
            return (Codec<T>) new BigDecimalCodec();

        return null;
    }

    private static class BigDecimalCodec implements Codec<BigDecimal> {

        @Override
        public BigDecimal decode(BsonReader reader, DecoderContext decoderContext) {
            String value = reader.readString();
            return new BigDecimal(value);
        }

        @Override
        public void encode(BsonWriter writer, BigDecimal bigDecimal, EncoderContext encoderContext) {
            writer.writeString(bigDecimal.toString());
        }

        @Override
        public Class<BigDecimal> getEncoderClass() {
            return BigDecimal.class;
        }
    }
}
