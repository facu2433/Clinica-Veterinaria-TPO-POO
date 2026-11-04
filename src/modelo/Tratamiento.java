package modelo;

import java.io.Serializable;

public class Tratamiento implements Serializable {
    private Producto medicamento;
    private double dosis;
    private String frecuencia;
    private int duracionDias;

    public Tratamiento(Producto medicamento, double dosis, String frecuencia,
                       int duracionDias) {
        if (dosis <= 0 || duracionDias <= 0) {
            throw new IllegalArgumentException("Dosis y duración deben ser mayores a cero.");
        }
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.duracionDias = duracionDias;
    }

    public Producto getMedicamento() { return medicamento; }
    public double getDosis() { return dosis; }
    public String getFrecuencia() { return frecuencia; }
    public int getDuracionDias() { return duracionDias; }
}