package ru.set404.AdsMetrika.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.services.CredentialsService;
import ru.set404.AdsMetrika.services.UsersService;

@Component
public class CredentialsValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Credentials.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Credentials credentials = (Credentials) target;
        if (credentials.getNetworkName() == Network.TF && credentials.getPassword().isEmpty())
            errors.rejectValue("password", "", "Need password for traffic factory API");
    }
}
