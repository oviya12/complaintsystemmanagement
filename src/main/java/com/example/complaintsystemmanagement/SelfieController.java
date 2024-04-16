package com.example.complaintsystemmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
public class SelfieController {

    @Autowired
    private SelfieService selfieService;

    @GetMapping("/selfie/{id}")
    public ResponseEntity<byte[]> getDecryptedSelfie(@PathVariable Long id) {
        try {
            byte[] decryptedSelfie = selfieService.getDecryptedSelfie(id);

            // Parse the decrypted data to extract the base64-encoded image data
            String decryptedString = new String(decryptedSelfie, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonObject = objectMapper.readTree(decryptedString);
            String base64ImageData = jsonObject.get("selfieData").asText().split(",")[1];
            byte[] binaryImageData = Base64.getDecoder().decode(base64ImageData);

            return ResponseEntity.ok().body(binaryImageData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/selfie")
    public ResponseEntity<String> saveSelfie(@RequestBody String selfieData) {
        try {
            selfieService.saveEncryptedSelfie(selfieData);
            return ResponseEntity.ok("Selfie saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save selfie: " + e.getMessage());
        }


    }


    @PostMapping("/recognize-face") // Define endpoint for face recognition
    public ResponseEntity<String> recognizeFace(@RequestBody String selfieData) {
        try {
            // Call the face recognition method from the service class
            boolean isFaceDetected = selfieService.isFaceDetected(selfieData);

            if (isFaceDetected) {
                // If face is detected, return success response
                return ResponseEntity.ok("Face recognized successfully!");
            } else {
                // If face is not detected, return failure response
                return ResponseEntity.badRequest().body("Face not recognized.");
            }
        } catch (Exception e) {
            // If any exception occurs during face recognition, return error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to recognize face: " + e.getMessage());
        }
    }
    // Function to check if a face is detected in the image data
}


