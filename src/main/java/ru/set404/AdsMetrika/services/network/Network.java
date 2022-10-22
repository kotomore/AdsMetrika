package ru.set404.AdsMetrika.services.network;

public enum Network {
    ADCOMBO ("adcombo"),
    EXO ("exo"),
    TF ("tf");
    private String text;

    Network(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
