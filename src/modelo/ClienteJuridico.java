package modelo;

public class ClienteJuridico extends Cliente {

    private static final long serialVersionUID = 1L;

    private String razonSocial;
    private String nombreContacto;
    private double descuentoMayorista; // porcentaje, por ejemplo 5 = 5 %

    public ClienteJuridico(int idCliente, String identificacion, String razonSocial,
            String nombreContacto, double descuentoMayorista,
            String telefono, String correo, String direccion) {
        super(idCliente, identificacion, telefono, correo, direccion);
        this.razonSocial = razonSocial;
        this.nombreContacto = nombreContacto;
        this.descuentoMayorista = descuentoMayorista;
    }

    @Override
    public String getNombreParaFactura() {
        return razonSocial;
    }

    // La persona juridica recibe su descuento mayorista
    @Override
    public double getPorcentajeDescuento() {
        return descuentoMayorista;
    }

    @Override
    public String getTipo() {
        return "Juridico";
    }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }
    public double getDescuentoMayorista() { return descuentoMayorista; }
    public void setDescuentoMayorista(double descuentoMayorista) { this.descuentoMayorista = descuentoMayorista; }
}
