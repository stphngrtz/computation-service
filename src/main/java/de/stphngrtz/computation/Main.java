package de.stphngrtz.computation;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.stphngrtz.computation.actor.Master;
import de.stphngrtz.computation.actor.Producer;
import de.stphngrtz.computation.actor.Web;
import de.stphngrtz.computation.actor.Worker;
import de.stphngrtz.computation.utils.cli.CommandLineInterface;
import de.stphngrtz.computation.utils.mongo.Mongo;
import de.stphngrtz.computation.web.Routes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletionStage;

public class Main {

    public static void main(String[] args) {
        CommandLineInterface cli = new CommandLineInterface(args);

        if (cli.wantsHelp()) {
            cli.printHelp();
        } else {
            String hostname = getHostname();

            if (cli.startMaster()) {
                startMaster(hostname, cli.masterPort());
            }
            if (cli.startWorker()) {
                startWorker(hostname, cli.workerPort(), cli.masterHostname(), cli.masterPort(), cli.dbHostname(), cli.dbPort());
            }
            if (cli.startWeb()) {
                startWeb(hostname, cli.webPort(), cli.httpPort(), cli.masterHostname(), cli.masterPort(), cli.dbHostname(), cli.dbPort());
            }
        }
    }

    private static String getHostname() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName().toLowerCase();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return hostname;
    }

    private static final String ACTOR_SYSTEM_NAME_MASTER = "Computation-Service-Master";
    private static final String ACTOR_SYSTEM_NAME_WORKER = "Computation-Service-Worker";
    private static final String ACTOR_SYSTEM_NAME_WEB = "Computation-Service-Web";

    private static Runnable startMaster(String masterHostname, int masterPort) {
        Config config = ConfigFactory.empty()
                .withFallback(ConfigFactory.parseString("akka.cluster.seed-nodes=[\"akka.tcp://" + ACTOR_SYSTEM_NAME_MASTER + "@" + masterHostname + ":" + masterPort + "\"]"))
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + masterHostname))
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + masterPort))
                .withFallback(ConfigFactory.parseResourcesAnySyntax("master"))
                .withFallback(ConfigFactory.load("default"));

        ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME_MASTER, config);
        system.actorOf(ClusterSingletonManager.props(
                Master.props(),
                PoisonPill.getInstance(),
                ClusterSingletonManagerSettings.create(system)
        ), "master");

        return system::terminate;
    }

    private static Runnable startWorker(String workerHostname, int workerPort, String masterHostname, int masterPort, String dbHostname, int dbPort) {
        Config config = ConfigFactory.empty()
                .withFallback(ConfigFactory.parseString("akka.cluster.client.initial-contacts=[\"akka.tcp://" + ACTOR_SYSTEM_NAME_MASTER + "@" + masterHostname + ":" + masterPort + "/system/receptionist\"]"))
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + workerHostname))
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + workerPort))
                .withFallback(ConfigFactory.parseResourcesAnySyntax("worker"))
                .withFallback(ConfigFactory.load("default"));

        MongoDatabase database = Mongo.getDatabase(dbHostname, dbPort);

        ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME_WORKER, config);
        system.actorOf(Worker.props(database), "worker");

        return () -> {
            Mongo.close();
            system.terminate();
        };
    }

    private static Runnable startWeb(String webHostname, int webPort, int httpPort, String masterHostname, int masterPort, String dbHostname, int dbPort) {
        Config config = ConfigFactory.empty()
                .withFallback(ConfigFactory.parseString("akka.cluster.client.initial-contacts=[\"akka.tcp://" + ACTOR_SYSTEM_NAME_MASTER + "@" + masterHostname + ":" + masterPort + "/system/receptionist\"]"))
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname=" + webHostname))
                .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + webPort))
                .withFallback(ConfigFactory.parseResourcesAnySyntax("web"))
                .withFallback(ConfigFactory.load("default"));

        MongoDatabase database = Mongo.getDatabase(dbHostname, dbPort);

        ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME_WEB, config);
        ActorRef producer = system.actorOf(Producer.props(), "producer");
        ActorRef web = system.actorOf(Web.props(producer, database), "web");

        Http http = Http.get(system);
        ActorMaterializer materializer = ActorMaterializer.create(system);
        CompletionStage<ServerBinding> binding = http.bindAndHandle(
                Routes.createRoute(web, system.dispatcher()).flow(system, materializer),
                ConnectHttp.toHost("0.0.0.0", httpPort),
                materializer
        );

        return () -> {
            Mongo.close();
            binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> system.terminate());
        };
    }
}
