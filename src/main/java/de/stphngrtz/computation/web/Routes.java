package de.stphngrtz.computation.web;

import akka.actor.ActorRef;
import akka.dispatch.Mapper;
import akka.http.javadsl.model.*;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.RejectionHandler;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.ValidationRejection;
import akka.http.javadsl.server.directives.LogEntry;
import akka.pattern.Patterns;
import akka.util.Timeout;
import de.stphngrtz.computation.actor.Web;
import de.stphngrtz.computation.model.Structure;
import de.stphngrtz.computation.utils.jackson.Jackson;
import scala.concurrent.ExecutionContextExecutor;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static akka.http.javadsl.server.Directives.*;

public class Routes {

    private static final RejectionHandler rejectionHandler = RejectionHandler.newBuilder()
            .handle(ValidationRejection.class, rejection -> logRequest(request -> LogEntry.error(rejection), () -> complete(StatusCodes.BAD_REQUEST)))
            .build();

    public static Route createRoute(ActorRef web, ExecutionContextExecutor dispatcher) {
        return logRequest(LogEntry::debug, () -> route(
                pathEndOrSingleSlash(() -> get(() -> complete("Computation-Service"))),
                pathPrefix("structures", () -> route(
                        pathEndOrSingleSlash(() -> route(
                                get(() -> parameterOptional("ids", ids -> parameterOptional("fields", fields -> completeWithFutureResponse(
                                        Patterns.ask(web, new Web.Protocol.StructuresGetRequest(ids, fields), Timeout.apply(5, TimeUnit.SECONDS))
                                        .map(with(Web.Protocol.StructuresGetResponse.class, structuresGetResponse ->
                                                HttpResponse.create()
                                                        .withStatus(StatusCodes.OK)
                                                        .withEntity(toEntity(structuresGetResponse.structures))
                                        ), dispatcher)
                                )))),
                                post(() -> handleRejections(rejectionHandler, () -> entity(Jackson.unmarshaller(Structure.class), structure -> extractUri(uri -> {
                                    Structure.Id id = new Structure.Id();
                                    web.tell(new Web.Protocol.StructureCreateRequest(structure.with(id)), ActorRef.noSender());
                                    return complete(HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(Location.create(uri.addPathSegment(id.toString()))));
                                }))))
                        )),
                        path(structureId -> route(
                                get(() -> completeWithFutureResponse(
                                        Patterns.ask(web, new Web.Protocol.StructureGetRequest(new Structure.Id(structureId)), Timeout.apply(5, TimeUnit.SECONDS))
                                        .map(with(Web.Protocol.StructureGetResponse.class, structureGetResponse -> {
                                            if (structureGetResponse.structure.isPresent())
                                                return HttpResponse.create().withStatus(StatusCodes.OK).withEntity(toEntity(structureGetResponse.structure.get()));
                                            else
                                                return HttpResponse.create().withStatus(StatusCodes.NOT_FOUND);

                                        }), dispatcher)
                                )),
                                put(() -> handleRejections(rejectionHandler, () -> entity(Jackson.unmarshaller(Structure.class), structure -> {
                                    web.tell(new Web.Protocol.StructureUpdateRequest(structure.with(new Structure.Id(structureId))), ActorRef.noSender());
                                    return complete(HttpResponse.create().withStatus(StatusCodes.NO_CONTENT));
                                }))),
                                delete(() -> {
                                    web.tell(new Web.Protocol.StructureDeleteRequest(new Structure.Id(structureId)), ActorRef.noSender());
                                    return complete(HttpResponse.create().withStatus(StatusCodes.NO_CONTENT));
                                })
                        ))
                )),
                pathPrefix("computations", () -> route(
                        pathEndOrSingleSlash(() -> route(
                                get(() -> complete("list of computations")),
                                post(() -> complete("new computation"))
                        )),
                        path(computationId -> route(
                                get(() -> complete("computation")),
                                put(() -> complete("updated computation")),
                                delete(() -> complete("deleted computation"))
                        ))
                ))
        ));
    }

    private static <T> Mapper<Object, HttpResponse> with(Class<T> clazz, Function<T, HttpResponse> f) {
        return new Mapper<Object, HttpResponse>() {
            @Override
            public HttpResponse apply(Object parameter) {
                return f.apply(to(clazz, parameter));
            }
        };
    }

    private static <T> T to(Class<T> clazz, Object o) {
        if (!clazz.isInstance(o))
            throw new IllegalArgumentException();

        return clazz.cast(o);
    }

    private static RequestEntity toEntity(Object value) {
        return HttpEntities.create(ContentTypes.APPLICATION_JSON, Jackson.writeAsString(value));
    }
}
