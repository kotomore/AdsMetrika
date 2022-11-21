package ru.set404.AdsMetrika.scheduled.googlesheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private static final String SPREADSHEET_ID = "157KBw6FLyJ2zQeKyQp_cCa5Pp1mQC0cGPelNP2Iegvo";
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

    public void writeTable(List<List<Object>> combinedStatsObject, LocalDate date) throws IOException, GeneralSecurityException {
        authorize();

        copyTable(combinedStatsObject.size() - 3);

        String dateString = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                .withLocale(new Locale.Builder().setLanguage("ru").build())
                .format(date);
        sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, "A1", new ValueRange().setValues(List.of(List.of(dateString))))
                .setValueInputOption("RAW")
                .execute();

        ValueRange body = new ValueRange()
                .setValues(combinedStatsObject);
        sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, "A4", body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    private int getSecondSheet() throws IOException {
        return sheetsService.spreadsheets()
                .get(SPREADSHEET_ID)
                .execute()
                .getSheets()
                .get(1)
                .getProperties()
                .getSheetId();
    }

    private void copyTable(int cellsCount) throws IOException, GeneralSecurityException {
        CopyPasteRequest copyRequest = new CopyPasteRequest()
                .setSource(new GridRange().setSheetId(getSecondSheet())
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
        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, body).execute();
    }

    private InsertDimensionRequest insertRows(int start, int end) {
        InsertDimensionRequest insertRow = new InsertDimensionRequest();
        insertRow.setRange(new DimensionRange().setDimension("ROWS").setStartIndex(start).setEndIndex(end).setSheetId(0));
        return insertRow;
    }
}
