package modelo;

/** Efectivo: 10 % de descuento sobre el total. */
public class PagoEfectivo implements PoliticaDeCobro {
    private static final long serialVersionUID = 1L;

    // Porcentaje "tal cual": 10 significa 10 %, no 0,10 (convención de la guía, 3.3).
    public static final double DESCUENTO_PORCENTAJE = 10;

    @Override
    public double aplicar(double subtotal) {
        return subtotal * (1 - DESCUENTO_PORCENTAJE / 100.0);
    }
}
