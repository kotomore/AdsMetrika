package ru.set404.AdsMetrika.dto;

import lombok.Getter;
import lombok.Setter;
import ru.set404.AdsMetrika.services.network.Network;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class OfferDTO {
    @NotEmpty(message = "Номер не может быть пустым")
    private int adcomboNumber;

    @NotEmpty(message = "Имя не может быть пустым")
    private String groupName;

    @Enumerated(EnumType.STRING)
    private Network networkName;
}
