package de.stphngrtz.computation.utils.mongo;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.HashMap;
import java.util.Map;

public class Mongo {

    private static final String DATABASE_NAME = "computation";

    private static final CodecRegistry CODEC_REGISTRY = CodecRegistries.fromRegistries(
            CodecRegistries.fromProviders(
                    new JavaCodecProvider(),
                    new ComputationCodecProvider()
            ),
            MongoClient.getDefaultCodecRegistry()
    );

    private static final Map<String, MongoClient> mongoClients = new HashMap<>();
    private static final Map<String, Fongo> fongoClients = new HashMap<>();

    public static MongoDatabase getDatabase(String hostname, int port, boolean inMemory) {
        return inMemory ? getFongoDatabase(hostname, port) : getMongoDatabase(hostname, port);
    }

    private static MongoDatabase getMongoDatabase(String hostname, int port) {
        return mongoClients.computeIfAbsent(hostname + port, key -> new MongoClient(new ServerAddress(hostname, port), MongoClientOptions.builder().codecRegistry(CODEC_REGISTRY).build())).getDatabase(DATABASE_NAME);
    }

    private static MongoDatabase getFongoDatabase(String hostname, int port) {
        return fongoClients.computeIfAbsent(hostname + port, key -> new Fongo(hostname + ":" + port)).getDatabase(DATABASE_NAME).withCodecRegistry(CODEC_REGISTRY);
    }

    public static void close() {
        mongoClients.values().forEach(com.mongodb.Mongo::close);
        mongoClients.clear();
        fongoClients.clear();
    }
}
