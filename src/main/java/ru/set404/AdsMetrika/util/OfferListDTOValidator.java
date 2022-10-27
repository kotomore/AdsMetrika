package ru.set404.AdsMetrika.util;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.set404.AdsMetrika.dto.OfferDTO;
import ru.set404.AdsMetrika.dto.OfferListDTO;
import ru.set404.AdsMetrika.repositories.OffersRepository;

@Component
public class OfferListDTOValidator implements Validator {

    private final OffersRepository offersRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public OfferListDTOValidator(OffersRepository offersRepository, ModelMapper modelMapper) {
        this.offersRepository = offersRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return OfferListDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OfferListDTO offerListDTO = (OfferListDTO) target;
        for (OfferDTO offerDTO : offerListDTO.getOffers()) {
            if (offerDTO.getId() != 0 && (offerDTO.getGroupName().isEmpty() || offerDTO.getAdcomboNumber() == 0)) {
                errors.rejectValue("offers", "", "Could not be empty");
            }
            if (offerDTO.getId() == 0 && offerDTO.getGroupName().isEmpty() && offerDTO.getAdcomboNumber() != 0) {
                errors.rejectValue("offers", "", "Could not be empty");
            }
            if (offerDTO.getId() == 0 && !offerDTO.getGroupName().isEmpty() && offerDTO.getAdcomboNumber() == 0) {
                errors.rejectValue("offers", "", "Could not be empty");
            }
        }

    }
}
