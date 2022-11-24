package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.scheduled.googlesheets.SpreadSheet;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.time.LocalDate;

@Service
public class GoogleSpreadSheetService {
    private final SpreadSheet spreadSheet;
    private final SettingsService settingsService;

    @Autowired
    public GoogleSpreadSheetService(SpreadSheet spreadSheet, SettingsService settingsService) {
        this.spreadSheet = spreadSheet;
        this.settingsService = settingsService;
    }

    public void writeSpreadSheetTable(String code, User user, TableDTO combinedStats, LocalDate date) {
        String sheetId = settingsService.userSettings(user).getSpreadSheetId();
        spreadSheet.writeTable(code, sheetId, StatisticsUtilities.convertTableDTOToObject(combinedStats), date);
    }
}
