package com.anpr.accesscontrol.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Mapira ceo JSON odgovor sa endpoint-a POST /recognize ML servisa.
 * Primer odgovora (iz main.py):
 * {
 *   "detected": true,
 *   "plates": [ { "box": [...], "confidence": 0.91, "text": "NS123AB", "ocr_confidence": 0.87 } ],
 *   "best_plate": "NS123AB",
 *   "best_confidence": 0.91
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MlRecognizeResponseDto(
        boolean detected,
        List<PlateDetectionDto> plates,

        @JsonProperty("best_plate")
        String bestPlate,

        @JsonProperty("best_confidence")
        Double bestConfidence
) {
}
