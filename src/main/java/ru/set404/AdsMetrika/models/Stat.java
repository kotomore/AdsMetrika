package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;
import ru.set404.AdsMetrika.network.Network;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Stat")
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "cost", nullable = false)
    private double spend;

    @Column(name = "approve_cost", nullable = false)
    private double revenue;

    @Enumerated(EnumType.STRING)
    @Column(name = "network_name", nullable = false)
    private Network networkName;
}
