package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
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
}
