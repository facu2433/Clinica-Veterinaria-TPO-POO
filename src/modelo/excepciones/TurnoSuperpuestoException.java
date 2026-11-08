package modelo.excepciones;

public class TurnoSuperpuestoException extends Exception {
    private static final long serialVersionUID = 1L;

    public TurnoSuperpuestoException(String mensaje) {
        super(mensaje);
    }
}