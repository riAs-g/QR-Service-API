package com.example.QR.Service;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

@SpringBootApplication
public class QrServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(QrServiceApplication.class, args);
	}

	@RestController
	public static class controller {
		@GetMapping("/health")
		public ResponseEntity<String> returnHealth() {
			return new ResponseEntity<>("200 OK", HttpStatus.OK);
		}

		@GetMapping("/qrcode")
		public ResponseEntity<?> getImage(@RequestParam String contents,
										  @RequestParam(defaultValue = "300") int size,
										  @RequestParam(defaultValue = "L") String correction,
										  @RequestParam(defaultValue = "png") String type) {
			if (contents == null || contents.isBlank()) {
				return ResponseEntity
						.badRequest()
						.body("{\"error\": \"Contents cannot be null or blank\"}");
			}
			if (size < 150
					|| size > 350) {
				return ResponseEntity
						.badRequest()
						.body("{\"error\": \"Image size must be between 150 and 350 pixels\"}");
			}
			if (!"L".equals(correction)
					&& !"M".equals(correction)
					&& !"Q".equals(correction)
					&& !"H".equals(correction)) {
				return ResponseEntity
						.badRequest()
						.body("{\"error\": \"Permitted error correction levels are L, M, Q, H\"}");
			}
			if ("png".equals(type)
					|| "jpeg".equals(type)
					|| "gif".equals(type)) {
				ErrorCorrectionLevel level = ErrorCorrectionLevel.valueOf(correction);
				BufferedImage bufferedImage = generateImage(contents, size, level);
				return ResponseEntity
						.ok()
						.contentType(MediaType.parseMediaType("image/" + type))
						.body(bufferedImage);
			}
			return ResponseEntity
					.badRequest()
					.body("{\"error\": \"Only png, jpeg and gif image types are supported\"}");
		}

		private static BufferedImage generateImage(String contents,
												   int size,
												   ErrorCorrectionLevel level) {
			QRCodeWriter writer = new QRCodeWriter();
			String path="/Users/"+System.getProperty("user.name")+"/Desktop/"+contents.hashCode()+".jpg";
			Map<EncodeHintType, ?> hints = Map.of(EncodeHintType.ERROR_CORRECTION, level);
			try {
				BitMatrix bitMatrix = writer.encode(contents, BarcodeFormat.QR_CODE, size, size, hints);
				MatrixToImageWriter.writeToPath(bitMatrix,"jpg", Paths.get(path));
				return MatrixToImageWriter.toBufferedImage(bitMatrix);
			} catch (WriterException | IOException e) {
				e.printStackTrace();
			}
            return null;
		}
	}

	@Configuration
	public static class QRCodeConverter {
		@Bean
		public HttpMessageConverter<BufferedImage> bufferedImageHttpMessageConverter() {
			return new BufferedImageHttpMessageConverter();
		}
	}
}
