package dev.nez.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;

import java.util.Optional;

@Entity
@Table(name = "devices")
public class Device extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String hardwareId;
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    public Status status;

    @Enumerated(EnumType.STRING)
    public MessageType messageType;

    public String topic;
    public String batteryTopic;

    public Device() {}

    public Device(
        String hardwareId,
        String passwordHash,
        Status status,
        MessageType messageType,
        String topic,
        String batteryTopic
    ) {
        this.hardwareId = hardwareId;
        this.passwordHash = passwordHash;
        this.status = status;
        this.messageType = messageType;
        this.topic = topic;
        this.batteryTopic = batteryTopic;
    }

    public Optional<String> getBatteryTopic() {
        return Optional.ofNullable(batteryTopic);
    }

    public static Uni<Device> findByHardwareId(String hardwareId) {
        return find("hardwareId", hardwareId).firstResult();
    }

    public enum  Status {
        ACTIVE,
        MAINTENANCE,
        BANNED,
        DECOMMISSIONED
    }

    public enum MessageType {
        JSON, PROTO
    }
}