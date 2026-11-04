package modelo;

import java.io.Serializable;

public class PlanSalud implements Serializable {
    private String nombre;
    private double descuento;  

    public PlanSalud(String nombre, double descuento) {
        if (descuento < 0 || descuento > 100) {
            throw new IllegalArgumentException(
                "El descuento debe estar entre 0 y 100.");
        }
        this.nombre = nombre;
        this.descuento = descuento;
    }

    public String getNombre() {
        return nombre;
    }

    public double getDescuento() {
        return descuento;
    }
}