package ru.set404.AdsMetrika.dto;

import lombok.Getter;
import lombok.Setter;
import ru.set404.AdsMetrika.services.network.Network;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class OfferDTO {
    private int id;
    private int adcomboNumber;
    private String groupName;
    @Enumerated(EnumType.STRING)
    private Network networkName;

    public OfferDTO() {
    }
}
