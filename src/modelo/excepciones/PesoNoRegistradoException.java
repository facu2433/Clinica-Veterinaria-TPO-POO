package modelo.excepciones;

public class PesoNoRegistradoException extends Exception {
    public PesoNoRegistradoException(String mensaje) {
        super(mensaje);
    }
}