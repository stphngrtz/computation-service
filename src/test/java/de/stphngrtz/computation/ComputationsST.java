package de.stphngrtz.computation;

import akka.http.javadsl.model.StatusCodes;
import com.mongodb.client.MongoDatabase;
import de.stphngrtz.computation.model.Computation;
import de.stphngrtz.computation.model.Element;
import de.stphngrtz.computation.model.Structure;
import de.stphngrtz.computation.utils.cli.CommandLineInterface;
import de.stphngrtz.computation.utils.jackson.Jackson;
import de.stphngrtz.computation.utils.mongo.Mongo;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ComputationsST {

    private static final String[] ARGS = {};
    private static final CommandLineInterface CLI = new CommandLineInterface(ARGS);
    private static final MongoDatabase DB = Mongo.getDatabase(CLI.dbHostname(), CLI.dbPort(), CLI.dbInMemory());

    @BeforeClass
    public static void setupURL() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((aClass, s) -> Jackson.mapper()));
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
    public void GET_Request_for_existing_Computations() throws Exception {
        Computation.Id id1 = new Computation.Id();
        Computation.Id id2 = new Computation.Id();
        Computation.Id id3 = new Computation.Id();

        create(new Computation(id1, new Structure.Id(), new Element.Name("Element 1"), new Computation.Expression("Expression 1")));
        create(new Computation(id2, new Structure.Id(), new Element.Name("Element 2"), new Computation.Expression("Expression 2")));
        create(new Computation(id3, new Structure.Id(), new Element.Name("Element 3"), new Computation.Expression("Expression 3")));

        when()
                .get("/computations")
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
    public void GET_Request_for_existing_Computations_filtered_by_Ids() throws Exception {
        Computation.Id id1 = new Computation.Id();
        Computation.Id id2 = new Computation.Id();
        Computation.Id id3 = new Computation.Id();

        create(new Computation(id1, new Structure.Id(), new Element.Name("Element 1"), new Computation.Expression("Expression 1")));
        create(new Computation(id2, new Structure.Id(), new Element.Name("Element 2"), new Computation.Expression("Expression 2")));
        create(new Computation(id3, new Structure.Id(), new Element.Name("Element 3"), new Computation.Expression("Expression 3")));

        when()
                .get("/computations?ids={id1},{id2}", id1.toString(), id2.toString())
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
    public void GET_Request_for_non_existing_Computations() throws Exception {
        when()
                .get("/computations")
                .then()
                .statusCode(StatusCodes.OK.intValue())
                .and()
                .body("", empty());
    }

    @Test
    public void POST_Request_with_subsequent_GET_Request() throws Exception {
        Structure.Id structureId = new Structure.Id();
        Element.Name elementName = new Element.Name("Element 1");
        Computation.Expression expression = new Computation.Expression("Expression 1");

        String location = given()
                .contentType(JSON)
                .body(new Computation(null, structureId, elementName, expression))
                .when()
                .post("/computations")
                .then()
                .statusCode(StatusCodes.CREATED.intValue())
                .and()
                .extract()
                .header("Location");

        Computation computation = when()
                .get("/computations/{id}", location.substring(location.lastIndexOf("/") + 1, location.length()))
                .then()
                .statusCode(StatusCodes.OK.intValue())
                .and()
                .extract()
                .body()
                .as(Computation.class);

        assertThat(computation.structureId, is(equalTo(structureId)));
        assertThat(computation.elementName, is(equalTo(elementName)));
        assertThat(computation.expression, is(equalTo(expression)));
        assertThat(computation.status, is(equalTo(Computation.Status.NEW)));
        assertThat(computation.result, is(equalTo(Optional.empty())));
    }

    @Test
    public void GET_Request_for_non_existing_Computation() throws Exception {
        when()
                .get("/computations/MISSING_ID")
                .then()
                .statusCode(StatusCodes.NOT_FOUND.intValue());
    }

    @Test
    public void PUT_Requests_are_not_supported() throws Exception {
        when()
                .put("/computations/ANY_ID")
                .then()
                .statusCode(StatusCodes.METHOD_NOT_ALLOWED.intValue());
    }

    @Test
    public void DELETE_Request_for_existing_Computation() throws Exception {
        Computation.Id id = new Computation.Id();
        create(new Computation(id, new Structure.Id(), new Element.Name("Element 1"), new Computation.Expression("Expression 1")));

        when()
                .delete("/computations/{id}", id.toString())
                .then()
                .statusCode(StatusCodes.NO_CONTENT.intValue());
    }

    @Test
    public void DELETE_Request_for_non_existing_Computation() throws Exception {
        when()
                .delete("/computations/MISSING_ID")
                .then()
                .statusCode(StatusCodes.NO_CONTENT.intValue());
    }

    private static void create(Computation computation) {
        Computation.collection(DB).insertOne(computation);
    }
}
