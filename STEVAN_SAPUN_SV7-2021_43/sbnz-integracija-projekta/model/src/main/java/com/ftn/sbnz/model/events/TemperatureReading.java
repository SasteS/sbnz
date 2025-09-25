package com.ftn.sbnz.model.events;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.time.Instant;
import java.util.Date;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("5m")
public class TemperatureReading extends SensorReading {
    public TemperatureReading() { super(); }
    public TemperatureReading(String machineId, double value, Date timestamp) {
        super(machineId, value, timestamp);
    }
}