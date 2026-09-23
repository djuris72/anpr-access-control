package com.anpr.accesscontrol.controller;

import com.anpr.accesscontrol.model.Vehicle;
import com.anpr.accesscontrol.repository.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * CRUD za whitelist vozila. NAPOMENA: ovi endpoint-i jos uvek nisu zasticeni
 * autentifikacijom - to dolazi u sledecem koraku plana (JWT + admin login).
 * Za sada svako ko zna URL moze da dodaje/menja/brise vozila - u redu je
 * za lokalno testiranje, ali ovo NE ide ovako u produkciju.
 */
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Z0-9]");

    public VehicleController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping
    public List<Vehicle> getAll() {
        return vehicleRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getById(@PathVariable Long id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@Valid @RequestBody Vehicle vehicle) {
        // Normalizujemo tablicu na isti nacin kao u AccessControlService,
        // da poredjenje kasnije bude pouzdano bez obzira kako je korisnik ukucao.
        vehicle.setPlateNumber(normalize(vehicle.getPlateNumber()));
        vehicle.setId(null); // sigurnosna mera - ne dozvoli klijentu da postavi ID
        Vehicle saved = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @Valid @RequestBody Vehicle updated) {
        return vehicleRepository.findById(id)
                .map(existing -> {
                    existing.setPlateNumber(normalize(updated.getPlateNumber()));
                    existing.setOwnerName(updated.getOwnerName());
                    existing.setAccessLevel(updated.getAccessLevel());
                    existing.setValidFrom(updated.getValidFrom());
                    existing.setValidUntil(updated.getValidUntil());
                    existing.setActive(updated.isActive());
                    return ResponseEntity.ok(vehicleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!vehicleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String normalize(String plate) {
        if (plate == null) return null;
        return NON_ALPHANUMERIC.matcher(plate.toUpperCase()).replaceAll("");
    }
}
