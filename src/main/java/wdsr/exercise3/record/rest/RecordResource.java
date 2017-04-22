package wdsr.exercise3.record.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wdsr.exercise3.record.Record;
import wdsr.exercise3.record.RecordInventory;

@Path("/records")
public class RecordResource {
    private static final Logger log = LoggerFactory.getLogger(RecordResource.class);

    @Inject
    private RecordInventory recordInventory;

    /**
     * GET https://localhost:8091/records<br>
     * Returns a list of all records (as application/xml)<br>
     * Response status: HTTP 200<br>
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getRequestToRecords() {
        GenericEntity<List<Record>> entity =
                new GenericEntity<List<Record>>(recordInventory.getRecords()) {};
        log.info("GET https://localhost:8091/records");
        return Response.ok(entity).build();
    }

    /**
     * POST https://localhost:8091/records<br>
     * Creates a new record, returns ID of the new record.<br>
     * Consumes: record (as application/xml), ID must be null.<br>
     * Response status if ok: HTTP 201, Location header points to the newly created resource.<br>
     * Response status if submitted record has ID set: HTTP 400<br>
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response postRequestToRecords(Record record, @Context UriInfo uriInfo) {
        if (record.getId() != null) {
            log.error("POST https://localhost:8091/records [" + Status.BAD_REQUEST + "]");
            return Response.status(Status.BAD_REQUEST).build();
        }

        log.info("POST https://localhost:8091/records");
        recordInventory.addRecord(record);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        builder.path(Integer.toString(record.getId()));
        return Response.created(builder.build()).build();
    }

    /**
     * GET https://localhost:8091/records/{id}<br>
     * Returns an existing record (as application/xml)<br>
     * Response status if ok: HTTP 200<br>
     * Response status if {id} is not known: HTTP 404<br>
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getRequestToRecords(@PathParam(value = "id") int id) {
        Record record = recordInventory.getRecord(id);

        if (record == null) {
            log.error("GET https://localhost:8091/records/" + id + " [" + Status.NOT_FOUND + "]");
            return Response.status(Status.NOT_FOUND).build();
        }

        log.info("GET https://localhost:8091/records/" + id);
        return Response.ok(record).build();
    }

    /**
     * PUT https://localhost:8091/records/{id}<br>
     * Replaces an existing record in entirety.<br>
     * Submitted record (as application/xml) must have null ID or ID must be identical to {id} in
     * the path.<br>
     * Response status if ok: HTTP 204<br>
     * Response status if {id} is not known: HTTP 404<br>
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response putRequestToRecord(Record record, @PathParam(value = "id") int id) {
        if (record.getId() != null && id != record.getId()) {
            log.error("PUT https://localhost:8091/records/" + id + " [" + Status.BAD_REQUEST + "]");
            String message = "Product ID is different in request path and the message body.";
            return Response.status(Status.BAD_REQUEST).entity(message).build();
        }

        if (recordInventory.updateRecord(id, record)) {
            log.info("PUT https://localhost:8091/records/" + id);
            return Response.noContent().build();
        }

        log.error("PUT https://localhost:8091/records/" + id + " [" + Status.NOT_FOUND + "]");
        return Response.status(Status.NOT_FOUND).build();
    }

    /**
     * DELETE https://localhost:8091/records/{id}<br>
     * Deletes an existing record.<br>
     * Response status if ok: HTTP 204<br>
     * Response status if {id} is not known: HTTP 404<br>
     */
    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response deleteRequestToRecord(@PathParam(value = "id") int id) {
        if (recordInventory.deleteRecord(id)) {
            log.info("DELETE https://localhost:8091/records/" + id);
            return Response.noContent().build();
        }

        log.error("DELETE https://localhost:8091/records/" + id + " [" + Status.NOT_FOUND + "]");
        return Response.status(Status.NOT_FOUND).build();
    }

}
