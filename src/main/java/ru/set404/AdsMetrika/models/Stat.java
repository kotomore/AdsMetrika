package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;
import ru.set404.AdsMetrika.services.network.Network;

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

    @Column(name = "campaign_id", nullable = false)
    private int campaignId;

    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @Column(name = "clicks", nullable = false)
    private int clicks;

    @Column(name = "cost", nullable = false)
    private double cost;

    @Column(name = "hold_cost", nullable = false)
    private double holdCost;

    @Column(name = "approve_count", nullable = false)
    private int approveCount;

    @Column(name = "approve_cost", nullable = false)
    private int approveCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "network_name", nullable = false)
    private Network networkName;
}
