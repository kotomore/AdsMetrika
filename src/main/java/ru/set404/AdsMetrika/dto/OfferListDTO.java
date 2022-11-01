package ru.set404.AdsMetrika.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OfferListDTO {

    private List<OfferDTO> offers = new ArrayList<>();

    public OfferListDTO(List<OfferDTO> offers) {
        this.offers = offers;
    }

    public OfferListDTO() {
    }

    public void addOffer(OfferDTO offerDTO) {
        this.offers.add(offerDTO);
    }
}



