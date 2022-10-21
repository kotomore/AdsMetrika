package ru.set404.AdsMetrika.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.set404.AdsMetrika.services.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", adminService.loadUsers());
        return "admin/list";
    }
}
