package dev.nez.monitoring.resource;

import dev.nez.monitoring.model.Device;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/devices")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Valid
public class DeviceResource {

    @GET
    public Uni<List<Device>> getAll() {
        return Device.listAll();
    }

    @GET
    @Path("/{id}")
    public Uni<Device> getById(@PathParam("id") long id) {
        return Device.findById(id);
    }

    @GET
    @Path("/hardware/{hardwareId}")
    public Uni<Device> getByHardwareId(@PathParam("hardwareId") String hardwareId) {
        return Device.findByHardwareId(hardwareId);
    }

    @GET
    @Path("/status/{status}")
    public Uni<List<Device>> getByStatus(@PathParam("status") Device.Status status) {
        return Device.list("status", status);
    }
}