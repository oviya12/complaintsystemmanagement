package com.example.complaintsystemmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

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
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id).orElse(null);
    }

    public void updateComplaintStatus(Long id, String status) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid complaint id: " + id));
        complaint.setStatus(status);
        complaintRepository.save(complaint);
    }

    public Complaint createComplaint(Complaint complaint) throws Exception {
        complaint.setStatus("Pending");
        // Generate a random 6-digit ID
        Random random = new Random();
        long id = 100000 + random.nextInt(900000); // Generates a random number between 100000 and 999999

        while(complaintRepository.existsById(id)) {
            id = 100000 + random.nextInt(900000); // Regenerate ID if it already exists
        }
        complaint.setId(id);


        String twofishEncryptedDescription = encryptWithTwofish(complaint.getDescription());
        complaint.setDescription(twofishEncryptedDescription);
        return complaintRepository.save(complaint);
    }

    public List<Complaint> getAllDecryptedComplaints() {
        List<Complaint> complaints = complaintRepository.findAll();
        return complaints.stream()
                .map(this::decryptComplaintDescription)
                .collect(Collectors.toList());
    }


    private String encryptWithTwofish(String description) throws Exception {
        TwofishEngine twofishEngine = new TwofishEngine();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(twofishEngine, new PKCS7Padding());
        KeyParameter key = new KeyParameter(aesSecretKey.getEncoded());
        cipher.init(true, key);
        byte[] input = description.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[cipher.getOutputSize(input.length)];
        int processed = cipher.processBytes(input, 0, input.length, output, 0);
        processed += cipher.doFinal(output, processed);
        return Base64.getEncoder().encodeToString(output);
    }

    public Complaint decryptComplaintDescription(Complaint complaint) {
        try {
            String twofishDecryptedDescription = decryptWithTwofish(complaint.getDescription());

            complaint.setDescription(twofishDecryptedDescription);
        } catch (Exception e) {
            e.printStackTrace(); // Handle decryption error
        }
        return complaint;
    }

    private String decryptWithTwofish(String encryptedDescription) throws Exception {
        TwofishEngine twofishEngine = new TwofishEngine();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(twofishEngine, new PKCS7Padding());
        KeyParameter key = new KeyParameter(aesSecretKey.getEncoded());
        cipher.init(false, key);
        byte[] input = Base64.getDecoder().decode(encryptedDescription);
        byte[] output = new byte[cipher.getOutputSize(input.length)];
        int processed = cipher.processBytes(input, 0, input.length, output, 0);
        processed += cipher.doFinal(output, processed);
        return new String(output, 0, processed).trim(); // Trim any additional whitespace
    }
    public class RandomIdGenerator {
        public static int generateRandomId() {
            Random rand = new Random();
            // Generate a random number between 100000 and 999999
            return rand.nextInt(900000) + 100000;
        }
    }



    public int getNumberOfResolvedComplaints() {
        return complaintRepository.countByStatus("Resolved");
    }

    public int getNumberOfPendingComplaints() {
        return complaintRepository.countByStatus("Pending");
    }
}
