package de.stphngrtz.computation;

import akka.http.javadsl.model.StatusCodes;
import com.google.common.graph.Graph;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import de.stphngrtz.computation.model.Definition;
import de.stphngrtz.computation.model.Element;
import de.stphngrtz.computation.model.Structure;
import de.stphngrtz.computation.utils.cli.CommandLineInterface;
import de.stphngrtz.computation.utils.guava.Graphs;
import de.stphngrtz.computation.utils.jackson.Jackson;
import de.stphngrtz.computation.utils.mongo.Mongo;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.stphngrtz.computation.utils.guava.Graphs.Builder.node;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StructuresST {

    private static final String[] ARGS = {};
    private static final CommandLineInterface CLI = new CommandLineInterface(ARGS);
    private static final MongoDatabase DB = Mongo.getDatabase(CLI.dbHostname(), CLI.dbPort(), CLI.dbInMemory());

    @BeforeClass
    public static void setupURL() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> Jackson.mapper()));

        // Workaround for avoiding an error while reading from Fongo without an earlier write..
        Structure.Id id = new Structure.Id();
        create(new Structure(id, Graphs.newGraph()));
        delete(id);
    }

    @Before
    public void setUp() throws Exception {
        Main.main(ARGS);
    }

    @After
    public void tearDown() throws Exception {
        Main.exit();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void GET_Request_for_existing_Structures() throws Exception {
        Structure.Id id1 = new Structure.Id();
        Structure.Id id2 = new Structure.Id();
        Structure.Id id3 = new Structure.Id();

        create(new Structure(id1, Graphs.newGraph()));
        create(new Structure(id2, Graphs.newGraph()));
        create(new Structure(id3, Graphs.newGraph()));

        when()
                .get("/structures")
                .then()
                .statusCode(StatusCodes.OK.intValue())
                .and()
                .body("", containsInAnyOrder(
                        hasEntry("id", id1.toString()),
                        hasEntry("id", id2.toString()),
                        hasEntry("id", id3.toString())
                ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void GET_Request_for_existing_Structures_filtered_by_Ids() throws Exception {
        Structure.Id id1 = new Structure.Id();
        Structure.Id id2 = new Structure.Id();
        Structure.Id id3 = new Structure.Id();

        create(new Structure(id1, Graphs.newGraph()));
        create(new Structure(id2, Graphs.newGraph()));
        create(new Structure(id3, Graphs.newGraph()));

        when()
                .get("/structures?ids={id1},{id2}", id1.toString(), id2.toString())
                .then()
                .statusCode(StatusCodes.OK.intValue())
                .and()
                .body("", containsInAnyOrder(
                        hasEntry("id", id1.toString()),
                        hasEntry("id", id2.toString())
                ));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void GET_Request_for_non_existing_Structures() throws Exception {
        when()
                .get("/structures")
                .then()
                .statusCode(StatusCodes.OK.intValue())
                .and()
                .body("", empty());
    }

    @Test
    public void POST_Request_with_subsequent_GET_Request() throws Exception {
        Graph<Element> elements = Graphs.builder(
                node(new Element("Element 1", with(new Definition("Definition 1", new BigDecimal(10)))),
                        node(new Element("Element 2", with(new Definition("Definition 2", new BigDecimal(20)))))
                )
        );

        String location = given()
                .contentType(JSON)
                .body(new Structure(null, elements))
                .when()
                .post("/structures")
                .then()
                .statusCode(StatusCodes.CREATED.intValue())
                .and()
                .extract()
                .header("Location");

        Structure structure = when()
                .get("/structures/{id}", location.substring(location.lastIndexOf("/") + 1, location.length()))
                .then()
                .statusCode(StatusCodes.OK.intValue())
                .and()
                .extract()
                .body()
                .as(Structure.class);

        assertThat(structure.elements, is(equalTo(elements)));
    }

    @Test
    public void GET_Request_for_non_existing_Structure() throws Exception {
        when()
                .get("/structures/MISSING_ID")
                .then()
                .statusCode(StatusCodes.NOT_FOUND.intValue());
    }

    @Test
    public void PUT_Request_for_existing_Structure() throws Exception {
        Structure.Id id = new Structure.Id();
        create(new Structure(id, Graphs.newGraph()));

        Graph<Element> elements = Graphs.builder(
                node(new Element("Element 1", with(new Definition("Definition 1", new BigDecimal(10)))),
                        node(new Element("Element 2", with(new Definition("Definition 2", new BigDecimal(20)))))
                )
        );

        given()
                .contentType(JSON)
                .body(new Structure(null, elements))
                .when()
                .put("/structures/{id}", id.toString())
                .then()
                .statusCode(StatusCodes.NO_CONTENT.intValue());

        Structure structure = when()
                .get("/structures/{id}", id.toString())
                .then()
                .statusCode(StatusCodes.OK.intValue())
                .and()
                .extract()
                .body()
                .as(Structure.class);

        assertThat(structure.id, is(equalTo(id)));
        assertThat(structure.elements, is(equalTo(elements)));
    }

    @Test
    public void PUT_Request_for_non_existing_Structure() throws Exception {
        Graph<Element> elements = Graphs.builder(
                node(new Element("Element 1", with(new Definition("Definition 1", new BigDecimal(10)))),
                        node(new Element("Element 2", with(new Definition("Definition 2", new BigDecimal(20)))))
                )
        );

        given()
                .contentType(JSON)
                .body(new Structure(null, elements))
                .when()
                .put("/structures/MISSING_ID")
                .then()
                .statusCode(StatusCodes.NO_CONTENT.intValue());

        Structure structure = when()
                .get("/structures/MISSING_ID")
                .then()
                .statusCode(StatusCodes.OK.intValue())
                .and()
                .extract()
                .body()
                .as(Structure.class);

        assertThat(structure.id, is(equalTo(new Structure.Id("MISSING_ID"))));
        assertThat(structure.elements, is(equalTo(elements)));
    }

    @Test
    public void DELETE_Request_for_existing_Structure() throws Exception {
        Structure.Id id = new Structure.Id();
        create(new Structure(id, Graphs.newGraph()));

        when()
                .delete("/structures/{id}", id.toString())
                .then()
                .statusCode(StatusCodes.NO_CONTENT.intValue());
    }

    @Test
    public void DELETE_Request_for_non_existing_Structure() throws Exception {
        when()
                .delete("/structures/MISSING_ID")
                .then()
                .statusCode(StatusCodes.NO_CONTENT.intValue());
    }

    private static Set<Definition> with(Definition... definitions) {
        return Stream.of(definitions).collect(Collectors.toSet());
    }

    private static void create(Structure structure) {
        Structure.collection(DB).insertOne(structure);
    }

    private static void delete(Structure.Id id) {
        Structure.collection(DB).deleteOne(Filters.eq(Structure.Fields.id, id));
    }
}
