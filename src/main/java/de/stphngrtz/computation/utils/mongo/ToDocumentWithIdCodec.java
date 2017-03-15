package de.stphngrtz.computation.utils.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public abstract class ToDocumentWithIdCodec<T, I> implements Codec<T> {

    private static final String ID = "_id";
    private static final String VERSION = "_version";
    private static final String DATE = "_date";

    private final Codec<I> idCodec;
    private Encoder<T> encoder;
    private Map<Integer, DecoderWithId<T, I>> decoderByVersion;

    ToDocumentWithIdCodec(Codec<I> idCodec) {
        this.idCodec = idCodec;
        this.decoderByVersion = new HashMap<>();
    }

    void register(Encoder<T> encoder) {
        this.encoder = encoder;
    }

    void register(DecoderWithId<T, I> decoder, int version) {
        decoderByVersion.put(version, decoder);
    }

    protected abstract I id(T value);
    protected abstract int version();

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        reader.readName(ID);
        I id = idCodec.decode(reader, decoderContext);
        int version = reader.readInt32(VERSION);
        reader.readString(DATE);

        DecoderWithId<T, I> decoder = decoderByVersion.get(version);
        if (decoder == null)
            throw new IllegalStateException(String.format("Unsupported Version: %d", version));

        T value = decoder.decode(reader, decoderContext, id);
        reader.readEndDocument();
        return value;
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        writer.writeStartDocument();

        writer.writeName(ID);
        encoderContext.encodeWithChildContext(idCodec, writer, id(value));

        writer.writeInt32(VERSION, version());
        writer.writeString(DATE, ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));

        encoder.encode(writer, value, encoderContext);
        writer.writeEndDocument();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getEncoderClass() {
        return (Class<T>) this.getClass().getTypeParameters()[0].getClass();
    }
}
