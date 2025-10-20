package com.ftn.sbnz.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HypothesisRequsetDTO {
    private String machineId;
    private String hypothesis;
}
