package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;
import ru.set404.AdsMetrika.network.Network;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Entity
@Table(name = "Credentials")
public class Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private User owner;

    @NotEmpty(message = "Username empty")
    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "network_name", nullable = false)
    private Network networkName;
}
