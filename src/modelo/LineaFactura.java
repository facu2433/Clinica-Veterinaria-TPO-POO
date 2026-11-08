package modelo;

import java.io.Serializable;

/**
 * Una línea de la factura. Es una única clase (sin herencia): en la línea de la
 * consulta la referencia al insumo es null; en las de insumos apunta al producto.
 */
public class LineaFactura implements Serializable {
    private static final long serialVersionUID = 1L;

    private String descripcion;
    private int cantidad;
    private double precio;      // precio unitario al momento de facturar
    private Producto insumo;    // null en la línea de la consulta

    // Package-private: las líneas las crea Factura (patrón Creador).
    LineaFactura(String descripcion, int cantidad, double precio, Producto insumo) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la línea es obligatoria.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precio = precio;
        this.insumo = insumo;
    }

    public double getSubtotal() {
        return cantidad * precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public Producto getInsumo() {
        return insumo;
    }
}
