package dev.nez.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;

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

    @Column(name = "hardware_id", length = 127, nullable = false, unique = true)
    public String hardwareId;

    @JsonIgnore
    @Column(name = "password_hash", length = 127)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 31)
    public Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 15)
    public MessageType messageType;

    @Column(name = "topic", length = 127)
    public String topic;

    @Column(name = "battery_topic", length = 127)
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