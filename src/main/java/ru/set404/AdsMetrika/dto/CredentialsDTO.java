package ru.set404.AdsMetrika.dto;

import lombok.Getter;
import lombok.Setter;
import ru.set404.AdsMetrika.network.Network;

import javax.persistence.*;

@Getter
@Setter
public class CredentialsDTO {

    private int id;
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Network networkName;
}
