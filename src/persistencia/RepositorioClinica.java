package persistencia;

import java.io.IOException;

/**
 * El controlador depende de esta interfaz y no de las clases concretas (DIP).
 * La ruta del archivo es un detalle de RepositorioArchivo, por eso no aparece acá.
 */
public interface RepositorioClinica {

    void guardar(DatosClinica clinica) throws IOException;

    /** Si todavía no hay nada guardado devuelve un DatosClinica vacío (nunca null). */
    DatosClinica recuperar() throws IOException, ClassNotFoundException;
}
