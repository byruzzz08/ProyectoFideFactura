package modelo;

public class ClienteFisico extends Cliente {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String apellidos;

    public ClienteFisico(int idCliente, String identificacion, String nombre, String apellidos,
            String telefono, String correo, String direccion) {
        super(idCliente, identificacion, telefono, correo, direccion);
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    @Override
    public String getNombreParaFactura() {
        return nombre + " " + apellidos;
    }

    // La persona fisica no tiene descuento
    @Override
    public double getPorcentajeDescuento() {
        return 0;
    }

    @Override
    public String getTipo() {
        return "Fisico";
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
}
