package ru.set404.AdsMetrika.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.services.UsersService;

@Component
public class UserValidator implements Validator {

    private final UsersService usersService;

    @Autowired
    public UserValidator(UsersService usersService) {
        this.usersService = usersService;
    }


    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User person = (User) target;
        if (usersService.loadUserByUsername(person.getUsername()).isPresent())
            errors.rejectValue("username", "", "Name is exists");
    }
}
