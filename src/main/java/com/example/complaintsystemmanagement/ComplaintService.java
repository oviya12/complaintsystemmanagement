package com.example.complaintsystemmanagement;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    private static final String DEFAULT_ENCRYPTION_KEY = "YourDefaultEncryptionKey";

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
        Security.addProvider(new BouncyCastleProvider());
        String encryptedDescription = encrypt(complaint.getDescription());
        complaint.setDescription(encryptedDescription);
        return complaintRepository.save(complaint);
    }

    public List<Complaint> getAllDecryptedComplaints() {
        List<Complaint> complaints = complaintRepository.findAll();
        return complaints.stream()
                .map(this::decryptComplaintDescription)
                .collect(Collectors.toList());
    }

    private static String encrypt(String description) throws Exception {
        TwofishEngine twofishEngine = new TwofishEngine();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(twofishEngine, new PKCS7Padding());
        KeyParameter key = new KeyParameter(DEFAULT_ENCRYPTION_KEY.getBytes());
        cipher.init(true, key);
        byte[] input = description.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[cipher.getOutputSize(input.length)];
        int processed = cipher.processBytes(input, 0, input.length, output, 0);
        processed += cipher.doFinal(output, processed);
        return Base64.getEncoder().encodeToString(output);
    }

    public Complaint decryptComplaintDescription(Complaint complaint) {
        try {
            String encryptionKey = DEFAULT_ENCRYPTION_KEY;
            String decryptedDescription = decryptComplaintDescription(complaint.getDescription(), encryptionKey);
            complaint.setDescription(decryptedDescription);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace(); // Handle decryption error
        } catch (Exception e) {
            e.printStackTrace();
        }
        return complaint;
    }

    public static String decryptComplaintDescription(String encryptedDescription, String encryptionKey) throws Exception {
        TwofishEngine twofishEngine = new TwofishEngine();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(twofishEngine);
        KeyParameter key = new KeyParameter(encryptionKey.getBytes());
        cipher.init(false, key);
        byte[] input = Base64.getDecoder().decode(encryptedDescription);
        byte[] output = new byte[cipher.getOutputSize(input.length)];
        int processed = cipher.processBytes(input, 0, input.length, output, 0);
        processed += cipher.doFinal(output, processed);
        return new String(output, 0, processed).trim(); // Trim any additional whitespace
    }

    public int getNumberOfResolvedComplaints() {
        return complaintRepository.countByStatus("Resolved");
    }

    public int getNumberOfPendingComplaints() {
        return complaintRepository.countByStatus("Pending");
    }
}
