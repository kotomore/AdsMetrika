package ru.set404.AdsMetrika.services.network.ads;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@Service
public interface NetworkStats {
    Map<Integer, NetworkStatEntity> getStat(Map<Integer, String> networkOffer, LocalDate dateStart, LocalDate dateEnd)
            throws IOException, InterruptedException;
}
