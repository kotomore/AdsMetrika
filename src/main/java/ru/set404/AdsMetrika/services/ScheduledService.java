package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.scheduled.googlesheets.SpreadSheet;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class ScheduledService {
    private final SpreadSheet spreadSheet;

    @Autowired
    public ScheduledService(SpreadSheet spreadSheet) {
        this.spreadSheet = spreadSheet;
    }

    public void writeSpreadSheetTable(TableDTO combinedStats) throws GeneralSecurityException, IOException {
        spreadSheet.writeTable(StatisticsUtilities.convertTableDTOToObject(combinedStats));
    }
}
