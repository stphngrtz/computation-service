package de.stphngrtz.computation.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

public class Producer extends AbstractActor {

    public static Props props() {
        return Props.create(Producer.class, () -> new Producer());
    }

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private Producer() {
        receive(ReceiveBuilder.matchAny(message -> log.debug("Received: {}", message)).build());
    }
}
