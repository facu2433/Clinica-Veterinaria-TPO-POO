package persistencia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import modelo.Factura;
import modelo.Mascota;
import modelo.Persona;
import modelo.Producto;
import modelo.Tarifario;
import modelo.Turno;
import modelo.Vacuna;

/**
 * Contenedor ÚNICO de todo lo que se persiste. Se guarda un solo objeto raíz para que
 * Java escriba cada objeto una sola vez y conserve las referencias cruzadas
 * (por ejemplo, un turno referenciado desde la lista y desde la agenda del veterinario).
 *
 * Se usan Persona y Mascota (no las subclases) para conservar el polimorfismo.
 * Para obtener un tipo concreto se filtra con instanceof.
 */
public class DatosClinica implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Persona> personas = new ArrayList<>();
    private List<Mascota> mascotas = new ArrayList<>();
    private List<Turno> turnos = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();
    private List<Factura> facturas = new ArrayList<>();
    private List<Vacuna> vacunas = new ArrayList<>();      // catálogo de vacunas
    private Tarifario tarifario = new Tarifario();         // precio de cada tipo de atención

    public List<Persona> getPersonas() {
        return personas;
    }

    public List<Mascota> getMascotas() {
        return mascotas;
    }

    public List<Turno> getTurnos() {
        return turnos;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Factura> getFacturas() {
        return facturas;
    }

    public List<Vacuna> getVacunas() {
        return vacunas;
    }

    public Tarifario getTarifario() {
        return tarifario;
    }
}
