package dev.nez.panel.dto;

import jakarta.ws.rs.BadRequestException;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ConfigUpdate(
    Integer airJsonCount,
    Integer airProtoCount,
    Integer powerJsonCount,
    Integer powerProtoCount,
    Integer smokeJsonCount,
    Integer smokeProtoCount
) {
    public ConfigUpdate {
        // --- AIR ---
        if (airJsonCount == null) {
            throw new BadRequestException("Field 'airJsonCount' cannot be null");
        }
        if (airJsonCount < 0) {
            throw new BadRequestException("Field 'airJsonCount' cannot be negative");
        }

        if (airProtoCount == null) {
            throw new BadRequestException("Field 'airProtoCount' cannot be null");
        }
        if (airProtoCount < 0) {
            throw new BadRequestException("Field 'airProtoCount' cannot be negative");
        }

        // --- POWER ---
        if (powerJsonCount == null) {
            throw new BadRequestException("Field 'powerJsonCount' cannot be null");
        }
        if (powerJsonCount < 0) {
            throw new BadRequestException("Field 'powerJsonCount' cannot be negative");
        }

        if (powerProtoCount == null) {
            throw new BadRequestException("Field 'powerProtoCount' cannot be null");
        }
        if (powerProtoCount < 0) {
            throw new BadRequestException("Field 'powerProtoCount' cannot be negative");
        }

        // --- SMOKE ---
        if (smokeJsonCount == null) {
            throw new BadRequestException("Field 'smokeJsonCount' cannot be null");
        }
        if (smokeJsonCount < 0) {
            throw new BadRequestException("Field 'smokeJsonCount' cannot be negative");
        }

        if (smokeProtoCount == null) {
            throw new BadRequestException("Field 'smokeProtoCount' cannot be null");
        }
        if (smokeProtoCount < 0) {
            throw new BadRequestException("Field 'smokeProtoCount' cannot be negative");
        }
    }
}
