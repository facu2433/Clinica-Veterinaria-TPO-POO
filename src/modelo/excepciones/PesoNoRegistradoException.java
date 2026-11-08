package modelo.excepciones;

public class PesoNoRegistradoException extends Exception {
    private static final long serialVersionUID = 1L;

    public PesoNoRegistradoException(String mensaje) {
        super(mensaje);
    }
}