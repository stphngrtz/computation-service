package de.stphngrtz.computation.utils.mongo;

import org.bson.BsonReader;
import org.bson.codecs.DecoderContext;

public interface DecoderWithId<T, I> {
    T decode(BsonReader reader, DecoderContext decoderContext, I id);
}
