package ru.set404.AdsMetrika.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Person")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @OneToMany(mappedBy = "owner")
    private List<Offer> offers;

    @OneToMany(mappedBy = "owner")
    private List<Credentials> credentials;

    @NotEmpty(message = "Username empty")
    @Size(min = 2, max = 100, message = "2 chars and more")
    @Column(name = "username", unique = true)
    private String username;


    @NotEmpty(message = "Password empty")
    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    public User() {
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
