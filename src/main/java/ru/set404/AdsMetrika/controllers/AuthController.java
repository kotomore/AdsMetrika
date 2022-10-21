package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
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
    public String loginPage() {
        if (isAuthenticated()) {
            return "redirect:/statistics";
        }
        return "auth/login";
    }

    @GetMapping("/registration")
    public String regPage(@ModelAttribute("person") User person) {
        if (isAuthenticated()) {
            return "redirect:/statistics";
        }
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String performReg(@ModelAttribute("person") @Valid User person, BindingResult bindingResult) {
        if (isAuthenticated()) {
            return "redirect:/statistics";
        }
        userValidator.validate(person, bindingResult);
        if (bindingResult.hasErrors())
            return "auth/registration";
        registrationService.register(person);
        return "redirect:/auth/login";
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
