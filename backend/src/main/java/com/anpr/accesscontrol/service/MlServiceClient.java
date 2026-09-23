package com.anpr.accesscontrol.service;

import com.anpr.accesscontrol.dto.MlRecognizeResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Klijent koji salje sliku ML servisu (FastAPI, YOLOv8 + EasyOCR) preko
 * multipart/form-data POST zahteva na /recognize, tacno onako kako smo
 * ranije testirali sa curl-om.
 */
@Service
public class MlServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MlServiceClient.class);

    private final RestTemplate restTemplate;
    private final String mlServiceUrl;

    public MlServiceClient(RestTemplate restTemplate,
                            @Value("${ml.service.url}") String mlServiceUrl) {
        this.restTemplate = restTemplate;
        this.mlServiceUrl = mlServiceUrl;
    }

    /**
     * Salje sliku ML servisu i vraca parsirani odgovor.
     * Vraca prazan (detected=false) odgovor ako ML servis ne odgovori ili
     * vrati gresku - ne bacamo izuzetak dalje, jer "servis je nedostupan"
     * treba tretirati kao "nismo mogli da prepoznamo tablicu", ne kao pad
     * cele aplikacije.
     */
    public MlRecognizeResponseDto recognizePlate(MultipartFile image) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", toResource(image));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            return restTemplate.postForObject(
                    mlServiceUrl + "/recognize",
                    requestEntity,
                    MlRecognizeResponseDto.class
            );
        } catch (RestClientException e) {
            log.error("Poziv ka ML servisu nije uspeo: {}", e.getMessage());
            return new MlRecognizeResponseDto(false, java.util.List.of(), null, null);
        }
    }

    /**
     * RestTemplate ocekuje Resource za multipart fajlove (obican byte[] ili
     * InputStream se ne salje ispravno kao "file" deo forme), pa pravimo
     * ByteArrayResource koji vraca originalni naziv fajla.
     */
    private ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Ne mogu da procitam upload-ovanu sliku", e);
        }
    }
}
