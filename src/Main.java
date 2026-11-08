import controlador.ControladorClinica;
import java.io.IOException;
import modelo.CalculadorDeFactura;
import persistencia.RepositorioArchivo;
import vista.MenuConsola;

public class Main {
    private static final String ARCHIVO = "clinica.dat";

    public static void main(String[] args) {
        // 1. Instanciar persistencia y utilidades
        RepositorioArchivo repositorio = new RepositorioArchivo(ARCHIVO);
        CalculadorDeFactura calculador = new CalculadorDeFactura();
        System.out.println("Archivo de datos: " + repositorio.getRutaAbsoluta());

        try {
            // 2. Inyectar dependencias al controlador (si el archivo no existe, arranca vacío)
            ControladorClinica controlador = new ControladorClinica(repositorio, calculador);

            // 3. Arrancar la vista
            new MenuConsola(controlador).iniciar();

        } catch (IOException | ClassNotFoundException e) {
            // Archivo ilegible, dañado, o guardado con una versión anterior de las clases
            // Algunas excepciones de lectura (por ejemplo EOFException) no traen mensaje: se muestra su tipo
            String motivo = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
            System.out.println("No se pudo leer el archivo de datos: " + motivo);
            System.out.println("Si el archivo es de una versión anterior del programa, bórrelo para empezar de cero.");
        }
    }
}
