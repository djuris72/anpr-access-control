package com.anpr.accesscontrol.model;

import com.anpr.accesscontrol.model.enums.AccessStatus;
import com.anpr.accesscontrol.model.enums.Direction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Zapis jednog pokusaja prolaska kroz kapiju - bilo da je odobren ili odbijen.
 * Cuva se i sirovi tekst koji je OCR procitao (moze se razlikovati od
 * stvarne tablice ako je citanje pogresno), radi kasnije analize gresaka.
 */
@Entity
@Table(name = "access_logs", indexes = {
        @Index(name = "idx_log_timestamp", columnList = "timestamp"),
        @Index(name = "idx_log_plate", columnList = "recognizedPlate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tekst koji je ML servis procitao - moze biti null/prazan ako OCR nije uspeo
    @Column(length = 20)
    private String recognizedPlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessStatus status;

    // Confidence skorovi vraceni iz ML servisa - korisno za kasniju analizu
    // koliko cesto sistem "nije siguran" i da li treba podesiti prag.
    private Double detectionConfidence;
    private Double ocrConfidence;

    // Veza ka vozilu ako je tablica prepoznata i pronadjena na whitelist-i.
    // Ostaje null ako tablica nije prepoznata ili nije na listi.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // Za slucaj vise kapija u buducnosti - za sada opciono
    private String gateId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRecognizedPlate() {
		return recognizedPlate;
	}

	public void setRecognizedPlate(String recognizedPlate) {
		this.recognizedPlate = recognizedPlate;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public AccessStatus getStatus() {
		return status;
	}

	public void setStatus(AccessStatus status) {
		this.status = status;
	}

	public Double getDetectionConfidence() {
		return detectionConfidence;
	}

	public void setDetectionConfidence(Double detectionConfidence) {
		this.detectionConfidence = detectionConfidence;
	}

	public Double getOcrConfidence() {
		return ocrConfidence;
	}

	public void setOcrConfidence(Double ocrConfidence) {
		this.ocrConfidence = ocrConfidence;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getGateId() {
		return gateId;
	}

	public void setGateId(String gateId) {
		this.gateId = gateId;
	}
    
}
