package ru.set404.AdsMetrika.scheduled.googlesheets;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@SessionScope
@NoArgsConstructor
@Setter
public class SpreadSheet {
    private GoogleAuthorizeConfig googleAuthorizeConfig;
    private Sheets sheetsService;

    @Autowired
    public SpreadSheet(GoogleAuthorizeConfig googleAuthorizeConfig) {
        this.googleAuthorizeConfig = googleAuthorizeConfig;
    }

    public void authorize(String code) {
        sheetsService = googleAuthorizeConfig.getSheetsService(code);
    }

    public void writeTable(String code, String sheetId, List<List<Object>> combinedStatsObject, LocalDate date) {
        authorize(code);
        try {
            copyTable(sheetId, combinedStatsObject.size() - 3);

            String dateString = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                    .withLocale(new Locale.Builder().setLanguage("ru").build())
                    .format(date);
            sheetsService.spreadsheets().values()
                    .update(sheetId, "A1", new ValueRange().setValues(List.of(List.of(dateString))))
                    .setValueInputOption("RAW")
                    .execute();

            ValueRange body = new ValueRange()
                    .setValues(combinedStatsObject);
            sheetsService.spreadsheets().values()
                    .update(sheetId, "A4", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

        } catch (GoogleJsonResponseException e) {
            authorize(GoogleAuthorizeConfig.PERMISSION_DENIED);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
