package wdsr.exercise3.record.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import wdsr.exercise3.MyServer;
import wdsr.exercise3.record.Genre;
import wdsr.exercise3.record.Record;

public class RecordResourceTest {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8091;

    private MyServer server;
    private Client client;
    private WebTarget baseTarget;

    @Before
    public void setUp() {
        server = new MyServer(SERVER_HOST, SERVER_PORT);
        server.deploy(RecordApplication.class, "Records inventory", "/", "/");
        client = ClientBuilder.newClient();
        baseTarget = client.target("http://" + SERVER_HOST + ":" + SERVER_PORT);
    }

    @After
    public void tearDown() throws InterruptedException {
        server.stop();
        client.close();
        TimeUnit.MILLISECONDS.sleep(200);
    }

    @Test
    public void getRequestToRecords_shouldReturnEmptyList_whenNoRecordsAdded() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");

        // when
        List<Record> result = recordsTarget.request(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<List<Record>>() {});

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void getRequestToRecords_shouldReturnAllRecords_whenRecordsAdded() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        List<Record> records =
                Arrays.asList(new Record("Rastaman Vibration", "Bob Marley", Genre.REGGAE),
                        new Record("Eurosis", "Ska-P", Genre.SKA));

        for (Record r : records) {
            // when
            Response response =
                    recordsTarget.request().post(Entity.entity(r, MediaType.APPLICATION_XML_TYPE));

            // then
            assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
            response.close();
        }

        // when
        List<Record> result = recordsTarget.request(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<List<Record>>() {});

