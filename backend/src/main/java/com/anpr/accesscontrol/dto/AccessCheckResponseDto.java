package com.anpr.accesscontrol.dto;

import com.anpr.accesscontrol.model.enums.AccessStatus;

/**
 * Odgovor koji nas backend vraca nakon provere pristupa - ovo vidi
 * frontend (ekran "kapija") da prikaze zeleno/crveno.
 */
public record AccessCheckResponseDto(
        AccessStatus status,
        String recognizedPlate,
        Double detectionConfidence,
        Double ocrConfidence,
        String message
) {
}
