package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;

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

    @NotEmpty(message = "Имя пользователя не может быть пустым")
    @Column(name = "username", nullable = false)
    private String username;

    @NotEmpty(message = "Пароль не может быть пустым")
    @Column(name = "password", nullable = false)
    private String password;

    @NotEmpty(message = "Имя сети не может быть пустым")
    @Column(name = "network_name", nullable = false)
    private String networkName;
}
