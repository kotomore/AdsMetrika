package ru.set404.AdsMetrika.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.set404.AdsMetrika.models.Settings;
import ru.set404.AdsMetrika.repositories.SettingsRepository;
import ru.set404.AdsMetrika.services.SettingsService;

import java.util.List;

@Component
public class SettingsValidator implements Validator {


    @Override
    public boolean supports(Class<?> clazz) {
        return Settings.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Settings settings = (Settings) target;
        if (!settings.getAdcomboId().trim().isEmpty()) {
            try {
                List<String> adcomboIdS = List.of(settings.getAdcomboId().split(","));
                List<Integer> adcomboIdSInteger = adcomboIdS.stream().map(Integer::valueOf).toList();
            } catch (Exception e) {
                errors.rejectValue("adcomboId", "", "adcombo id not valid");
            }
        }

    }
}
