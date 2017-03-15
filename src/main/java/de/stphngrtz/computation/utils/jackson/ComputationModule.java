package de.stphngrtz.computation.utils.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.stphngrtz.computation.model.Structure;

import java.io.IOException;
import java.util.function.Function;

class ComputationModule extends SimpleModule {

    ComputationModule() {
        super("ComputationModule", new Version(1, 0, 0, "SNAPSHOT", "de.stphngrtz", "computation-service"));

        // addSerializer(Structure.Id.class, toStringSerializer());
        // addDeserializer(Structure.Id.class, fromStringDeserializer(Structure.Id::new));
    }

    private static <T> JsonSerializer<T> toStringSerializer() {
        return new JsonSerializer<T>() {
            @Override
            public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(t.toString());
            }
        };
    }

    private static <T> JsonDeserializer<T> fromStringDeserializer(Function<String, T> f) {
        return new JsonDeserializer<T>() {
            @Override
            public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                if (!jsonParser.hasToken(JsonToken.VALUE_STRING))
                    throw new IllegalArgumentException(String.format("Unexpected token! (is: %s, should be: %s)", jsonParser.getCurrentToken(), JsonToken.VALUE_STRING));

                return f.apply(jsonParser.getText());
            }
        };
    }
}
