package persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Persistencia con serialización de objetos (ObjectOutputStream / ObjectInputStream). */
public class RepositorioArchivo implements RepositorioClinica {
    private final String ruta;

    public RepositorioArchivo(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo es obligatoria.");
        }
        this.ruta = ruta;
    }

    /**
     * Guarda primero en un archivo temporal y recién después reemplaza al definitivo:
     * si algo falla a mitad de la escritura, el archivo anterior queda intacto.
     */
    @Override
    public void guardar(DatosClinica clinica) throws IOException {
        File temporal = new File(ruta + ".tmp");
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(temporal))) {
            salida.writeObject(clinica);
        }
        Files.move(temporal.toPath(), new File(ruta).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /** Si el archivo no existe (primera ejecución) devuelve datos vacíos en vez de fallar. */
    @Override
    public DatosClinica recuperar() throws IOException, ClassNotFoundException {
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return new DatosClinica();
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            Object leido = entrada.readObject();
            if (!(leido instanceof DatosClinica)) {
                throw new InvalidClassException("El archivo " + ruta + " no contiene datos de la clínica.");
            }
            return (DatosClinica) leido;
        }
    }

    /** La ruta es relativa al directorio desde donde se ejecuta: sirve para saber dónde quedó el archivo. */
    public String getRutaAbsoluta() {
        return new File(ruta).getAbsolutePath();
    }
}
