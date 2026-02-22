package dev.nez;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;

@Entity
@Table(name = "devices")
public class Device extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String hardwareId;
    public String passwordHash;
    @Enumerated(EnumType.STRING)
    public Status status;
    public String topic;

    public static Uni<Device> findByHardwareId(String hardwareId) {
        return find("hardwareId", hardwareId).firstResult();
    }

    public enum  Status {
        ACTIVE,
        MAINTENANCE,
        BANNED,
        DECOMMISSIONED
    }
}