package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.set404.AdsMetrika.models.User;

@Repository
public interface AdminsRepository extends JpaRepository<User, Integer> {
}
