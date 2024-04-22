package com.example.complaintsystemmanagement;

import com.example.complaintsystemmanagement.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Method to count complaints by status
    int countByStatus(String status);
    Long findIdByTitle(String title);

}
