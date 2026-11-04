package modelo;

public class Administrador extends Persona {
    public Administrador(String nombre, String apellido, String DNI, String telefono) {
        super(nombre, apellido, DNI, telefono);
    }

    @Override 
    public String descripcion() {
        return "Administrador: \n" + super.descripcion();
    }
    
}
