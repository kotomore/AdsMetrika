package ru.set404.AdsMetrika.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.CredentialsDTO;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;

import java.util.List;

@Service
public class CredentialsService {
    private final CredentialsRepository credentialsRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CredentialsService(CredentialsRepository credentialsRepository, ModelMapper modelMapper) {
        this.credentialsRepository = credentialsRepository;
        this.modelMapper = modelMapper;
    }

    public void deleteById(int id) {
        credentialsRepository.deleteById(id);
    }

    public List<CredentialsDTO> getUserCredentialsList(User user) {
        return credentialsRepository.findByOwner(user).stream()
                .map(credentials -> modelMapper.map(credentials, CredentialsDTO.class)).toList();
    }

    public void saveCredentialsDTO(CredentialsDTO credentialsDTO, User user) {
        Credentials credentials = modelMapper.map(credentialsDTO, Credentials.class);
        credentials.setOwner(user);
        credentialsRepository.save(credentials);
    }
}
