package de.stphngrtz.computation.utils.jackson;

import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.stphngrtz.computation.model.Structure;

import java.io.IOException;

public class Jackson {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new ComputationModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private Jackson() {
    }

    public static ObjectMapper mapper() {
        return mapper;
    }

    /**
     * @see ObjectMapper#writeValueAsString(Object)
     */
    public static String writeAsString(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException(e);
        }
    }

    /**
     * @see ObjectMapper#readValue(String, Class)
     */
    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            return mapper.readValue(content, valueType);
        } catch (JsonParseException | JsonMappingException e) {
            throw new JsonProcessingRuntimeException(e);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * @see akka.http.javadsl.marshallers.jackson.Jackson#unmarshaller(ObjectMapper, Class)
     */
    public static <T> Unmarshaller<HttpEntity, T> unmarshaller(Class<T> expectedType) {
        return akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller(mapper, expectedType);
    }

    /**
     * Wrapper für {@link JsonProcessingException} als {@link RuntimeException}
     */
    public static final class JsonProcessingRuntimeException extends RuntimeException {
        JsonProcessingRuntimeException(JsonProcessingException cause) {
            super(cause);
        }
    }

    /**
     * Wrapper für {@link IOException} als {@link RuntimeException}
     */
    public static final class IORuntimeException extends RuntimeException {
        IORuntimeException(IOException cause) {
            super(cause);
        }
    }
}
