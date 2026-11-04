package modelo.excepciones;

public class TurnoSuperpuestoException extends Exception {
    public TurnoSuperpuestoException(String mensaje) {
        super(mensaje);
    }
}