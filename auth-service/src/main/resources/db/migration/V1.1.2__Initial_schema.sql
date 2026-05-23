CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- ==========================================
-- PowerConsumption
-- ==========================================
CREATE SEQUENCE power_consumption_seq INCREMENT BY 1;

CREATE TABLE power_consumption (
   id BIGINT DEFAULT nextval('power_consumption_seq'),

   device_id BIGINT NOT NULL,
   voltage REAL,
   current REAL,
   power REAL,
   time_date TIMESTAMPTZ NOT NULL,

   PRIMARY KEY (device_id, time_date)
);

SELECT create_hypertable(
   'power_consumption',
   'time_date',
    chunk_time_interval => INTERVAL '10 minutes'
);

ALTER TABLE power_consumption
SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'device_id',
    timescaledb.compress_orderby = 'time_date DESC'
);

SELECT add_compression_policy('power_consumption', INTERVAL '7 days');
SELECT add_retention_policy('power_consumption', INTERVAL '30 days');

-- ==========================================
-- TemperatureData
-- ==========================================
CREATE SEQUENCE temperature_data_seq INCREMENT BY 1;

CREATE TABLE temperature_data (
  id BIGINT DEFAULT nextval('temperature_data_seq'),

  device_id BIGINT NOT NULL,
  temperature REAL,
  humidity REAL,
  time_date TIMESTAMPTZ NOT NULL,

  PRIMARY KEY (device_id, time_date)
);

SELECT create_hypertable(
   'temperature_data',
   'time_date',
   chunk_time_interval => INTERVAL '10 minutes'
);

ALTER TABLE temperature_data
SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'device_id',
    timescaledb.compress_orderby = 'time_date DESC'
);

SELECT add_compression_policy('temperature_data', INTERVAL '7 days');
SELECT add_retention_policy('temperature_data', INTERVAL '30 days');

-- ==========================================
-- AirQuality
-- ==========================================
CREATE SEQUENCE air_quality_seq INCREMENT BY 1;

CREATE TABLE air_quality (
     id BIGINT DEFAULT nextval('air_quality_seq'),

     device_id BIGINT NOT NULL,
     co2 INTEGER,
     pm25 REAL,
     pm10 REAL,
     tvoc REAL,
     temperature REAL,
     humidity REAL,
     time_date TIMESTAMPTZ NOT NULL,

     PRIMARY KEY (device_id, time_date)
);

SELECT create_hypertable(
   'air_quality',
   'time_date',
   chunk_time_interval => INTERVAL '10 minutes'
);

ALTER TABLE air_quality
SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'device_id',
    timescaledb.compress_orderby = 'time_date DESC'
);

SELECT add_compression_policy('air_quality', INTERVAL '7 days');
SELECT add_retention_policy('air_quality', INTERVAL '30 days');

-- ==========================================
-- BatteryData
-- ==========================================
CREATE SEQUENCE battery_data_seq INCREMENT BY 1;

CREATE TABLE battery_data (
      id BIGINT DEFAULT nextval('battery_data_seq'),

      device_id BIGINT NOT NULL,
      val REAL,
      time_date TIMESTAMPTZ NOT NULL,

      PRIMARY KEY (device_id, time_date)
);

SELECT create_hypertable(
   'battery_data',
   'time_date',
   chunk_time_interval => INTERVAL '10 minutes'
);

ALTER TABLE battery_data
SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'device_id',
    timescaledb.compress_orderby = 'time_date DESC'
);

SELECT add_compression_policy('battery_data', INTERVAL '7 days');
SELECT add_retention_policy('battery_data', INTERVAL '30 days');

-- ==========================================
-- SmokeDetector
-- ==========================================
CREATE SEQUENCE smoke_detector_seq INCREMENT BY 1;

CREATE TABLE smoke_detector (
    id BIGINT DEFAULT nextval('smoke_detector_seq'),

    device_id BIGINT NOT NULL,
    smoke_raw INTEGER,
    co_level INTEGER,
    time_date TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (device_id, time_date)
);

SELECT create_hypertable(
   'smoke_detector',
   'time_date',
   chunk_time_interval => INTERVAL '6 hours'
);

ALTER TABLE smoke_detector
SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'device_id',
    timescaledb.compress_orderby = 'time_date DESC'
);

SELECT add_compression_policy('smoke_detector', INTERVAL '7 days');
SELECT add_retention_policy('smoke_detector', INTERVAL '30 days');

-- ==========================================
-- Devices
-- ==========================================
CREATE SEQUENCE devices_seq INCREMENT BY 1;

CREATE TABLE devices (
     id BIGINT DEFAULT nextval('devices_SEQ') PRIMARY KEY,
     hardware_id VARCHAR(127) NOT NULL UNIQUE,
     password_hash VARCHAR(127),
     status VARCHAR(31) CHECK (status IN ('ACTIVE','MAINTENANCE','BANNED','DECOMMISSIONED')),
     message_type VARCHAR(15) CHECK (message_type IN ('JSON','PROTO')),
     topic VARCHAR(127),
     battery_topic VARCHAR(127)
);

