package ru.set404.AdsMetrika.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.OffersRepository;
import ru.set404.AdsMetrika.security.UserDetails;

@Controller
@RequestMapping("/statistics")
public class UserController {
    private final OffersRepository offersRepository;

    public UserController(OffersRepository offersRepository) {
        this.offersRepository = offersRepository;
    }

    @GetMapping
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("offers", offersRepository.findByOwner(((UserDetails) authentication.getPrincipal()).getUser()));
        return "index";
    }
}
