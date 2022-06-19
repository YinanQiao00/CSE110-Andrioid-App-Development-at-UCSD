package edu.ucsd.cse110.zooseeker.component;

public class PlanCard {
    public String exhibit;
    public String location;
    public double distance;
    public int rank;

    public PlanCard(String exhibit, String location, double distance) {
        this.exhibit = exhibit;
        this.location = location;
        this.distance = distance;
        this.rank = 1;
    }
    public void setExhibit(String exhibit) {
        this.exhibit = exhibit;
    }
    public String getExhibit() {
        return exhibit;
    }
    public void setLocation(String locatioon) {
        this.location = location;
    }
    public String getLocation() {
        return location;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public double getDistance() {
        return distance;
    }
    public void setRank(int rank) {
        this.rank = rank;
    }
    public int getRank() {
        return rank;
    }
}
