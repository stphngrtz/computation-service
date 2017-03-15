package de.stphngrtz.computation.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.mongodb.client.MongoDatabase;

public class Worker extends AbstractActor {

    public static Props props(MongoDatabase database) {
        return Props.create(Worker.class, () -> new Worker(database));
    }

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private Worker(MongoDatabase database) {
        receive(ReceiveBuilder.matchAny(message -> log.debug("Received: {}", message)).build());
    }
}
