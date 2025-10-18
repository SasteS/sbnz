package com.ftn.sbnz.model.events;

import com.ftn.sbnz.model.enums.OperationContext;
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.util.Date;

@Role(Role.Type.EVENT)
@Timestamp("timestamp")
@Expires("10m")
public class ContextReading {
    private String machineId;
    private OperationContext context;
    // Was instant, now is Date because Drools CEP only supports java.util.Date or long for the @timestamp field
    private Date timestamp; //private Instant timestamp;

    public ContextReading() { this.timestamp = new Date(); }

    public ContextReading(String machineId, OperationContext context, Date timestamp) {
        this.machineId = machineId;
        this.context = context;
        this.timestamp = timestamp == null ? new Date() : timestamp;
    }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public OperationContext getContext() { return context; }
    public void setContext(OperationContext context) { this.context = context; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
