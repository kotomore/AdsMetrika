package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.scheduled.googlesheets.SpreadSheet;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;

@Service
public class ScheduledService {
    private final SpreadSheet spreadSheet;

    @Autowired
    public ScheduledService(SpreadSheet spreadSheet) {
        this.spreadSheet = spreadSheet;
    }

    public void writeSpreadSheetTable(User user, TableDTO combinedStats, LocalDate date) throws GeneralSecurityException, IOException {
        spreadSheet.writeTable(user, StatisticsUtilities.convertTableDTOToObject(combinedStats), date);
    }
}
