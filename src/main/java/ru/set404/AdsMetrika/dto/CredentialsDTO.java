package ru.set404.AdsMetrika.dto;

import lombok.Getter;
import lombok.Setter;
import ru.set404.AdsMetrika.services.network.Network;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class CredentialsDTO {
    @NotEmpty(message = "Имя пользователя не может быть пустым")
    private String username;

    @NotEmpty(message = "Пароль не может быть пустым")
    private String password;

    @Enumerated(EnumType.STRING)
    private Network networkName;
}
