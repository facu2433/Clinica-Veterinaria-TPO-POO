package modelo.excepciones;

public class EdadMinimaVacunaException extends Exception {
    private static final long serialVersionUID = 1L;

    public EdadMinimaVacunaException(String mensaje) {
        super(mensaje);
    }
}