package ru.set404.AdsMetrika.network;

public enum Network {
    ADCOMBO("adcombo", "Adcombo"),
    EXO("exo", "ExoClick"),
    TF("tf", "Traffic Factory");
    private final String name;
    private final String fullName;

    Network(String name, String fullName) {
        this.name = name;
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

}
