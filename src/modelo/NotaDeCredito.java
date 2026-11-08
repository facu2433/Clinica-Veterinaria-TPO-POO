package modelo;

import java.io.Serializable;
import java.time.LocalDate;

/** Anula una factura emitida. Se crea únicamente desde Factura.anular(...). */
public class NotaDeCredito implements Serializable {
    private static final long serialVersionUID = 1L;

    private Factura facturaAnulada;
    private String descripcion;
    private LocalDate fecha;
    private double importe;

    // Package-private: solo Factura la crea (así una nota siempre queda ligada a su factura).
    NotaDeCredito(Factura facturaAnulada, String descripcion, double importe) {
        this.facturaAnulada = facturaAnulada;
        this.descripcion = descripcion;
        this.fecha = LocalDate.now();
        this.importe = importe;
    }

    public Factura getFacturaAnulada() {
        return facturaAnulada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    /** Importe que se acredita: el total de la factura anulada. */
    public double getImporte() {
        return importe;
    }
}
