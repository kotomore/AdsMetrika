package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.StatsRepository;
import ru.set404.AdsMetrika.network.Network;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatsService {
    private final StatsRepository statsRepository;

    @Autowired
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public List<Stat> getStatsList(User user, LocalDate dateStart) {
        return statsRepository.findAllByOwnerAndCreatedDateAfter(user, dateStart);
    }

    public void saveStatDTOList(List<StatDTO> statDTOList, User user, Network network, LocalDate date) {

        double spend = 0;
        double revenue = 0;
        for (StatDTO statDTO : statDTOList) {
            spend += statDTO.getSpend();
            revenue += statDTO.getRevenue();
        }
        Stat stat = new Stat();
        stat.setSpend(spend);
        stat.setRevenue(revenue);
        stat.setOwner(user);
        stat.setNetworkName(network);
        stat.setCreatedDate(date);
        stat.setId(statsRepository.findSimilar(user, network, date).orElse(0));
        statsRepository.save(stat);
    }
}
