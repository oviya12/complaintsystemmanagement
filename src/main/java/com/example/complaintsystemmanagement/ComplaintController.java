
package com.example.complaintsystemmanagement;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired ComplaintRepository complaintRepository;



    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        if (user.getUsername().equals("admin") && user.getPassword().equals("admin")) {
            return "redirect:/admin";
        } else {
            if (userService.authenticate(user.getUsername(), user.getPassword())) {
                return "redirect:/homepage";
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                return "redirect:/login";
            }
        }
    }

    @GetMapping("/homepage")
    public String showHomePage(Model model) {

        int numberOfResolvedComplaints = complaintService.getNumberOfResolvedComplaints();
        int numberOfPendingComplaints = complaintService.getNumberOfPendingComplaints();
        long numberOfUsers = userService.getNumberOfUsers();
        model.addAttribute("numberOfResolvedComplaints", numberOfResolvedComplaints);
        model.addAttribute("numberOfPendingComplaints", numberOfPendingComplaints);
        model.addAttribute("numberOfUsers", numberOfUsers);
        return "homepage";

    }



    @GetMapping("/submit-complaint")
    public String showSubmitComplaintForm(Model model) {
        model.addAttribute("complaint", new Complaint());
        return "submit-complaint";
    }
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us");
        return "contact"; // Assuming you have a contact.html template
    }
    @GetMapping("/helpline")
    public String helpline(Model model) {
        model.addAttribute("pageTitle", "Helpline");
        return "helpline"; // Assuming you have a contact.html template
    }
    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("pageTitle", "Profile");
        return "profile"; // Assuming you have a contact.html template
    }
    @GetMapping("/capture-selfie")
    public String capture(Model model) {
        model.addAttribute("pageTitle", "Capture Selfie");
        return "capture-selfie"; // Assuming you have a contact.html template
    }
    @GetMapping("/verify")
    public String verify(Model model) {
        model.addAttribute("pageTitle", "Verify User");
        return "verify"; // Assuming you have a contact.html template
    }
    @GetMapping("/confirmation")
    public String confirmation(@RequestParam(name = "complaintId") Long complaintId,Model model) {
        model.addAttribute("complaintId", complaintId);
        model.addAttribute("pageTitle", "Confirmation");
        return "confirmation";
    }



    @PostMapping("/submit-complaint")
    public String submitComplaint(@ModelAttribute("complaint") Complaint complaint, Model model) {
        try {
            Map<String, Object> result = complaintService.createComplaint(complaint);
            Complaint savedComplaint = (Complaint) result.get("complaint");
            Long complaintId = (Long) result.get("complaintId");

            // Add the complaint ID to the model
            model.addAttribute("complaintId", complaintId);

            return "/confirmation";
        } catch (Exception e) {
            return "error";
        }
    }
    @GetMapping("/admin")
    public String showAdminPage(Model model) {
        List<Complaint> complaints = complaintService.getAllComplaints();
        model.addAttribute("complaints", complaints);
        return "admin";
    }


    @PostMapping("/admin/update-status")
    public String updateComplaintStatus(@RequestParam("id") Long id, @RequestParam("status") String status) {
        complaintService.updateComplaintStatus(id, status);
        return "redirect:/admin";
    }


    @GetMapping("/admin/view-complaint/{id}")
    public String viewComplaint(@PathVariable Long id, Model model) {
        try {
            Complaint complaint = complaintService.getComplaintById(id);
            Complaint decryptedComplaint = complaintService.decryptComplaintDescription(complaint);
            model.addAttribute("complaint", decryptedComplaint);
        } catch (Exception e) {
            // Handle exception
            return "error";
        }
        return "view-complaint";
    }

    @PostMapping("/admin/view-complaint/{id}")
    public String viewComplaint(@PathVariable Long id, @RequestParam("decryptionKey") String decryptionKey, Model model) {
        try {
            Complaint complaint = complaintService.getComplaintById(id);
            complaint = complaintService.decryptComplaintDescription(complaint);
            model.addAttribute("complaint", complaint); // Add decrypted complaint to model


        } catch (Exception e) {
            // Handle decryption error
            return "error";
        }
        return "view-complaint";
    }
}
