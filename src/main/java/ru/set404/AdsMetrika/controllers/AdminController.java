package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.set404.AdsMetrika.services.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final ApplicationContext context;
    @Autowired
    public AdminController(AdminService adminService, ApplicationContext context) {
        this.adminService = adminService;
        this.context = context;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", adminService.loadUsers());
        return "admin/list";
    }

    @PostMapping("/shutdown")
    public void shutdown() {
        int exitCode = SpringApplication.exit(context, (ExitCodeGenerator) () -> 0);
        System.exit(exitCode);
    }
}
