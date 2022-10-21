package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;


@Getter
@Setter
@Entity
@Table(name = "Offer")
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private User owner;

    @NotEmpty(message = "Номер не может быть пустым")
    @Column(name = "adcombo_number", nullable = false)
    private int adcomboNumber;

    @NotEmpty(message = "Имя не может быть пустым")
    @Column(name = "group_name", nullable = false)
    private String groupName;

    @NotEmpty(message = "Имя не может быть пустым")
    @Column(name = "network_name", nullable = false)
    private String networkName;
}
