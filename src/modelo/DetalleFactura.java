package modelo;

import java.io.Serializable;

// Linea de una factura. Guarda precio y tasa del momento de la venta (composicion)
public class DetalleFactura implements Serializable {

    private static final long serialVersionUID = 1L;

    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private double tasaImpuesto;

    public DetalleFactura(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioUnitario();
        this.tasaImpuesto = producto.getTasaImpuesto(); // polimorfismo: cada tipo da su tasa
    }

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }

    // El IVA se calcula sobre el subtotal ya rebajado con el descuento del cliente
    public double calcularImpuesto(double porcentajeDescuento) {
        double base = calcularSubtotal() * (1 - porcentajeDescuento / 100.0);
        return base * tasaImpuesto;
    }

    public double calcularTotalLinea() {
        return calcularSubtotal();
    }

    public void aumentarCantidad(int extra) {
        this.cantidad += extra;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getTasaImpuesto() { return tasaImpuesto; }
}
