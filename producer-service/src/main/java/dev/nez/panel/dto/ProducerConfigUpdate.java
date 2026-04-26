package dev.nez.panel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ProducerConfigUpdate(
    @NotNull(message = "Field 'airJsonCount' cannot be null")
    @Min(value = 0, message = "Field 'airJsonCount' cannot be negative")
    @Max(value = 5000, message = "Field 'airJsonCount' cannot exceed 5000")
    Integer airJsonCount,

    @NotNull(message = "Field 'airProtoCount' cannot be null")
    @Min(value = 0, message = "Field 'airProtoCount' cannot be negative")
    @Max(value = 5000, message = "Field 'airProtoCount' cannot exceed 5000")
    Integer airProtoCount,

    @NotNull(message = "Field 'powerJsonCount' cannot be null")
    @Min(value = 0, message = "Field 'powerJsonCount' cannot be negative")
    @Max(value = 5000, message = "Field 'powerJsonCount' cannot exceed 5000")
    Integer powerJsonCount,

    @NotNull(message = "Field 'powerProtoCount' cannot be null")
    @Min(value = 0, message = "Field 'powerProtoCount' cannot be negative")
    @Max(value = 5000, message = "Field 'powerProtoCount' cannot exceed 5000")
    Integer powerProtoCount,

    @NotNull(message = "Field 'smokeJsonCount' cannot be null")
    @Min(value = 0, message = "Field 'smokeJsonCount' cannot be negative")
    @Max(value = 5000, message = "Field 'smokeJsonCount' cannot exceed 5000")
    Integer smokeJsonCount,

    @NotNull(message = "Field 'smokeProtoCount' cannot be null")
    @Min(value = 0, message = "Field 'smokeProtoCount' cannot be negative")
    @Max(value = 5000, message = "Field 'smokeProtoCount' cannot exceed 5000")
    Integer smokeProtoCount
) {
}
