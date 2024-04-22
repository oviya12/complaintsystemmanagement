package com.example.complaintsystemmanagement;

import com.example.complaintsystemmanagement.User;
import com.example.complaintsystemmanagement.UserService;
import com.example.complaintsystemmanagement.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ComplaintService complaintService;

    @Autowired
    public UserController(UserService userService, ComplaintService complaintService) {
        this.userService = userService;
        this.complaintService = complaintService;
    }






    // Show profile page
    @GetMapping("/profile")
    public ModelAndView showProfilePage(Model model) {
        // Load user details and add them to the model
        User user = userService.getCurrentUser(); // Example method to get current user
        model.addAttribute("user", user);
        return new ModelAndView("profile");
    }

    // Handle POST request to update user details
    @PostMapping("/profile")
    public ResponseEntity<Void> updateUserDetails(@RequestParam("id") Long id,
                                                  @RequestParam("username") String username,
                                                  @RequestParam("address") String address,
                                                  @RequestParam("phoneNumber") String phoneNumber,
                                                  @RequestParam("email") String email) {
        // Update the user details in the database
        userService.updateUserDetails(id, username, address, phoneNumber, email);

        // Return response indicating success
        return ResponseEntity.ok().build();
    }

    // Handle GET request to check status
    @GetMapping("/check-status")
    public ResponseEntity<String> checkStatus(@RequestParam("complaintId") Long complaintId) {
        // Check the status of the complaint using the complaintId
        String status = complaintService.checkStatus(complaintId);

        // Return the status as a response
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserDetails(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserDTO userDTO = convertToDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setAddress(user.getAddress());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }
}
