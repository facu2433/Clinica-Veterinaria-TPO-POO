package modelo;

public class Recepcionista extends Persona {
    private static final long serialVersionUID = 1L;

    public Recepcionista(String nombre, String apellido, String DNI, String telefono) {
        super(nombre, apellido, DNI, telefono);
    }

    @Override 
    public String descripcion() {
        return "Recepcionista: \n" + super.descripcion();
    }
    
}
