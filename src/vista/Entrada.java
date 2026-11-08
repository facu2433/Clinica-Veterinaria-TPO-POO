package vista;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Lectura de datos por teclado. Cada método repite la pregunta hasta que el dato sea
 * válido, capturando solo las excepciones esperables (NumberFormatException y
 * DateTimeParseException) y explicando el formato correcto.
 *
 * Si se cierra la entrada (fin de archivo) el Scanner lanza NoSuchElementException,
 * que no se captura acá: sube hasta MenuConsola.iniciar(), que guarda y termina.
 */
public class Entrada {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final Scanner scanner;

    public Entrada(Scanner scanner) {
        this.scanner = scanner;
    }

    private String leerLinea(String mensaje) {
        System.out.print(mensaje + ": ");
        return scanner.nextLine().trim();
    }

    /** Texto obligatorio: repite hasta que no esté vacío. */
    public String leerTexto(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("  Este dato es obligatorio.");
        }
    }

    /** Texto que se puede dejar vacío (devuelve "" si el usuario solo apreta Enter). */
    public String leerTextoOpcional(String mensaje) {
        return leerLinea(mensaje);
    }

    public int leerEntero(String mensaje, int min, int max) {
        while (true) {
            String texto = leerLinea(mensaje);
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // cae al mensaje de abajo: no era un número entero
            }
            System.out.println("  Ingrese un número entero entre " + min + " y " + max + ".");
        }
    }

    /** Número decimal mayor a cero; acepta coma o punto como separador. */
    public double leerDecimalPositivo(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (valor > 0 && !Double.isInfinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // no era un número
            }
            System.out.println("  Ingrese un número mayor a cero (ej. 12,5).");
        }
    }

    public double leerPorcentaje(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje).replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (valor >= 0 && valor <= 100) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // no era un número
            }
            System.out.println("  Ingrese un porcentaje entre 0 y 100 (ej. 15).");
        }
    }

    public LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerLinea(mensaje + " (dd/mm/aaaa)"), FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("  Fecha inválida. Use el formato dd/mm/aaaa, por ejemplo 15/03/2026.");
            }
        }
    }

    public LocalDateTime leerFechaHora(String mensaje) {
        while (true) {
            try {
                return LocalDateTime.parse(leerLinea(mensaje + " (dd/mm/aaaa hh:mm)"), FECHA_HORA);
            } catch (DateTimeParseException e) {
                System.out.println("  Fecha u hora inválida. Use dd/mm/aaaa hh:mm, por ejemplo 16/11/2026 10:30.");
            }
        }
    }

    public LocalTime leerHora(String mensaje) {
        while (true) {
            try {
                return LocalTime.parse(leerLinea(mensaje + " (hh:mm)"), HORA);
            } catch (DateTimeParseException e) {
                System.out.println("  Hora inválida. Use el formato hh:mm, por ejemplo 09:30.");
            }
        }
    }

    public boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerLinea(mensaje + " (s/n)").toLowerCase();
            if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
                return true;
            }
            if (respuesta.equals("n") || respuesta.equals("no")) {
                return false;
            }
            System.out.println("  Responda s o n.");
        }
    }

    /**
     * Muestra una lista numerada y devuelve la opción elegida, o null si no hay opciones
     * o el usuario cancela (0).
     */
    public <T> T elegir(String titulo, List<T> opciones, Function<T, String> descripcion) {
        if (opciones.isEmpty()) {
            System.out.println("No hay " + titulo.toLowerCase() + " disponibles.");
            return null;
        }
        System.out.println(titulo + ":");
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + descripcion.apply(opciones.get(i)));
        }
        System.out.println("  0. Cancelar");
        int numero = leerEntero("Elija una opción", 0, opciones.size());
        return (numero == 0) ? null : opciones.get(numero - 1);
    }
}
