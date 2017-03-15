package de.stphngrtz.computation.utils.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;

import java.util.HashMap;
import java.util.Map;

public class Mongo {

    private static final Map<String, MongoClient> mongoClients = new HashMap<>();

    public static MongoDatabase getDatabase(String hostname, int port) {
        return mongoClients.computeIfAbsent(hostname + port, key -> new MongoClient(
                new ServerAddress(hostname, port),
                MongoClientOptions.builder().codecRegistry(
                        CodecRegistries.fromRegistries(
                                CodecRegistries.fromProviders(
                                        new ComputationCodecProvider()
                                ),
                                MongoClient.getDefaultCodecRegistry()
                        )
                ).build()
        )).getDatabase("computation");
    }

    public static void close() {
        mongoClients.values().forEach(com.mongodb.Mongo::close);
        mongoClients.clear();
    }
}
