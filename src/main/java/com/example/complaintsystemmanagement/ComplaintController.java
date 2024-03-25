package com.example.complaintsystemmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

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

        model.addAttribute("numberOfResolvedComplaints", numberOfResolvedComplaints);
        model.addAttribute("numberOfPendingComplaints", numberOfPendingComplaints);

        return "homepage";
    }

    @GetMapping("/submit-complaint")
    public String showSubmitComplaintForm(Model model) {
        model.addAttribute("complaint", new Complaint());
        return "submit-complaint";
    }

    @PostMapping("/submit-complaint")
    public String submitComplaint(@ModelAttribute("complaint") Complaint complaint) {
        try {
            complaintService.createComplaint(complaint);
            return "redirect:/homepage";
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

    @PostMapping("/update-status")
    public String updateComplaintStatus(@RequestParam("id") Long id, @RequestParam("status") String status) {
        complaintService.updateComplaintStatus(id, status);
        return "redirect:/admin";
    }

    @GetMapping("/admin/view-complaint/{id}")
    public String viewComplaint(@PathVariable Long id, Model model) {
        model.addAttribute("complaintId", id);
        return "view-complaint";
    }

    @PostMapping("/admin/view-complaint/{id}")
    public String viewComplaint(@PathVariable Long id, @RequestParam("decryptionKey") String decryptionKey, Model model) {
        try {
            Complaint complaint = complaintService.getComplaintById(id);
            String decryptedDescription = complaintService.decryptComplaintDescription(complaint.getDescription(), decryptionKey);
            model.addAttribute("decryptedDescription", decryptedDescription);
        } catch (Exception e) {
            // Handle decryption error
            return "error";
        }
        return "view-complaint";
    }
}
