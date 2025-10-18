package com.ftn.sbnz.model.events;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.time.Instant;
import java.util.Date;

// Current reading. Value can be amps or percent of rated current depending on pipeline.
// In our model we will usually use percent-of-rated (e.g., 110.0 means 110%).
@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("10m")
public class CurrentReading extends SensorReading {
    public CurrentReading() { super(); }
    public CurrentReading(String machineId, double percentOfRated, Date timestamp) {
        super(machineId, percentOfRated, timestamp);
    }
}
