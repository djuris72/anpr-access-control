package com.anpr.accesscontrol.service;

import com.anpr.accesscontrol.dto.AccessCheckResponseDto;
import com.anpr.accesscontrol.dto.MlRecognizeResponseDto;
import com.anpr.accesscontrol.dto.PlateDetectionDto;
import com.anpr.accesscontrol.model.AccessLog;
import com.anpr.accesscontrol.model.Vehicle;
import com.anpr.accesscontrol.model.enums.AccessStatus;
import com.anpr.accesscontrol.model.enums.Direction;
import com.anpr.accesscontrol.repository.AccessLogRepository;
import com.anpr.accesscontrol.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Sprovodi ceo tok jednog pokusaja pristupa:
 *   1. posalji sliku ML servisu
 *   2. proveri da li je procitana tablica na whitelist-i i vazeca
 *   3. upisi rezultat u access_logs
 *   4. vrati odluku (odobreno/odbijeno)
 */
@Service
public class AccessControlService {

    private final MlServiceClient mlServiceClient;
    private final VehicleRepository vehicleRepository;
    private final AccessLogRepository accessLogRepository;
    private final double minConfidence;

    // Tablice se cuvaju normalizovane - samo velika slova i brojevi -
    // isto sto radi i clean_plate_text() u ML servisu, radi pouzdanog poredjenja.
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Z0-9]");

    public AccessControlService(MlServiceClient mlServiceClient,
                                 VehicleRepository vehicleRepository,
                                 AccessLogRepository accessLogRepository,
                                 @Value("${access.control.min-confidence:0.5}") double minConfidence) {
        this.mlServiceClient = mlServiceClient;
        this.vehicleRepository = vehicleRepository;
        this.accessLogRepository = accessLogRepository;
        this.minConfidence = minConfidence;
    }

    public AccessCheckResponseDto checkAccess(MultipartFile image, Direction direction, String gateId) {
        MlRecognizeResponseDto mlResponse = mlServiceClient.recognizePlate(image);

        if (!mlResponse.detected() || mlResponse.plates().isEmpty()) {
            return logAndRespond(null, direction, gateId, AccessStatus.DENIED,
                    null, null, "Tablica nije detektovana na slici.");
        }

        // Uzimamo detekciju sa najvisim detection confidence-om (ML servis
        // ih vec sortira, ali ne oslanjamo se na to ovde).
        PlateDetectionDto bestDetection = mlResponse.plates().stream()
                .max((a, b) -> Double.compare(a.confidence(), b.confidence()))
                .orElseThrow();

        String normalizedPlate = normalize(bestDetection.text());

        if (bestDetection.confidence() < minConfidence) {
            return logAndRespond(normalizedPlate, direction, gateId, AccessStatus.LOW_CONFIDENCE,
                    bestDetection.confidence(), bestDetection.ocrConfidence(),
                    "Detekcija ima nizak nivo pouzdanosti, potrebna je rucna provera.");
        }

        Optional<Vehicle> vehicleOpt = vehicleRepository.findByPlateNumber(normalizedPlate);

        if (vehicleOpt.isPresent() && vehicleOpt.get().isCurrentlyValid()) {
            Vehicle vehicle = vehicleOpt.get();
            AccessLog savedLog = logAccess(vehicle, normalizedPlate, direction, gateId,
                    AccessStatus.GRANTED, bestDetection.confidence(), bestDetection.ocrConfidence());
            return new AccessCheckResponseDto(
                    AccessStatus.GRANTED, normalizedPlate,
                    bestDetection.confidence(), bestDetection.ocrConfidence(),
                    "Pristup odobren za: " + vehicle.getOwnerName()
            );
        }

        return logAndRespond(normalizedPlate, direction, gateId, AccessStatus.DENIED,
                bestDetection.confidence(), bestDetection.ocrConfidence(),
                "Tablica nije pronadjena na listi ovlascenih vozila.");
    }

    private AccessCheckResponseDto logAndRespond(String plate, Direction direction, String gateId,
                                                  AccessStatus status, Double detConf, Double ocrConf,
                                                  String message) {
        logAccess(null, plate, direction, gateId, status, detConf, ocrConf);
        return new AccessCheckResponseDto(status, plate, detConf, ocrConf, message);
    }

    private AccessLog logAccess(Vehicle vehicle, String plate, Direction direction, String gateId,
                                 AccessStatus status, Double detConf, Double ocrConf) {
        AccessLog log = new AccessLog();
        log.setVehicle(vehicle);
        log.setRecognizedPlate(plate);
        log.setDirection(direction);
        log.setGateId(gateId);
        log.setStatus(status);
        log.setDetectionConfidence(detConf);
        log.setOcrConfidence(ocrConf);
        return accessLogRepository.save(log);
    }

    private String normalize(String rawText) {
        if (rawText == null) {
            return "";
        }
        return NON_ALPHANUMERIC.matcher(rawText.toUpperCase()).replaceAll("");
    }
}
