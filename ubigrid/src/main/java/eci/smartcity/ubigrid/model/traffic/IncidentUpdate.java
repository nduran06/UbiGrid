package eci.smartcity.ubigrid.model.traffic;

import java.time.LocalDateTime;

import eci.smartcity.ubigrid.model.traffic.enums.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Updates on an incident's status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentUpdate {
    
    private LocalDateTime timestamp;
    private String updateText;
    private IncidentStatus previousStatus;
    private IncidentStatus newStatus;
    private String updatedBy;
}
