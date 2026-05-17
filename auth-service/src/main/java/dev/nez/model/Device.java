package dev.nez.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import java.util.Optional;

@RegisterForReflection
@Entity
@Table(name = "devices")
public class Device extends PanacheEntityBase {

    @Id
    @SequenceGenerator(
        name = "devicesSequence",
        sequenceName = "devices_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "devicesSequence")
    public Long id;

    @Column(length = 127, nullable = false, unique = true)
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

    public enum Status {
        ACTIVE, MAINTENANCE, BANNED, DECOMMISSIONED
    }

    public enum MessageType {
        JSON, PROTO
    }

    public Device() {}

    public Optional<String> getBatteryTopic() {
        return Optional.ofNullable(batteryTopic);
    }

    public static Uni<Device> findByHardwareId(String hardwareId) {
        return find("hardwareId", hardwareId).firstResult();
    }

    public Device(String hardwareId, String passwordHash, Status status, MessageType messageType, String topic, String batteryTopic) {
        this.hardwareId = hardwareId;
        this.passwordHash = passwordHash;
        this.status = status;
        this.messageType = messageType;
        this.topic = topic;
        this.batteryTopic = batteryTopic;
    }
}