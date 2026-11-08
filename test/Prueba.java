/**
 * Ayuda mínima para escribir las pruebas sin librerías externas.
 * Cada prueba es un método chico que llama a correr(...); si algo no se cumple
 * lanza una Falla y la prueba se marca como fallida.
 */
public class Prueba {

    public interface Cuerpo {
        void correr() throws Exception;
    }

    private static class Falla extends RuntimeException {
        Falla(String mensaje) {
            super(mensaje);
        }
    }

    private static int ok = 0;
    private static int fallidas = 0;

    /** Ejecuta una prueba y muestra si salió bien. */
    public static void correr(String nombre, Cuerpo cuerpo) {
        try {
            cuerpo.correr();
            ok++;
            System.out.println("  OK     " + nombre);
        } catch (Falla f) {
            fallidas++;
            System.out.println("  FALLA  " + nombre + " -> " + f.getMessage());
        } catch (Exception e) {
            fallidas++;
            System.out.println("  ERROR  " + nombre + " -> " + e);
        }
    }

    public static void afirmar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new Falla(mensaje);
        }
    }

    public static void afirmarImporte(double esperado, double real, String mensaje) {
        if (Math.abs(esperado - real) > 0.005) {
            throw new Falla(mensaje + " (esperado " + esperado + ", obtenido " + real + ")");
        }
    }

    /** Verifica que el código lance esa excepción. */
    public static void esperar(Class<? extends Exception> tipo, Cuerpo cuerpo, String mensaje) {
        try {
            cuerpo.correr();
        } catch (Exception e) {
            if (tipo.isInstance(e)) {
                return;
            }
            throw new Falla(mensaje + ": lanzó " + e.getClass().getSimpleName()
                    + " y se esperaba " + tipo.getSimpleName());
        }
        throw new Falla(mensaje + ": no lanzó " + tipo.getSimpleName());
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
    }

    /** Muestra el resumen y termina con código 1 si alguna prueba falló. */
    public static void terminar() {
        System.out.println();
        System.out.println("Pruebas: " + (ok + fallidas) + " | Correctas: " + ok + " | Fallidas: " + fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }
}
