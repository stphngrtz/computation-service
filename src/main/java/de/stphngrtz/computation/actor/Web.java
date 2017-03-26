package de.stphngrtz.computation.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import de.stphngrtz.computation.model.Computation;
import de.stphngrtz.computation.model.Structure;
import de.stphngrtz.computation.utils.akka.Message;
import org.bson.BsonDocument;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Web extends AbstractActor {

    public static Props props(ActorRef producer, MongoDatabase database) {
        return Props.create(Web.class, () -> new Web(producer, database));
    }

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private Web(ActorRef producer, MongoDatabase database) {
        receive(ReceiveBuilder
                .match(Protocol.StructureCreateRequest.class, message -> {
                    log.debug("StructureCreateRequest! structure:{}", message.structure.id);
                    Structure.collection(database).insertOne(message.structure);
                })
                .match(Protocol.StructureGetRequest.class, message -> {
                    log.debug("StructureGetRequest! structure:{}", message.structureId);
                    Optional<Structure> structure = Optional.ofNullable(Structure.collection(database).find(Filters.eq(Structure.Fields.id, message.structureId)).first());
                    sender().tell(new Protocol.StructureGetResponse(structure), self());
                })
                .match(Protocol.StructureUpdateRequest.class, message -> {
                    log.debug("StructureUpdateRequest! structure:{}", message.structure.id);
                    Structure.collection(database).replaceOne(Filters.eq(Structure.Fields.id, message.structure.id), message.structure, new UpdateOptions().upsert(true));
                })
                .match(Protocol.StructureDeleteRequest.class, message -> {
                    log.debug("StructureDeleteRequest! structure:{}", message.structureId);
                    Structure.collection(database).deleteOne(Filters.eq(Structure.Fields.id, message.structureId));
                })
                .match(Protocol.StructuresGetRequest.class, message -> {
                    log.debug("StructuresGetRequest! ids:{} fields:{}", message.ids(), message.fields());

                    BiMap<String, String> fields = Structure.Fields.asMap(field -> Objects.equals(field, "id") || message.fields().contains(field));
                    List<Map<String, Object>> structures = Structure.rawCollection(database)
                            .find(message.ids().isEmpty() ? new BsonDocument() : Filters.in(Structure.Fields.id, message.ids()))
                            .projection(Projections.include(new ArrayList<>(fields.values())))
                            .map(document -> fields.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> document.get(e.getValue()))))
                            .into(new ArrayList<>());

                    sender().tell(new Protocol.StructuresGetResponse(structures), self());
                })
                .match(Protocol.ComputationCreateRequest.class, message -> {
                    log.debug("ComputationCreateRequest! computation:{}", message.computation.id);
                    Computation.collection(database).insertOne(message.computation);
                })
                .match(Protocol.ComputationGetRequest.class, message -> {
                    log.debug("ComputationGetRequest! computation:{}", message.computationId);
                    Optional<Computation> computation = Optional.ofNullable(Computation.collection(database).find(Filters.eq(Computation.Fields.id, message.computationId)).first());
                    sender().tell(new Protocol.ComputationGetResponse(computation), self());
                })
                .match(Protocol.ComputationDeleteRequest.class, message -> {
                    log.debug("ComputationDeleteRequest! computation:{}", message.computationId);
                    Computation.collection(database).deleteOne(Filters.eq(Computation.Fields.id, message.computationId));
                })
                .match(Protocol.ComputationsGetRequest.class, message -> {
                    log.debug("ComputationsGetRequest! ids:{} fields:{}", message.ids(), message.fields());

                    BiMap<String, String> fields = Computation.Fields.asMap(field -> Objects.equals(field, "id") || message.fields().contains(field));
                    List<Map<String, Object>> computations = Computation.rawCollection(database)
                            .find(message.ids().isEmpty() ? new BsonDocument() : Filters.in(Computation.Fields.id, message.ids()))
                            .projection(Projections.include(new ArrayList<>(fields.values())))
                            .map(document -> fields.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> document.get(e.getValue()))))
                            .into(new ArrayList<>());

                    sender().tell(new Protocol.ComputationsGetResponse(computations), self());
                })
                .build());
    }

    public interface Protocol {

        final class StructureCreateRequest implements Message {
            final Structure structure;

            public StructureCreateRequest(Structure structure) {
                this.structure = structure;
            }
        }

        final class StructureGetRequest implements Message {
            final Structure.Id structureId;

            public StructureGetRequest(Structure.Id structureId) {
                this.structureId = structureId;
            }
        }

        final class StructureGetResponse implements Message {
            public final Optional<Structure> structure;

            StructureGetResponse(Optional<Structure> structure) {
                this.structure = structure;
            }
        }

        final class StructureUpdateRequest implements Message {
            final Structure structure;

            public StructureUpdateRequest(Structure structure) {
                this.structure = structure;
            }
        }

        final class StructureDeleteRequest implements Message {
            final Structure.Id structureId;

            public StructureDeleteRequest(Structure.Id structureId) {
                this.structureId = structureId;
            }
        }

        final class StructuresGetRequest implements Message {
            private final Optional<String> ids;
            private final Optional<String> fields;

            public StructuresGetRequest(Optional<String> ids, Optional<String> fields) {
                this.ids = ids;
                this.fields = fields;
            }

            Set<Structure.Id> ids() {
                return Stream.of(ids.orElse("").split(",")).filter(id -> !Strings.isNullOrEmpty(id)).map(Structure.Id::new).collect(Collectors.toSet());
            }

            Set<String> fields() {
                return Stream.of(fields.orElse("").split(",")).filter(field -> !Strings.isNullOrEmpty(field)).collect(Collectors.toSet());
            }
        }

        final class StructuresGetResponse implements Message {
            public final List<Map<String, Object>> structures;

            StructuresGetResponse(List<Map<String, Object>> structures) {
                this.structures = structures;
            }
        }

        final class ComputationCreateRequest implements Message {
            final Computation computation;

            public ComputationCreateRequest(Computation computation) {
                this.computation = computation;
            }
        }

        final class ComputationGetRequest implements Message {
            final Computation.Id computationId;

            public ComputationGetRequest(Computation.Id computationId) {
                this.computationId = computationId;
            }
        }

        final class ComputationGetResponse implements Message {
            public final Optional<Computation> computation;

            ComputationGetResponse(Optional<Computation> computation) {
                this.computation = computation;
            }
        }

        final class ComputationDeleteRequest implements Message {
            final Computation.Id computationId;

            public ComputationDeleteRequest(Computation.Id computationId) {
                this.computationId = computationId;
            }
        }

        final class ComputationsGetRequest implements Message {
            private final Optional<String> ids;
            private final Optional<String> fields;

            public ComputationsGetRequest(Optional<String> ids, Optional<String> fields) {
                this.ids = ids;
                this.fields = fields;
            }

            Set<Computation.Id> ids() {
                return Stream.of(ids.orElse("").split(",")).filter(id -> !Strings.isNullOrEmpty(id)).map(Computation.Id::new).collect(Collectors.toSet());
            }

            Set<String> fields() {
                return Stream.of(fields.orElse("").split(",")).filter(field -> !Strings.isNullOrEmpty(field)).collect(Collectors.toSet());
            }
        }

        final class ComputationsGetResponse implements Message {
            public final List<Map<String, Object>> computations;

            ComputationsGetResponse(List<Map<String, Object>> computations) {
                this.computations = computations;
            }
        }
    }
}
