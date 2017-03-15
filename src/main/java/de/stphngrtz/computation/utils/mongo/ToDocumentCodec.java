package de.stphngrtz.computation.utils.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.*;

import java.util.HashMap;
import java.util.Map;

public abstract class ToDocumentCodec<T> implements Codec<T> {

    private static final String VERSION = "_version";

    private Encoder<T> encoder;
    private Map<Integer, Decoder<T>> decoderByVersion;

    ToDocumentCodec() {
        this.decoderByVersion = new HashMap<>();
    }

    void register(Encoder<T> encoder) {
        this.encoder = encoder;
    }

    void register(Decoder<T> decoder, int version) {
        decoderByVersion.put(version, decoder);
    }

    protected abstract int version();

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        int version = reader.readInt32(VERSION);

        Decoder<T> decoder = decoderByVersion.get(version);
        if (decoder == null)
            throw new IllegalStateException(String.format("Unsupported Version: %d", version));

        T value = decoder.decode(reader, decoderContext);
        reader.readEndDocument();
        return value;
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeInt32(VERSION, version());
        encoder.encode(writer, value, encoderContext);
        writer.writeEndDocument();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getEncoderClass() {
        return (Class<T>) this.getClass().getTypeParameters()[0].getClass();
    }
}
