package ru.set404.AdsMetrika.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.AdsMetrika.dto.CredentialsDTO;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;
import ru.set404.AdsMetrika.network.Network;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CredentialsService {
    private final CredentialsRepository credentialsRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public CredentialsService(CredentialsRepository credentialsRepository, ModelMapper modelMapper) {
        this.credentialsRepository = credentialsRepository;
        this.modelMapper = modelMapper;
    }

    @Cacheable("credentials")
    public Set<Network> userNetworks(User user) {
        Set<Network> networks = credentialsRepository.findByOwner(user).stream()
                .collect(Collectors.groupingBy(Credentials::getNetworkName)).keySet();
        networks.remove(Network.ADCOMBO);
        return networks;
    }

    @CacheEvict(value = "credentials", allEntries = true)
    public void deleteById(int id) {
        credentialsRepository.deleteById(id);
    }

    @Cacheable("credentials")
    public List<CredentialsDTO> getUserCredentialsList(User user) {
        System.out.println();
        return credentialsRepository.findByOwner(user).stream()
                .map(credentials -> modelMapper.map(credentials, CredentialsDTO.class)).toList();
    }

    @Transactional
    @CacheEvict(value = "credentials", allEntries = true)
    public void saveCredentialsDTO(Credentials credentials, User user) {
        if (credentials.getUsername().isEmpty())
            credentialsRepository.deleteById(credentials.getId());
        else {
            credentials.setOwner(user);
            credentialsRepository.save(credentials);
        }

    }
}
