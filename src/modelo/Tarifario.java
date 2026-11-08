package modelo;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Precio de cada tipo de atención. El enunciado no los define, así que viven acá
 * (y no en el enum TipoAtencion, que es de la Parte A y solo guarda la duración).
 * Los valores se cargan al iniciar el sistema.
 */
public class Tarifario implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<TipoAtencion, Double> precios = new EnumMap<>(TipoAtencion.class);

    public void definirPrecio(TipoAtencion tipo, double precio) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de atención es obligatorio.");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero.");
        }
        precios.put(tipo, precio);
    }

    public boolean tienePrecio(TipoAtencion tipo) {
        return precios.containsKey(tipo);
    }

    public double getPrecio(TipoAtencion tipo) {
        Double precio = precios.get(tipo);
        if (precio == null) {
            throw new IllegalStateException("No hay un precio definido para " + tipo + ".");
        }
        return precio;
    }
}
