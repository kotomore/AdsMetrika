package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.services.RegistrationService;
import ru.set404.AdsMetrika.util.UserValidator;

import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final UserValidator userValidator;

    @Autowired
    public AuthController(RegistrationService registrationService, UserValidator userValidator) {
        this.registrationService = registrationService;
        this.userValidator = userValidator;
    }

    @GetMapping("/login")
    public String loginPage(@ModelAttribute("person") User user) {
        if (isAuthenticated()) {
            return "redirect:/statistics";
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String performReg(@ModelAttribute("person") @Valid User user, BindingResult bindingResult, Model model) {
        if (isAuthenticated()) {
            return "redirect:/statistics";
        }
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("hasError", true);
            return "auth/login";
        }
        registrationService.register(user);
        model.addAttribute("success", true);
        return "auth/login";
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }
}
