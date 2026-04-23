package dev.nez.edge.messaging.filter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DynamicFilterConfig {

    // ================= POWER =================
    private Boolean powerConsume;
    private Boolean powerOptimise;
    private Integer powerThreshold;

    // ================= SMOKE =================
    private Boolean smokeConsume;
    private Boolean smokeOptimise;
    private Integer smokeThreshold;

    // ================= AIR =================
    private Boolean airConsume;
    private Boolean airOptimise;
    private Integer airThreshold;

    // ================= BATTERY =================
    private Boolean battConsume;
    private Boolean battOptimise;
    private Integer battThreshold;

    @Inject
    public DynamicFilterConfig(FilterConfig filterConfig) {
        this.powerConsume = filterConfig.powerIn().consume();
        this.powerOptimise = filterConfig.powerIn().optimise();
        this.powerThreshold = filterConfig.powerIn().threshold();

        this.smokeConsume = filterConfig.smokeIn().consume();
        this.smokeOptimise = filterConfig.smokeIn().optimise();
        this.smokeThreshold = filterConfig.smokeIn().threshold();

        this.airConsume = filterConfig.airIn().consume();
        this.airOptimise = filterConfig.airIn().optimise();
        this.airThreshold = filterConfig.airIn().threshold();

        this.battConsume = filterConfig.battIn().consume();
        this.battOptimise = filterConfig.battIn().optimise();
        this.battThreshold = filterConfig.battIn().threshold();
    }

    public record ConfigChangeEvent(
        String topic,
        int newCount
    ) {}

    // ================= POWER =================

    public boolean isPowerConsume() {
        return powerConsume;
    }
    public void setPowerConsume(boolean consume) {
        this.powerConsume = consume;
    }

    public boolean isPowerOptimise() {
        return powerOptimise;
    }
    public void setPowerOptimise(boolean optimise) {
        this.powerOptimise = optimise;
    }

    public int getPowerThreshold() {
        return powerThreshold;
    }
    public void setPowerThreshold(int threshold) {
        this.powerThreshold = threshold;
    }

    // ================= SMOKE =================

    public boolean isSmokeConsume() {
        return smokeConsume;
    }
    public void setSmokeConsume(boolean consume) {
        this.smokeConsume = consume;
    }

    public boolean isSmokeOptimise() {
        return smokeOptimise;
    }
    public void setSmokeOptimise(boolean optimise) {
        this.smokeOptimise = optimise;
    }

    public int getSmokeThreshold() {
        return smokeThreshold;
    }
    public void setSmokeThreshold(int threshold) {
        this.smokeThreshold = threshold;
    }

    // ================= AIR =================

    public boolean isAirConsume() {
        return airConsume;
    }
    public void setAirConsume(boolean consume) {
        this.airConsume = consume;
    }

    public boolean isAirOptimise() {
        return airOptimise;
    }
    public void setAirOptimise(boolean optimise) {
        this.airOptimise = optimise;
    }

    public int getAirThreshold() {
        return airThreshold;
    }
    public void setAirThreshold(int threshold) {
        this.airThreshold = threshold;
    }

    // ================= BATTERY =================

    public boolean isBattConsume() {
        return battConsume;
    }
    public void setBattConsume(boolean consume) {
        this.battConsume = consume;
    }

    public boolean isBattOptimise() {
        return battOptimise;
    }
    public void setBattOptimise(boolean optimise) {
        this.battOptimise = optimise;
    }

    public int getBattThreshold() {
        return battThreshold;
    }
    public void setBattThreshold(int threshold) {
        this.battThreshold = threshold;
    }
}
