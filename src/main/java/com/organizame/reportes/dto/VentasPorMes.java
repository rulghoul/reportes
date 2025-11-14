package com.organizame.reportes.dto;

public class VentasPorMes {
    private String mes;
    private double stellantis;
    private double renault;
    private double volkswagen;
    private double generalMotors;
    private double hyundai;

    public VentasPorMes(String mes, double s, double r, double v, double g, double h) {
        this.mes = mes;
        this.stellantis = s;
        this.renault = r;
        this.volkswagen = v;
        this.generalMotors = g;
        this.hyundai = h;
    }

    // Getters
    public String getMes() { return mes; }
    public double getStellantis() { return stellantis; }
    public double getRenault() { return renault; }
    public double getVolkswagen() { return volkswagen; }
    public double getGeneralMotors() { return generalMotors; }
    public double getHyundai() { return hyundai; }
}