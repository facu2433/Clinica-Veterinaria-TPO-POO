package modelo;

import java.util.List;

/**
 * Calcula el total de una factura. Acá vive la regla que combina el plan de salud
 * con el medio de pago (la guía, 5.4, pide que esa comparación NO esté en PoliticaDeCobro).
 */
public class CalculadorDeFactura {

    // Porcentaje "tal cual": 20 significa 20 %.
    public static final double CARGO_CANCELACION_PORCENTAJE = 20;
    // Supuesto (el enunciado no fija el monto): la seña es el 50 % de la consulta general.
    public static final double SENIA_PORCENTAJE = 50;

    /**
     * Reglas:
     * - El descuento o recargo se aplica sobre el total, no por línea.
     * - Efectivo (descuento) y plan de salud NO se acumulan: se aplica el mayor descuento.
     * - Débito (neutro) y crédito (recargo) sí se combinan con el plan: primero se
     *   descuenta el plan y sobre ese importe se aplica el medio de pago.
     */
    public double calcularTotal(List<LineaFactura> lineas, PoliticaDeCobro politica, PlanSalud plan) {
        if (lineas == null || politica == null) {
            throw new IllegalArgumentException("Las líneas y la política de cobro son obligatorias.");
        }
        double subtotal = 0;
        for (LineaFactura linea : lineas) {
            subtotal += linea.getSubtotal();
        }

        double totalConPlan = subtotal;
        if (plan != null) {
            totalConPlan = subtotal * (1 - plan.getDescuento() / 100.0);
        }

        double totalConMedioDePago = politica.aplicar(subtotal);
        double total;
        if (totalConMedioDePago < subtotal) {
            // El medio de pago da descuento (efectivo): no se acumula con el plan, gana el mayor.
            total = Math.min(totalConMedioDePago, totalConPlan);
        } else {
            // Débito o crédito: el plan se descuenta y después se aplica el medio de pago.
            total = politica.aplicar(totalConPlan);
        }
        return redondear(total);
    }

    /** Cargo por cancelar con menos de 2 horas: 20 % del precio real de la consulta. */
    public double calcularCargoCancelacion(double precioConsulta) {
        if (precioConsulta <= 0) {
            throw new IllegalArgumentException("El precio de la consulta debe ser mayor a cero.");
        }
        return redondear(precioConsulta * CARGO_CANCELACION_PORCENTAJE / 100.0);
    }

    /** Seña para volver a pedir turnos tras tres ausencias seguidas: 50 % del precio de la consulta general. */
    public double calcularSenia(double precioConsultaGeneral) {
        if (precioConsultaGeneral <= 0) {
            throw new IllegalArgumentException("El precio de la consulta debe ser mayor a cero.");
        }
        return redondear(precioConsultaGeneral * SENIA_PORCENTAJE / 100.0);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
