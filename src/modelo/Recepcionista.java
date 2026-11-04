package modelo;

public class Recepcionista extends Persona {
    public Recepcionista(String nombre, String apellido, String DNI, String telefono) {
        super(nombre, apellido, DNI, telefono);
    }

    @Override 
    public String descripcion() {
        return "Recepcionista: \n" + super.descripcion();
    }
    
}
