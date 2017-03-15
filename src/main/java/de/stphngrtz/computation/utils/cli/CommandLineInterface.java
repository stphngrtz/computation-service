package de.stphngrtz.computation.utils.cli;

import org.apache.commons.cli.*;

public class CommandLineInterface {

    private static final Options OPTIONS = new Options()
            .addOption(Option.builder().longOpt("master").desc("Starting Service as Master-Node").build())
            .addOption(Option.builder().longOpt("worker").desc("Starting Service as Worker-Node").build())
            .addOption(Option.builder().longOpt("web").desc("Starting Service as Web-Node").build())
            .addOption(Option.builder().longOpt("?").desc("Prints this :)").build())
            .addOption(Option.builder().longOpt("master-hostname").hasArg().argName("hostname").desc("Hostname of Master-Node").build())
            .addOption(Option.builder().longOpt("master-port").hasArg().argName("port").desc("Port of Master-Node").build())
            .addOption(Option.builder().longOpt("worker-port").hasArg().argName("port").desc("Port of Worker-Node").build())
            .addOption(Option.builder().longOpt("web-port").hasArg().argName("port").desc("Port of Web-Node").build())
            .addOption(Option.builder().longOpt("http-port").hasArg().argName("port").desc("HTTP-Port for Web-Node").build())
            .addOption(Option.builder().longOpt("db-hostname").hasArg().argName("hostname").desc("Hostname of Database").build())
            .addOption(Option.builder().longOpt("db-port").hasArg().argName("port").desc("Port of Database").build());

    private final CommandLine cl;

    public CommandLineInterface(String[] args) {
        try {
            cl = new DefaultParser().parse(OPTIONS, args);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean wantsHelp() {
        return cl.hasOption("?");
    }

    public void printHelp() {
        new HelpFormatter().printHelp("<executable>", OPTIONS);
    }

    public boolean startMaster() {
        return cl.hasOption("master") || (!cl.hasOption("worker") && !cl.hasOption("web"));
    }

    public boolean startWorker() {
        return cl.hasOption("worker") || (!cl.hasOption("master") && !cl.hasOption("web"));
    }

    public boolean startWeb() {
        return cl.hasOption("web") || (!cl.hasOption("master") && !cl.hasOption("worker"));
    }

    public String masterHostname() {
        return cl.getOptionValue("master-hostname", "localhost");
    }

    public int masterPort() {
        return Integer.valueOf(cl.getOptionValue("master-port", "2551"));
    }

    public int workerPort() {
        return Integer.valueOf(cl.getOptionValue("worker-port", "2561"));
    }

    public int webPort() {
        return Integer.valueOf(cl.getOptionValue("web-port", "2571"));
    }

    public int httpPort() {
        return Integer.valueOf(cl.getOptionValue("http-port", "8080"));
    }

    public String dbHostname() {
        return cl.getOptionValue("db-hostname", "localhost");
    }

    public int dbPort() {
        return Integer.valueOf(cl.getOptionValue("db-port", "27017"));
    }
}
