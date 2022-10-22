package ru.set404.AdsMetrika.services.network.adsnetworks;

public class NetworkStatEntity {
    private int clicks;
    private double cost;

    public NetworkStatEntity(int clicks, double cost) {
        this.clicks = clicks;
        this.cost = cost;
    }

    public int getClicks() {
        return clicks;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.valueOf(clicks).replace(".",",") + "\t" +
                String.valueOf(cost).replace(".",",");
    }
}
