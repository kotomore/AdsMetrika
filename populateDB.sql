CREATE TABLE Person
(
    id       int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    username varchar(100) NOT NULL UNIQUE,
    password varchar      NOT NULL,
    role     varchar(100) NOT NULL
);

CREATE TABLE Offer
(
    id             int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    person_id      int REFERENCES Person (id) ON DELETE CASCADE,
    adcombo_number int          not null,
    group_name     varchar(100) not null,
    network_name   varchar(100) not null

);

CREATE TABLE Credentials
(
    id           int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    person_id    int REFERENCES Person (id) ON DELETE CASCADE,
    username     varchar(100) not null,
    password     varchar(100) not null,
    network_name varchar(100) not null UNIQUE

);

CREATE TABLE Stat
(
    id            int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    person_id     int REFERENCES Person (id) ON DELETE CASCADE,
    created_date  date         not null,
    cost          float        not null,
    approve_cost  float        not null,
    network_name  varchar(100) not null
);
