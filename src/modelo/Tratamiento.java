package modelo;

import java.io.Serializable;

/**
 * Solo guarda la referencia al Producto: el stock NO se toca al indicar el
 * tratamiento, se descuenta al confirmar la consulta (Parte B).
 */
public class Tratamiento implements Serializable {
    private static final long serialVersionUID = 1L;

    private Producto medicamento;
    private double dosis;
    private String frecuencia;
    private int duracionDias;

    public Tratamiento(Producto medicamento, double dosis, String frecuencia,
                       int duracionDias) {
        if (medicamento == null) {
            throw new IllegalArgumentException("El medicamento es obligatorio.");
        }
        if (dosis <= 0 || duracionDias <= 0) {
            throw new IllegalArgumentException("Dosis y duración deben ser mayores a cero.");
        }
        if (frecuencia == null || frecuencia.isBlank()) {
            throw new IllegalArgumentException("La frecuencia es obligatoria.");
        }
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.frecuencia = frecuencia;
        this.duracionDias = duracionDias;
    }

    public Producto getMedicamento() {
        return medicamento;
    }

    public double getDosis() {
        return dosis;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public int getDuracionDias() {
        return duracionDias;
    }
}
