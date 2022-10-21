package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

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
    private Person owner;

    @Column(name = "adcombo_number", nullable = false)
    private int adcomboNumber;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "network_name", nullable = false)
    private String networkName;
}