        // then
        assertEquals(records.size(), result.size());
    }

    @Test
    public void postRequestToRecords_shouldReturnHttp201_whenRecordAdded() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record record = new Record("Rastaman Vibration", "Bob Marley", Genre.REGGAE);

        // when
        Response response =
                recordsTarget.request().post(Entity.entity(record, MediaType.APPLICATION_XML_TYPE));


        // then
        assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
        response.close();
    }

    @Test
    public void postRequestToRecords_shouldReturnHttp400_whenIdAlreadySetOnRecord() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record record = new Record(300, "Rastaman Vibration", "Bob Marley", Genre.REGGAE);

        // when
        Response response =
                recordsTarget.request().post(Entity.entity(record, MediaType.APPLICATION_XML_TYPE));


        // then
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        response.close();
    }

    @Test
    public void getRequestToRecord_shouldReturnHttp200AndRecord_whenRecordExists() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record record = new Record("Rastaman Vibration", "Bob Marley", Genre.REGGAE);

        // when
        Response postResponse =
                recordsTarget.request().post(Entity.entity(record, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
        URI createdRecordResource = postResponse.getLocation();
        postResponse.close();

        // when
        WebTarget recordTarget = client.target(createdRecordResource);
        Record result = recordTarget.request(MediaType.APPLICATION_XML_TYPE).get(Record.class);

        // then
        assertEquals(record.getArtist(), result.getArtist());
        assertEquals(record.getTitle(), result.getTitle());
        assertEquals(record.getGenre(), result.getGenre());
    }

    @Test(expected = NotFoundException.class)
    public void getRequestToRecord_shouldReturnHttp404_whenRecordDoesNotExist() {
        // given
        WebTarget recordTarget = baseTarget.path("/records/1");

        // when
        recordTarget.request(MediaType.APPLICATION_XML_TYPE).get(Record.class);

        // then
    }

    @Test(expected = NotFoundException.class)
    public void putRequestToRecord_shouldReturnHttp404_whenRecordDoesNotExist() {
        // given
        WebTarget recordTarget = baseTarget.path("/records/1");
        Record record = new Record("Rastaman Vibration", "Bob Marley", Genre.REGGAE);

        // when
        recordTarget.request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(record, MediaType.APPLICATION_XML_TYPE), Void.class);

        // then
    }

    @Test
    public void putRequestToRecord_shouldReturnHttp204_whenRecordUpdated_recordHasNullID() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record record = new Record("Rastaman Vibration", "Bob Marley", Genre.DUB);

        // when
        Response postResponse =
                recordsTarget.request().post(Entity.entity(record, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
        URI createdRecordResource = postResponse.getLocation();
        postResponse.close();

        // when
        WebTarget recordTarget = client.target(createdRecordResource);
        Record updatedRecord = new Record("Rastaman Vibration", "Bob Marley", Genre.REGGAE);
        Response result = recordTarget.request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(updatedRecord, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.NO_CONTENT.getStatusCode(), result.getStatus());

        // when
        Record retrievedRecord =
                recordTarget.request(MediaType.APPLICATION_XML_TYPE).get(Record.class);

        // then
        assertEquals(updatedRecord.getGenre(), retrievedRecord.getGenre());
    }

    @Test
    public void putRequestToRecord_shouldReturnHttp204_whenRecordUpdated_recordHasSameIDAsPath() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record record = new Record("Rastaman Vibration", "Bob Marley", Genre.DUB);

        // when
        Response postResponse =
                recordsTarget.request().post(Entity.entity(record, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
        URI createdRecordResource = postResponse.getLocation();
        postResponse.close();

        // when
        WebTarget recordTarget = client.target(createdRecordResource);
        String createdRecordPath = createdRecordResource.getPath();
        int id = Integer
                .valueOf(createdRecordPath.substring(createdRecordPath.lastIndexOf("/") + 1));
        Record updatedRecord = new Record(id, "Rastaman Vibration", "Bob Marley", Genre.REGGAE);
        Response result = recordTarget.request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(updatedRecord, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.NO_CONTENT.getStatusCode(), result.getStatus());

        // when
        Record retrievedRecord =
                recordTarget.request(MediaType.APPLICATION_XML_TYPE).get(Record.class);

        // then
        assertEquals(updatedRecord.getGenre(), retrievedRecord.getGenre());
    }

    @Test
    public void putRequestToRecord_shouldReturnHttp400_whenIdMismatch() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record originalRecord = new Record("Rastaman Vibration", "Bob Marley", Genre.DUB);

        // when
        Response postResponse = recordsTarget.request()
                .post(Entity.entity(originalRecord, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
        URI createdRecordResource = postResponse.getLocation();
        postResponse.close();

        // when
        WebTarget recordTarget = client.target(createdRecordResource);
        String createdRecordPath = createdRecordResource.getPath();
        int id = Integer
                .valueOf(createdRecordPath.substring(createdRecordPath.lastIndexOf("/") + 1));
        Record updatedRecord = new Record(id + 1, "Rastaman Vibration", "Bob Marley", Genre.REGGAE);
        Response result = recordTarget.request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(updatedRecord, MediaType.APPLICATION_XML_TYPE));
        result.close();

        // then
        assertEquals("ID in request URL and value of ID field (if present) must be the same",
                Status.BAD_REQUEST.getStatusCode(), result.getStatus());

        // when
        Record retrievedRecord =
                recordTarget.request(MediaType.APPLICATION_XML_TYPE).get(Record.class);

        // then
        assertEquals(originalRecord.getGenre(), retrievedRecord.getGenre());
    }

    @Test
    public void putRequestToRecord_shouldBeIdempotent() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record record = new Record("Rastaman Vibration", "Bob Marley", Genre.DUB);

        // when
        Response postResponse =
                recordsTarget.request().post(Entity.entity(record, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
        URI createdRecordResource = postResponse.getLocation();
        postResponse.close();

        // when
        WebTarget recordTarget = client.target(createdRecordResource);
        Record updatedRecord = new Record("Rastaman Vibration", "Bob Marley", Genre.REGGAE);
        Response putResponse = recordTarget.request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(updatedRecord, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.NO_CONTENT.getStatusCode(), putResponse.getStatus());

        // when
        // Invoke PUT again with the same parameters to check idempotence
        putResponse = recordTarget.request(MediaType.APPLICATION_XML_TYPE)
                .put(Entity.entity(updatedRecord, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.NO_CONTENT.getStatusCode(), putResponse.getStatus());

        // when
        Record retrievedRecord =
                recordTarget.request(MediaType.APPLICATION_XML_TYPE).get(Record.class);

        // then
        assertEquals(updatedRecord.getGenre(), retrievedRecord.getGenre());
    }

    @Test(expected = NotFoundException.class)
    public void deleteRequestToRecord_shouldReturnHttp404_whenRecordDoesNotExist() {
        // given
        WebTarget recordTarget = baseTarget.path("/records/55");

        // when
        recordTarget.request(MediaType.APPLICATION_XML_TYPE).delete(Void.class);

        // then
    }

    @Test
    public void deleteRequestToRecord_shouldReturnHttp204_whenRecordDeletedSuccessfully() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record record = new Record("Rastaman Vibration", "Bob Marley", Genre.DUB);

        // when
        Response postResponse =
                recordsTarget.request().post(Entity.entity(record, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
        URI createdRecordResource = postResponse.getLocation();
        postResponse.close();

        // when
        WebTarget recordTarget = client.target(createdRecordResource);
        Response deleteResponse = recordTarget.request(MediaType.APPLICATION_XML_TYPE).delete();

        // then
        assertEquals(Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // when
        Response getResponse = recordTarget.request(MediaType.APPLICATION_XML_TYPE).get();

        // then
        assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
    }

    @Test
    public void deleteRequestToRecord_shouldReturnHttp404_whenRecordAlreadyDeleted() {
        // given
        WebTarget recordsTarget = baseTarget.path("/records");
        Record record = new Record("Rastaman Vibration", "Bob Marley", Genre.DUB);

        // when
        Response postResponse =
                recordsTarget.request().post(Entity.entity(record, MediaType.APPLICATION_XML_TYPE));

        // then
        assertEquals(Status.CREATED.getStatusCode(), postResponse.getStatus());
        URI createdRecordResource = postResponse.getLocation();
        postResponse.close();

        // when
        WebTarget recordTarget = client.target(createdRecordResource);
        Response deleteResponse = recordTarget.request(MediaType.APPLICATION_XML_TYPE).delete();

        // then
        assertEquals(Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // when
        deleteResponse = recordTarget.request(MediaType.APPLICATION_XML_TYPE).delete();

        // then
        assertEquals(Status.NOT_FOUND.getStatusCode(), deleteResponse.getStatus());
    }
}
