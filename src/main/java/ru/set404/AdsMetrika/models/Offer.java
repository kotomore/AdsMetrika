package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;
import ru.set404.AdsMetrika.services.network.Network;

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

    @Column(name = "adcombo_number", nullable = false)
    private int adcomboNumber;

    @NotEmpty
    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Enumerated(EnumType.STRING)
    @Column(name = "network_name", nullable = false)
    private Network networkName;
}
