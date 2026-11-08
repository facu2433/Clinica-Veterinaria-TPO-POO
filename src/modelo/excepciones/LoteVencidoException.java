package modelo.excepciones;

public class LoteVencidoException extends Exception {
    private static final long serialVersionUID = 1L;

    public LoteVencidoException(String mensaje) {
        super(mensaje);
    }
}