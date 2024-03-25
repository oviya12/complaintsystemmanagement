package com.example.complaintsystemmanagement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String showUserRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "user-registration";
    }

    @PostMapping("/user-registration")
    public String userRegistration(@ModelAttribute("user") @Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            // If validation fails, return back to registration form with error messages
            return "user-registration";
        }

        // Check if username already exists
        if (userService.findByUsername(user.getUsername()) != null) {
            result.rejectValue("username", "error.user", "Username is already taken");
            return "user-registration";
        }

        // Save the new user
        userService.save(user);

        // Redirect to login page after successful registration
        return "redirect:/login";
    }
}
