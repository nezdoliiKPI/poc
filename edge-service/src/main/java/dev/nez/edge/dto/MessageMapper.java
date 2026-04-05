package dev.nez.edge.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import com.google.protobuf.InvalidProtocolBufferException;

import dev.nez.edge.data.AirQualityData;
import dev.nez.edge.data.BatteryData;
import dev.nez.edge.data.PowerConsumptionData;
import dev.nez.edge.data.SmokeDetectorData;
import dev.nez.edge.dto.mqtt.AirQuality;
import dev.nez.edge.dto.mqtt.Battery;

import dev.nez.edge.exception.DecodeMessageException;
import io.vertx.core.json.DecodeException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.google.protobuf.util.Timestamps;

import java.io.IOException;

@Singleton
public class MessageMapper {
    private final ObjectMapper objectMapper;
    private final JsonMessageReader<AirQuality> jsonAirQualityReader;
    private final JsonMessageReader<Battery> jsonBatteryReader;

    @Inject
    public MessageMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.jsonAirQualityReader = new JsonMessageReader<>(AirQuality.class);
        this.jsonBatteryReader = new JsonMessageReader<>(Battery.class);
    }

    public PowerConsumptionData fromProtoPowerConsumption(byte[] payload) throws DecodeMessageException {
        try {
            return PowerConsumptionData.parseFrom(payload)
                .toBuilder()
                .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        } catch (final InvalidProtocolBufferException e) {
            throw new DecodeMessageException(e.getMessage(), e);
        }
    }

    public AirQualityData fromJsonAirQuality(byte[] payload) throws DecodeMessageException {
        final AirQuality dto = jsonAirQualityReader.fromJson(payload);

        return AirQualityData.newBuilder()
            .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
            .setDeviceId(dto.id())
            .setCo2(dto.co2())
            .setPm25(dto.pm25())
            .setPm10(dto.pm10())
            .setTvoc(dto.tvoc())
            .setTemperature(dto.t())
            .setHumidity(dto.h())
            .build();
    }

    public AirQualityData fromProtoAirQuality(byte[] payload) throws DecodeMessageException {
        try {
            return AirQualityData.parseFrom(payload)
                .toBuilder()
                .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        } catch (final InvalidProtocolBufferException e) {
            throw new DecodeMessageException(e.getMessage(), e);
        }
    }

    public BatteryData fromJsonBattery(byte[] payload) throws DecodeMessageException {
        final Battery dto = jsonBatteryReader.fromJson(payload);

        return BatteryData.newBuilder()
            .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
            .setDeviceId(dto.id())
            .setVal(dto.v())
            .build();
    }

    public BatteryData fromProtoBattery(byte[] payload) throws DecodeMessageException {
        try {
            return BatteryData.parseFrom(payload)
                .toBuilder()
                .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        } catch (final InvalidProtocolBufferException e) {
            throw new DecodeMessageException(e.getMessage(), e);
        }
    }

    public SmokeDetectorData fromProtoSmoke(byte[] payload) throws DecodeMessageException {
        try {
            return SmokeDetectorData.parseFrom(payload)
                .toBuilder()
                .setTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        } catch (final InvalidProtocolBufferException e) {
            throw new DecodeMessageException(e.getMessage(), e);
        }
    }


    private class JsonMessageReader<T> {
        private final ObjectReader reader;

        public JsonMessageReader(Class<T> clazz) {
            this.reader = objectMapper.readerFor(clazz);
        }

        public T fromJson(byte[] payload) throws DecodeMessageException {
            try {
                return reader.readValue(payload);
            } catch (final DecodeException | IOException e) {
                throw new DecodeMessageException(e.getMessage(), e);
            }
        }
    }

    @Deprecated
    public <T> T fromJson(byte[] payload, Class<T> clazz) throws DecodeMessageException {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (final DecodeException | IOException e) {
            throw new DecodeMessageException(e.getMessage(), e);
        }
    }
}



