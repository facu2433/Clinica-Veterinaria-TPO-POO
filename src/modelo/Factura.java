package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Una factura por consulta: la consulta es una línea y cada insumo utilizado es otra.
 * Mientras no se emite se pueden agregar líneas; una vez emitida NO se modifica:
 * si hay un error se anula con una nota de crédito.
 */
public class Factura implements Serializable {
    private static final long serialVersionUID = 1L;

    private int numero;                    // se asigna al emitir
    private LocalDate fecha;               // fecha de emisión
    private Consulta consulta;
    private PoliticaDeCobro politicaDeCobro;
    private List<LineaFactura> lineas;

    // Se "congelan" al emitir: el total no debe cambiar si después cambia el dueño o su plan.
    private boolean emitida;
    private PlanSalud planAplicado;
    private double totalEmitido;

    private NotaDeCredito notaDeCredito;   // null mientras no se anule

    public Factura(Consulta consulta, PoliticaDeCobro politicaDeCobro) {
        if (consulta == null || politicaDeCobro == null) {
            throw new IllegalArgumentException("La consulta y la política de cobro son obligatorias.");
        }
        this.consulta = consulta;
        this.politicaDeCobro = politicaDeCobro;
        this.lineas = new ArrayList<>();
    }

    // ---- Armado (patrón Creador: la factura crea sus propias líneas) ----------

    public void agregarLinea(String descripcion, int cantidad, double precio, Producto insumo) {
        exigirNoEmitida();
        lineas.add(new LineaFactura(descripcion, cantidad, precio, insumo));
    }

    /** Línea de la consulta (sin insumo): el precio sale del tarifario según el tipo de atención. */
    public void agregarLineaConsulta(Tarifario tarifario) {
        TipoAtencion tipo = consulta.getTurno().getTipoAtencion();
        agregarLinea("Atención: " + tipo.name().replace('_', ' ').toLowerCase(),
                1, tarifario.getPrecio(tipo), null);
    }

    /** Línea de un insumo usado: toma nombre y precio del producto. */
    public void agregarLineaInsumo(Producto insumo, int cantidad) {
        if (insumo == null) {
            throw new IllegalArgumentException("El insumo es obligatorio.");
        }
        if (!insumo.tienePrecio()) {
            throw new IllegalStateException(
                "El producto " + insumo.getNombre() + " no tiene un precio definido.");
        }
        agregarLinea(insumo.getNombre(), cantidad, insumo.getPrecio(), insumo);
    }

    // ---- Emisión y anulación -------------------------------------------------

    /**
     * Emite la factura: fija número y fecha, y congela el plan de salud del dueño
     * y el total. Desde acá la factura no se puede modificar.
     */
    public void emitir(int numero, CalculadorDeFactura calculador) {
        exigirNoEmitida();
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de factura debe ser mayor a cero.");
        }
        if (calculador == null) {
            throw new IllegalArgumentException("El calculador es obligatorio.");
        }
        if (lineas.isEmpty()) {
            throw new IllegalStateException("No se puede emitir una factura sin líneas.");
        }
        this.planAplicado = planActualDelDuenio();
        this.totalEmitido = calculador.calcularTotal(lineas, politicaDeCobro, planAplicado);
        this.numero = numero;
        this.fecha = LocalDate.now();
        this.emitida = true;
    }

    /** Anula una factura emitida generando su nota de crédito por el total. */
    public NotaDeCredito anular(String motivo) {
        if (!emitida) {
            throw new IllegalStateException("Solo se puede anular una factura emitida.");
        }
        if (notaDeCredito != null) {
            throw new IllegalStateException("La factura " + numero + " ya está anulada.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la anulación es obligatorio.");
        }
        notaDeCredito = new NotaDeCredito(this, motivo, totalEmitido);
        return notaDeCredito;
    }

    /**
     * Total de la factura. Si ya está emitida devuelve el total congelado;
     * si no, lo calcula con el plan que tiene el dueño en este momento.
     */
    public double getTotal(CalculadorDeFactura calculador) {
        if (emitida) {
            return totalEmitido;
        }
        if (calculador == null) {
            throw new IllegalArgumentException("El calculador es obligatorio.");
        }
        return calculador.calcularTotal(lineas, politicaDeCobro, planActualDelDuenio());
    }

    private PlanSalud planActualDelDuenio() {
        Duenio duenio = consulta.getTurno().getMascota().getDuenio();
        return (duenio == null) ? null : duenio.getPlanSalud();
    }

    private void exigirNoEmitida() {
        if (emitida) {
            throw new IllegalStateException("La factura " + numero + " ya fue emitida y no se puede modificar.");
        }
    }

    // ---- Consultas -----------------------------------------------------------

    public boolean isEmitida() {
        return emitida;
    }

    public boolean estaAnulada() {
        return notaDeCredito != null;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public PoliticaDeCobro getPoliticaDeCobro() {
        return politicaDeCobro;
    }

    public NotaDeCredito getNotaDeCredito() {
        return notaDeCredito;
    }

    public List<LineaFactura> getLineas() {
        return Collections.unmodifiableList(lineas);
    }
}
