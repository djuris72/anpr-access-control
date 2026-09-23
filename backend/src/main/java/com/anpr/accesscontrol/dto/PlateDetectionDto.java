package com.anpr.accesscontrol.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Mapira jednu detekciju iz JSON odgovora ML servisa (polje "plates").
 * ML servis (Python/FastAPI) vraca polja u snake_case (ocr_confidence),
 * dok mi u Javi koristimo camelCase - zato eksplicitni @JsonProperty.
 */
public record PlateDetectionDto(
        int[] box,
        double confidence,
        String text,

        @JsonProperty("ocr_confidence")
        double ocrConfidence
) {
}
