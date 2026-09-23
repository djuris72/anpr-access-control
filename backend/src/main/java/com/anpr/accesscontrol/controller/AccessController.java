package com.anpr.accesscontrol.controller;

import com.anpr.accesscontrol.dto.AccessCheckResponseDto;
import com.anpr.accesscontrol.model.enums.Direction;
import com.anpr.accesscontrol.service.AccessControlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    private final AccessControlService accessControlService;

    public AccessController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    /**
     * Glavni endpoint "kapije": prima sliku, prosledjuje je ML servisu,
     * proverava whitelist i loguje pokusaj. Vraca GRANTED/DENIED/LOW_CONFIDENCE.
     *
     * Primer poziva (curl), analogno onome kako smo testirali ML servis:
     * curl -X POST "http://localhost:8080/api/access/check?direction=ENTRY" \
     *      -F "file=@slika.jpg"
     */
    @PostMapping(value = "/check", consumes = "multipart/form-data")
    public ResponseEntity<AccessCheckResponseDto> checkAccess(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "direction", defaultValue = "ENTRY") Direction direction,
            @RequestParam(value = "gateId", required = false) String gateId
    ) {
        AccessCheckResponseDto response = accessControlService.checkAccess(file, direction, gateId);
        return ResponseEntity.ok(response);
    }
}
