package de.stphngrtz.computation.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

public class Master extends AbstractActor {

    public static Props props() {
        return Props.create(Master.class, () -> new Master());
    }

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private Master() {
        receive(ReceiveBuilder.matchAny(message -> log.debug("Received: {}", message)).build());
    }
}
