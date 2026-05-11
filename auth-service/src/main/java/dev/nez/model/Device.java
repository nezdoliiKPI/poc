package dev.nez.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;

import java.util.Optional;

@RegisterForReflection
@Entity
@Table(name = "devices")
public class Device extends PanacheEntity {
    @Column(unique = true, nullable = false, length = 127)
    public String hardwareId;

    @Column(length = 127)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 31)
    public Status status;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    public MessageType messageType;

    @Column(length = 127)
    public String topic;

    @Column(length = 127)
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