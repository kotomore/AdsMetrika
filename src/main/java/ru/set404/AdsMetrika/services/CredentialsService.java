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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    @Cacheable("networks")
    public Set<Network> userNetworks(User user) {
        Set<Network> networks = getUserCredentialsList(user).stream()
                .collect(Collectors.groupingBy(CredentialsDTO::getNetworkName)).keySet();
        networks.remove(Network.ADCOMBO);
        return networks;
    }

    public Credentials getUserCredentialsbyNetwork(User user, Network network) {
        return credentialsRepository.findCredentialsByOwnerAndNetworkName(user, network).orElseThrow(() ->
                new RuntimeException("Couldn't find %s API. Check it".formatted(network.getFullName())));
    }

    public Map<Network, Credentials> getUserCredentials(User user) {
        List<Credentials> credentials = credentialsRepository.findByOwner(user);
        return credentials.stream().collect(Collectors.toMap(Credentials::getNetworkName, Function.identity()));
    }

    @CacheEvict(value = {"credentials", "networks"}, allEntries = true)
    public void remove(Credentials credentials) {
        credentialsRepository.delete(credentials);
    }

    @Cacheable("credentials")
    public List<CredentialsDTO> getUserCredentialsList(User user) {
        return credentialsRepository.findByOwner(user).stream()
                .map(credentials -> modelMapper.map(credentials, CredentialsDTO.class)).toList();
    }

    @Transactional
    @CacheEvict(value = {"credentials", "networks"}, allEntries = true)
    public void save(Credentials credentials, User user) {
        if (credentials.getUsername().isEmpty())
            credentialsRepository.deleteById(credentials.getId());
        else {
            credentials.setOwner(user);
            credentialsRepository.save(credentials);
        }

    }
}
