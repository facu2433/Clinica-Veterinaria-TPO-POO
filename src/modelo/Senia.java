package modelo;

import java.io.Serializable;
import java.time.LocalDate;

/** Seña que paga un dueño con tres ausencias seguidas para poder pedir turnos de nuevo. */
public class Senia implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double monto;
    private final LocalDate fecha;

    // Package-private: solo la crea Duenio.registrarSenia (patrón Creador).
    Senia(double monto, LocalDate fecha) {
        this.monto = monto;
        this.fecha = fecha;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
