package modelo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Factura implements Exportable, Serializable {

    private static final long serialVersionUID = 1L;

    // Consecutivo compartido por todas las facturas (static no se serializa, se restaura al cargar)
    private static int consecutivo = 0;

    private static final String LINEA_DOBLE = "==========================================================";
    private static final String LINEA_SIMPLE = "----------------------------------------------------------";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int numero;
    private LocalDateTime fecha;
    private Cliente cliente;
    private Usuario cajero;
    private ArrayList<DetalleFactura> detalles;
    private MetodoPago metodoPago;
    private EstadoFactura estado;
    private double montoRecibido;

    public Factura(Cliente cliente, Usuario cajero) {
        this.cliente = cliente;
        this.cajero = cajero;
        this.detalles = new ArrayList<>();
        this.metodoPago = MetodoPago.EFECTIVO;
        this.numero = 0;      // 0 = borrador, recibe numero al confirmar
        this.estado = null;   // null = todavia no se ha emitido
    }

    // Si el producto ya esta en la factura, suma la cantidad a esa misma linea
    public void agregarDetalle(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0");
        }
        for (DetalleFactura d : detalles) {
            if (d.getProducto().getCodigo().equals(producto.getCodigo())) {
                d.aumentarCantidad(cantidad);
                return;
            }
        }
        detalles.add(new DetalleFactura(producto, cantidad));
    }

    public void quitarDetalle(int indice) {
        if (indice < 0 || indice >= detalles.size()) {
            throw new IllegalArgumentException("Linea invalida");
        }
        detalles.remove(indice);
    }

    // Cantidad de un producto que ya esta en la factura (para validar existencias)
    public int cantidadDe(String codigo) {
        for (DetalleFactura d : detalles) {
            if (d.getProducto().getCodigo().equals(codigo)) {
                return d.getCantidad();
            }
        }
        return 0;
    }

    public double calcularSubtotal() {
        double total = 0;
        for (DetalleFactura d : detalles) {
            total += d.calcularSubtotal();
        }
        return total;
    }

    public double calcularDescuento() {
        if (cliente == null) {
            return 0;
        }
        return calcularSubtotal() * cliente.getPorcentajeDescuento() / 100.0;
    }

    public double calcularImpuestos() {
        double pct = (cliente == null) ? 0 : cliente.getPorcentajeDescuento();
        double total = 0;
        for (DetalleFactura d : detalles) {
            total += d.calcularImpuesto(pct);
        }
        return total;
    }

    public double calcularTotal() {
        return calcularSubtotal() - calcularDescuento() + calcularImpuestos();
    }

    public double calcularVuelto() {
        return montoRecibido - calcularTotal();
    }

    // Se llama al confirmar: asigna el consecutivo, la fecha y el estado
    public void emitir() {
        consecutivo++;
        this.numero = consecutivo;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoFactura.EMITIDA;
    }

    public void anular() {
        this.estado = EstadoFactura.ANULADA;
    }

    public String getNombreArchivo() {
        return String.format("Factura_%06d.txt", numero);
    }

    // Genera la factura fisica en un archivo .txt
    @Override
    public void exportar(String ruta) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta, StandardCharsets.UTF_8))) {
            bw.write(LINEA_DOBLE); bw.newLine();
            bw.write("                    FIDECOMPRO S.A."); bw.newLine();
            bw.write("            Venta al por mayor - FideFactura"); bw.newLine();
            bw.write(LINEA_DOBLE); bw.newLine();
            bw.write(String.format("Factura N.o: %06d                  Fecha: %s", numero, fecha.format(FORMATO_FECHA)));
            bw.newLine();
            bw.write(String.format("Cliente:     %s (%s)", cliente.getNombreParaFactura(),
                    cliente.getTipo().toLowerCase()));
            bw.newLine();
            bw.write("Ident.:      " + cliente.getIdentificacion()); bw.newLine();
            bw.write("Cajero:      " + cajero.getNombreUsuario()); bw.newLine();
            bw.write(LINEA_SIMPLE); bw.newLine();
            bw.write(String.format("%-8s %-20s %5s %10s %11s", "Codigo", "Producto", "Cant.", "Precio", "Total"));
            bw.newLine();
            for (DetalleFactura d : detalles) {
                String nombre = d.getProducto().getNombre();
                if (d.getTasaImpuesto() == 0.01) {
                    nombre += " (CB)";
                }
                if (nombre.length() > 20) {
                    nombre = nombre.substring(0, 20);
                }
                bw.write(String.format("%-8s %-20s %5d %10s %11s", d.getProducto().getCodigo(), nombre,
                        d.getCantidad(), formatearMonto(d.getPrecioUnitario()),
                        formatearMonto(d.calcularTotalLinea())));
                bw.newLine();
            }
            bw.write(LINEA_SIMPLE); bw.newLine();
            bw.write(String.format("%-44s %13s", "Subtotal", formatearMonto(calcularSubtotal())));
            bw.newLine();
            String etiquetaDesc = String.format("Descuento (%s %%)", formatearPorcentaje(cliente.getPorcentajeDescuento()));
            bw.write(String.format("%-44s %13s", etiquetaDesc, "-" + formatearMonto(calcularDescuento())));
            bw.newLine();
            bw.write(String.format("%-44s %13s", "IVA (1 % CB / 13 % general)", formatearMonto(calcularImpuestos())));
            bw.newLine();
            bw.write(LINEA_SIMPLE); bw.newLine();
            bw.write(String.format("%-40s %17s", "TOTAL", "CRC " + formatearMonto(calcularTotal())));
            bw.newLine();
            bw.write("Metodo de pago: " + metodoPago); bw.newLine();
            if (metodoPago == MetodoPago.EFECTIVO) {
                bw.write(String.format("%-44s %13s", "Monto recibido", formatearMonto(montoRecibido)));
                bw.newLine();
                bw.write(String.format("%-44s %13s", "Vuelto", formatearMonto(calcularVuelto())));
                bw.newLine();
            }
            if (estado == EstadoFactura.ANULADA) {
                bw.write("*** FACTURA ANULADA ***"); bw.newLine();
            }
            bw.write(LINEA_DOBLE); bw.newLine();
            bw.write("                 Gracias por su compra!"); bw.newLine();
        }
    }

    // Formato de colones: 12 500,00
    public static String formatearMonto(double monto) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator(' ');
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.00", simbolos).format(monto);
    }

    public static String formatearPorcentaje(double pct) {
        if (pct == Math.floor(pct)) {
            return String.valueOf((int) pct);
        }
        return String.valueOf(pct);
    }

    // Se usa al cargar los datos guardados para que el consecutivo siga donde iba
    public static void setConsecutivo(int valor) {
        consecutivo = valor;
    }

    public static int getConsecutivo() {
        return consecutivo;
    }

    public int getNumero() { return numero; }
    public LocalDateTime getFecha() { return fecha; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Usuario getCajero() { return cajero; }
    public List<DetalleFactura> getDetalles() { return detalles; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public EstadoFactura getEstado() { return estado; }
    public double getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(double montoRecibido) { this.montoRecibido = montoRecibido; }
}
