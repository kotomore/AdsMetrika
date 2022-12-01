package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.StatsRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatsService {
    private final StatsRepository statsRepository;

    @Autowired
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Cacheable("stats")
    public List<Stat> getStatsList(User user, LocalDate dateStart) {
        return statsRepository.findAllByOwnerAndCreatedDateAfter(user, dateStart);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @CacheEvict(value = "stats", allEntries = true)
    public void saveStatDTOList(List<TableDTO> tableDTOList, User user, LocalDate date) {
        List<Stat> statList = new ArrayList<>();
        for (TableDTO tableDTO : tableDTOList) {
            double spend = 0;
            double revenue = 0;
            for (StatDTO statDTO : tableDTO.getCurrentStats()) {
                spend += statDTO.getSpend();
                revenue += statDTO.getRevenue();
            }
            Stat stat = new Stat();
            stat.setSpend(spend);
            stat.setRevenue(revenue);
            stat.setOwner(user);
            stat.setNetworkName(tableDTO.getNetwork());
            stat.setCreatedDate(date);
            stat.setId(statsRepository.findSimilar(user, tableDTO.getNetwork(), date).orElse(0));
            statList.add(stat);
        }
        statsRepository.saveAll(statList);
    }
}
