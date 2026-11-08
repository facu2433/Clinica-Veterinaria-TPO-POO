package modelo.excepciones;

/** El dueño tiene tres ausencias seguidas y todavía no pagó la seña para pedir turnos. */
public class SeniaPendienteException extends Exception {
    private static final long serialVersionUID = 1L;

    public SeniaPendienteException(String mensaje) {
        super(mensaje);
    }
}
