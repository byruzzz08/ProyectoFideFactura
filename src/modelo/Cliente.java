package modelo;

import java.io.Serializable;

// Clase abstracta: cada tipo de cliente define su nombre y su descuento (polimorfismo)
public abstract class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idCliente;
    private String identificacion;
    private String telefono;
    private String correo;
    private String direccion;
    private boolean activo;

    public Cliente(int idCliente, String identificacion, String telefono,
            String correo, String direccion) {
        this.idCliente = idCliente;
        this.identificacion = identificacion;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.activo = true;
    }

    public abstract String getNombreParaFactura();

    // Porcentaje de descuento (0 a 100) que se aplica antes del IVA
    public abstract double getPorcentajeDescuento();

    public abstract String getTipo();

    public int getIdCliente() { return idCliente; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
