package com.example.complaintsystemmanagement;


import com.example.complaintsystemmanagement.Selfie;
import com.example.complaintsystemmanagement.SelfieRepository;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.springframework.util.ResourceUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@Service
public class SelfieService {

    @Autowired
    private SelfieRepository selfieRepository;

    private static SecretKey aesSecretKey;
    static {
        try {
            // Generate AES secret key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // Key size 256 bits
            aesSecretKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }// Assuming you have this key initialized somewhere




    public void saveEncryptedSelfie(String selfieData) throws Exception {

        byte[] encryptedData = encryptWithTwofish(selfieData);
        selfieRepository.save(new Selfie(encryptedData));
    }

    public byte[] getDecryptedSelfie(Long selfieId) throws Exception {
        Selfie selfie = selfieRepository.findById(selfieId)
                .orElseThrow(() -> new RuntimeException("Selfie not found"));

        return decryptWithTwofish(selfie.getSelfieData());
    }

    public byte[] encryptWithTwofish(String description) throws Exception {
        TwofishEngine twofishEngine = new TwofishEngine();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(twofishEngine, new PKCS7Padding());
        KeyParameter key = new KeyParameter(aesSecretKey.getEncoded());
        cipher.init(true, key);
        byte[] input = description.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[cipher.getOutputSize(input.length)];
        int processed = cipher.processBytes(input, 0, input.length, output, 0);
        processed += cipher.doFinal(output, processed);
        return Base64.getEncoder().encodeToString(output).getBytes();
    }

    public byte[] decryptWithTwofish(byte[] encryptedDescription) throws Exception {
        TwofishEngine twofishEngine = new TwofishEngine();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(twofishEngine, new PKCS7Padding());
        KeyParameter key = new KeyParameter(aesSecretKey.getEncoded());
        cipher.init(false, key);
        byte[] input = Base64.getDecoder().decode(encryptedDescription);
        byte[] output = new byte[cipher.getOutputSize(input.length)];
        int processed = cipher.processBytes(input, 0, input.length, output, 0);
        processed += cipher.doFinal(output, processed);
        byte[] decryptedData = Arrays.copyOf(output, processed);
        String decryptedString = new String(decryptedData, StandardCharsets.UTF_8);

// Print the decrypted string to the console




        return decryptedData;
    }

    public boolean isFaceDetected(String selfieData) {

        try {


            // Load OpenCV native library
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            // Convert base64 image data to Mat object
            byte[] imageBytes = java.util.Base64.getDecoder().decode(selfieData.split(",")[1]);

            // Convert base64 image data to Mat object

            Mat image = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_UNCHANGED);

            // Load pre-trained cascade classifier for face detection
            File cascadeFile = ResourceUtils.getFile("classpath:haarcascade_frontalface_default.xml");
            CascadeClassifier faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

            // Detect faces in the image
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(image, faceDetections);

            // Check if any faces are detected
            return faceDetections.toArray().length > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false in case of any errors
        }}


}



