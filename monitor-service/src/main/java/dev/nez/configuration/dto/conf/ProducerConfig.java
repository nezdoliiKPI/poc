package dev.nez.configuration.dto.conf;

import io.smallrye.common.constraint.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProducerConfig(
    @NotNull(message = "Field 'airJsonCount' cannot be null")
    @Min(value = 0, message = "Field 'airJsonCount' cannot be negative")
    @Max(value = 2000, message = "Field 'airJsonCount' cannot exceed 2000")
    Integer airJsonCount,

    @NotNull(message = "Field 'airProtoCount' cannot be null")
    @Min(value = 0, message = "Field 'airProtoCount' cannot be negative")
    @Max(value = 2000, message = "Field 'airProtoCount' cannot exceed 2000")
    Integer airProtoCount,

    @NotNull(message = "Field 'powerJsonCount' cannot be null")
    @Min(value = 0, message = "Field 'powerJsonCount' cannot be negative")
    @Max(value = 2000, message = "Field 'powerJsonCount' cannot exceed 2000")
    Integer powerJsonCount,

    @NotNull(message = "Field 'powerProtoCount' cannot be null")
    @Min(value = 0, message = "Field 'powerProtoCount' cannot be negative")
    @Max(value = 2000, message = "Field 'powerProtoCount' cannot exceed 2000")
    Integer powerProtoCount,

    @NotNull(message = "Field 'smokeJsonCount' cannot be null")
    @Min(value = 0, message = "Field 'smokeJsonCount' cannot be negative")
    @Max(value = 2000, message = "Field 'smokeJsonCount' cannot exceed 2000")
    Integer smokeJsonCount,

    @NotNull(message = "Field 'smokeProtoCount' cannot be null")
    @Min(value = 0, message = "Field 'smokeProtoCount' cannot be negative")
    @Max(value = 2000, message = "Field 'smokeProtoCount' cannot exceed 2000")
    Integer smokeProtoCount,

    @NotNull(message = "Field 'tempJsonCount' cannot be null")
    @Min(value = 0, message = "Field 'tempJsonCount' cannot be negative")
    @Max(value = 2000, message = "Field 'tempJsonCount' cannot exceed 2000")
    Integer tempJsonCount,

    @NotNull(message = "Field 'tempProtoCount' cannot be null")
    @Min(value = 0, message = "Field 'tempProtoCount' cannot be negative")
    @Max(value = 2000, message = "Field 'tempProtoCount' cannot exceed 2000")
    Integer tempProtoCount,

    @Nullable
    Float intensity
) {
}
