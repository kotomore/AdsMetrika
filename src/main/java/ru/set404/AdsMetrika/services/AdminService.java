package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.AdminsRepository;

import java.util.List;

@Service
public class AdminService {
    private final AdminsRepository adminsRepository;

    @Autowired
    public AdminService(AdminsRepository adminsRepository) {
        this.adminsRepository = adminsRepository;
    }

    public List<User> loadUsers() {
        return adminsRepository.findAll();
    }

    public void makeRole(int userId) {
        User user = adminsRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!user.getRole().equals("ROLE_ADMIN")) {
            user.setRole(user.getRole().equals("ROLE_GUEST") ? "ROLE_USER" : "ROLE_GUEST");
            adminsRepository.save(user);
        }
    }
}
