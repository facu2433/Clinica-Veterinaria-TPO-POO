package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.excepciones.LoteVencidoException;
import modelo.excepciones.StockInsuficienteException;

/**
 * Producto del stock (medicamentos e insumos).
 * El stock NO es un atributo: es la suma de las cantidades de sus lotes no vencidos.
 */
public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nombre;
    private double precio;          // precio unitario de venta; 0 = todavía sin precio definido
    private int stockMinimo;
    private List<LoteProducto> lotes;        // siempre ordenados por vencimiento
    private List<Proveedor> proveedores;

    /** Constructor mínimo (el de la versión provisoria): sin precio ni stock mínimo. */
    public Producto(String codigo, String nombre) {
        this(codigo, nombre, 0, 0);
    }

    public Producto(String codigo, String nombre, double precio, int stockMinimo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stockMinimo = stockMinimo;
        this.lotes = new ArrayList<>();
        this.proveedores = new ArrayList<>();
    }

    // ---- Lotes y proveedores ---------------------------------------------------

    public void agregarLote(LoteProducto lote) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }
        lotes.add(lote);
        Collections.sort(lotes);   // orden natural de LoteProducto: el que vence antes primero
    }

    public void agregarProveedor(Proveedor proveedor) {
        if (proveedor == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio.");
        }
        if (!proveedores.contains(proveedor)) {
            proveedores.add(proveedor);
        }
    }

    // ---- Stock -------------------------------------------------------------------

    /** Unidades utilizables hoy: suma de los lotes no vencidos. */
    public int getStockActual() {
        return getStockActual(LocalDate.now());
    }

    public int getStockActual(LocalDate hoy) {
        int total = 0;
        for (LoteProducto lote : lotes) {
            if (!lote.estaVencido(hoy)) {
                total += lote.getCantidad();
            }
        }
        return total;
    }

    /** Alerta de reposición: se llegó (o se bajó de) al stock mínimo. */
    public boolean necesitaReposicion() {
        return necesitaReposicion(LocalDate.now());
    }

    public boolean necesitaReposicion(LocalDate hoy) {
        return getStockActual(hoy) <= stockMinimo;
    }

    /** ¿Alcanza el stock utilizable para esta cantidad? Sirve para validar antes de descontar. */
    public boolean puedeDescontar(int cantidad) {
        return puedeDescontar(cantidad, LocalDate.now());
    }

    public boolean puedeDescontar(int cantidad, LocalDate hoy) {
        return cantidad > 0 && getStockActual(hoy) >= cantidad;
    }

    /**
     * Descuenta stock consumiendo primero el lote que vence antes. Los lotes vencidos no se usan.
     * La operación es todo o nada: si no alcanza el stock no se toca ningún lote.
     *
     * @return true si, después de descontar, el producto necesita reposición.
     * @throws LoteVencidoException       hay unidades en el depósito pero están en lotes vencidos.
     * @throws StockInsuficienteException no hay unidades suficientes.
     */
    public boolean descontarStock(int cantidad)
            throws StockInsuficienteException, LoteVencidoException {
        return descontarStock(cantidad, LocalDate.now());
    }

    public boolean descontarStock(int cantidad, LocalDate hoy)
            throws StockInsuficienteException, LoteVencidoException {
        verificarStock(cantidad, hoy);             // si no alcanza, lanza antes de tocar nada
        int restante = cantidad;
        for (LoteProducto lote : lotes) {          // ya están ordenados por vencimiento
            if (restante == 0) {
                break;
            }
            if (lote.estaVencido(hoy) || lote.getCantidad() == 0) {
                continue;
            }
            int tomar = Math.min(restante, lote.getCantidad());
            lote.descontar(tomar);
            restante -= tomar;
        }
        return necesitaReposicion(hoy);
    }

    /**
     * Comprueba, SIN modificar el stock, que se puedan descontar esas unidades.
     * Sirve para validar varios insumos antes de descontar cualquiera de ellos.
     *
     * @throws LoteVencidoException       hay unidades en el depósito pero están en lotes vencidos.
     * @throws StockInsuficienteException no hay unidades suficientes.
     */
    public void verificarStock(int cantidad)
            throws StockInsuficienteException, LoteVencidoException {
        verificarStock(cantidad, LocalDate.now());
    }

    public void verificarStock(int cantidad, LocalDate hoy)
            throws StockInsuficienteException, LoteVencidoException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
        }
        int disponible = getStockActual(hoy);
        if (disponible >= cantidad) {
            return;
        }
        if (getStockEnDeposito() >= cantidad) {
            throw new LoteVencidoException(
                "Hay " + getStockEnDeposito() + " unidades de " + nombre
                + ", pero los lotes vencidos no pueden usarse (sin vencer: " + disponible
                + ", pedidas: " + cantidad + ").");
        }
        throw new StockInsuficienteException(
            "Stock insuficiente de " + nombre + ": se pidieron " + cantidad
            + " y hay " + disponible + " disponibles.");
    }

    // Unidades físicas, incluidos los lotes vencidos (solo para distinguir el motivo del rechazo).
    private int getStockEnDeposito() {
        int total = 0;
        for (LoteProducto lote : lotes) {
            total += lote.getCantidad();
        }
        return total;
    }

    // ---- Datos -------------------------------------------------------------------

    public void setPrecio(double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        }
        this.precio = precio;
    }

    public boolean tienePrecio() {
        return precio > 0;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public List<LoteProducto> getLotes() {
        return Collections.unmodifiableList(lotes);
    }

    public List<Proveedor> getProveedores() {
        return Collections.unmodifiableList(proveedores);
    }
}
