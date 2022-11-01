package ru.set404.AdsMetrika.network.ads;

public class NetworkStats {
    private final int clicks;
    private final double cost;

    public NetworkStats(int clicks, double cost) {
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
        return String.valueOf(clicks).replace(".", ",") + "\t" +
                String.valueOf(cost).replace(".", ",");
    }
}
