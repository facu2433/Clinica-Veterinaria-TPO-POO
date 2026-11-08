package modelo;

import java.io.Serializable;

public class PlanSalud implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private double descuento; // porcentaje "tal cual": 15 significa 15 %, no 0,15

    public PlanSalud(String nombre, double descuento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del plan es obligatorio.");
        }
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

    @Override
    public String toString() {
        return nombre + " (" + descuento + " % de descuento)";
    }
}
