package com.ftn.sbnz.model.models;

import java.time.Instant;

public class TemperatureReading extends SensorReading {
    public TemperatureReading() { super(); }
    public TemperatureReading(String machineId, double value, Instant timestamp) {
        super(machineId, value, timestamp);
    }
}