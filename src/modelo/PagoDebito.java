package modelo;

/** Débito: sin descuento ni recargo. */
public class PagoDebito implements PoliticaDeCobro {
    private static final long serialVersionUID = 1L;

    @Override
    public double aplicar(double subtotal) {
        return subtotal;
    }
}
