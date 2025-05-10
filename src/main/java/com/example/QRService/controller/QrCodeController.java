package com.example.QRService.controller;

import com.example.QRService.service.QrCodeService;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

@RestController
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> returnHealth() {
        return ResponseEntity.ok("200 OK");
    }

    @GetMapping("/qrcode")
    public ResponseEntity<?> getImage(@RequestParam String contents,
            @RequestParam(defaultValue = "300") int size,
            @RequestParam(defaultValue = "L") String correction,
            @RequestParam(defaultValue = "png") String type) {
        if (contents == null || contents.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Contents cannot be null or blank"));
        }

        if (size < 150 || size > 350) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image size must be between 150 and 350 pixels"));
        }

        Set<String> allowedCorrections = Set.of("L", "M", "Q", "H");
        if (!allowedCorrections.contains(correction)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Permitted error correction levels are L, M, Q, H"));
        }

        Set<String> supportedTypes = Set.of("png", "jpeg", "gif");
        if (!supportedTypes.contains(type)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only png, jpeg and gif image types are supported"));
        }

        ErrorCorrectionLevel level = ErrorCorrectionLevel.valueOf(correction);
        try {
            BufferedImage image = qrCodeService.generateImage(contents, size, level);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("image/" + type))
                    .body(image);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "QR code generation failed"));
        }
    }
}
