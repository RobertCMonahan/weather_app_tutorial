package model;

public class Temperature {
    private double temp;
    private float minTemp;
    private float maxTemp;

    public float getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(float maxTemp) {
        this.maxTemp = maxTemp;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(float minTemp) {
        this.minTemp = minTemp;
    }

    public double getTemp() {
        double truncatedTemp = Math.floor(100 * temp) / 100;
        return truncatedTemp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }


}
