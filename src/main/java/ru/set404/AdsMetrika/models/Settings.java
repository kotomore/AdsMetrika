package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Settings")
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @OneToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "spreadsheet_id")
    private String spreadSheetId;

    @Column(name = "spreadsheet_enabled")
    private boolean spreadSheetEnabled;

    @Column(name = "spreadsheet_schedule_enabled")
    private boolean spreadSheetScheduleEnabled;

    @Column(name = "telegram_username")
    private String telegramUsername;

    @Column(name = "telegram_enabled")
    private boolean telegramEnabled;
}
