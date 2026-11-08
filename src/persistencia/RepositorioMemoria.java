package persistencia;

/** Para pruebas: guarda en memoria, no toca el disco. */
public class RepositorioMemoria implements RepositorioClinica {
    private DatosClinica datos;

    public RepositorioMemoria() {
        this.datos = new DatosClinica();
    }

    @Override
    public void guardar(DatosClinica clinica) {
        this.datos = clinica;
    }

    @Override
    public DatosClinica recuperar() {
        return this.datos;
    }
}
