package ru.set404.AdsMetrika.scheduled.googlesheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.set404.AdsMetrika.exceptions.GoogleAuthTimedOutException;
import ru.set404.AdsMetrika.models.User;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class SpreadSheet {
    private final GoogleAuthorizeUtil googleAuthorizeUtil;
    //Google Spreadsheet id (getting from url)
    private Sheets sheetsService;


    @Autowired
    public SpreadSheet(GoogleAuthorizeUtil googleAuthorizeUtil) {
        this.googleAuthorizeUtil = googleAuthorizeUtil;
    }

    public void authorize() throws GeneralSecurityException, IOException {
        sheetsService = googleAuthorizeUtil.getSheetsService();
    }

    public boolean isAuth() {
        return googleAuthorizeUtil.isAuth();
    }

    public void writeTable(User user, List<List<Object>> combinedStatsObject, LocalDate date) {
        try {
            authorize();

            String spreadSheetId = user.getSettings().getSpreadSheetId();

            copyTable(spreadSheetId, combinedStatsObject.size() - 3);

            String dateString = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                    .withLocale(new Locale.Builder().setLanguage("ru").build())
                    .format(date);
            sheetsService.spreadsheets().values()
                    .update(spreadSheetId, "A1", new ValueRange().setValues(List.of(List.of(dateString))))
                    .setValueInputOption("RAW")
                    .execute();

            ValueRange body = new ValueRange()
                    .setValues(combinedStatsObject);
            sheetsService.spreadsheets().values()
                    .update(spreadSheetId, "A4", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Something wrong with google authorization");
        }
    }

    private int getSecondSheet(String spreadSheetId) throws IOException {
        return sheetsService.spreadsheets()
                .get(spreadSheetId)
                .execute()
                .getSheets()
                .get(1)
                .getProperties()
                .getSheetId();
    }

    private void copyTable(String spreadSheetId, int cellsCount) throws IOException {
        CopyPasteRequest copyRequest = new CopyPasteRequest()
                .setSource(new GridRange().setSheetId(getSecondSheet(spreadSheetId))
                        .setStartColumnIndex(0).setEndColumnIndex(10)
                        .setStartRowIndex(0).setEndRowIndex(12))
                .setDestination(new GridRange().setSheetId(0)
                        .setStartColumnIndex(0).setEndColumnIndex(10)
                        .setStartRowIndex(0).setEndRowIndex(12))
                .setPasteType("PASTE_NORMAL");

        CopyPasteRequest copyCell = new CopyPasteRequest()
                .setSource(new GridRange().setSheetId(0)
                        .setStartColumnIndex(0).setEndColumnIndex(10)
                        .setStartRowIndex(4).setEndRowIndex(5))
                .setDestination(new GridRange().setSheetId(0)
                        .setStartColumnIndex(0).setEndColumnIndex(10)
                        .setStartRowIndex(5).setEndRowIndex(cellsCount + 5))
                .setPasteType("PASTE_NORMAL");

        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setInsertDimension(insertRows(0, 12)));
        requests.add(new Request()
                .setCopyPaste(copyRequest));
        requests.add(new Request()
                .setInsertDimension(insertRows(5, cellsCount + 5)));
        requests.add(new Request()
                .setCopyPaste(copyCell));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(spreadSheetId, body).execute();
    }

    private InsertDimensionRequest insertRows(int start, int end) {
        InsertDimensionRequest insertRow = new InsertDimensionRequest();
        insertRow.setRange(new DimensionRange().setDimension("ROWS").setStartIndex(start).setEndIndex(end).setSheetId(0));
        return insertRow;
    }
}
