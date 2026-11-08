package modelo;

/** Crédito en cuotas: 8 % de recargo sobre el total (sin importar la cantidad de cuotas). */
public class PagoCreditoCuotas implements PoliticaDeCobro {
    private static final long serialVersionUID = 1L;

    // Porcentaje "tal cual": 8 significa 8 %, no 1,08 (convención de la guía, 3.3).
    public static final double RECARGO_PORCENTAJE = 8;

    private int cuotas;

    public PagoCreditoCuotas(int cuotas) {
        if (cuotas < 1) {
            throw new IllegalArgumentException("La cantidad de cuotas debe ser al menos 1.");
        }
        this.cuotas = cuotas;
    }

    @Override
    public double aplicar(double subtotal) {
        return subtotal * (1 + RECARGO_PORCENTAJE / 100.0);
    }

    public int getCuotas() {
        return cuotas;
    }
}
