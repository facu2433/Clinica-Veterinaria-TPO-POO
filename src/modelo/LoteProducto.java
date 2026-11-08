package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class LoteProducto implements Serializable, Comparable<LoteProducto> {
    private static final long serialVersionUID = 1L;

    private String numeroLote;
    private int cantidad;
    private LocalDate fechaVencimiento;

    public LoteProducto(String numeroLote, int cantidad, LocalDate fechaVencimiento) {
        if (numeroLote == null || numeroLote.isBlank()) {
            throw new IllegalArgumentException("El número de lote es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad del lote debe ser mayor a cero.");
        }
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }
        this.numeroLote = numeroLote;
        this.cantidad = cantidad;
        this.fechaVencimiento = fechaVencimiento;
    }

    /** Un lote está vencido desde su fecha de vencimiento (inclusive). */
    public boolean estaVencido() {
        return estaVencido(LocalDate.now());
    }

    /** Igual que estaVencido() pero con la fecha de "hoy" explícita (útil para probar). */
    public boolean estaVencido(LocalDate hoy) {
        return !hoy.isBefore(fechaVencimiento);
    }

    // Package-private: el stock solo se descuenta a través de Producto.descontarStock(...).
    void descontar(int cant) {
        if (cant <= 0 || cant > cantidad) {
            throw new IllegalArgumentException(
                "No se pueden descontar " + cant + " unidades de un lote con " + cantidad + ".");
        }
        this.cantidad -= cant;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public int getCantidad() {
        return cantidad;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    /** Orden natural: primero el que vence antes (se consume primero). */
    @Override
    public int compareTo(LoteProducto otro) {
        int porFecha = this.fechaVencimiento.compareTo(otro.fechaVencimiento);
        if (porFecha != 0) {
            return porFecha;
        }
        return this.numeroLote.compareTo(otro.numeroLote);
    }
}
