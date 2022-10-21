package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.models.Person;
import ru.set404.AdsMetrika.repositories.PeopleRepository;
import ru.set404.AdsMetrika.security.PersonDetails;

import java.util.Optional;

@Service
public class PersonDetailsService implements UserDetailsService {
    private final PeopleRepository peopleRepository;

    @Autowired
    public PersonDetailsService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
         Optional<Person> person = peopleRepository.findByUsername(username);
         if (person.isEmpty())
             throw new UsernameNotFoundException("Пользователь не найден");
         return new PersonDetails(person.get());
    }
}
