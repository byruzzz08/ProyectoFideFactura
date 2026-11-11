package logica;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.DetalleFactura;
import modelo.EstadoFactura;
import modelo.Factura;
import modelo.MetodoPago;
import modelo.Usuario;

public class GestorFacturas implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Factura> facturas;
    private Inventario inventario;

    public GestorFacturas(Inventario inventario) {
        this.facturas = new ArrayList<>();
        this.inventario = inventario;
    }

    public Factura crearFactura(Cliente cliente, Usuario cajero) {
        return new Factura(cliente, cajero);
    }

    // Valida, rebaja existencias, asigna numero y guarda la factura
    public void confirmar(Factura f, MetodoPago metodo, double montoRecibido)
            throws StockInsuficienteException, ProductoNoEncontradoException {
        if (f.getCliente() == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente");
        }
        if (f.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La factura no tiene lineas");
        }
        f.setMetodoPago(metodo);
        if (metodo == MetodoPago.EFECTIVO) {
            if (montoRecibido < f.calcularTotal()) {
                throw new IllegalArgumentException("El monto recibido no cubre el total");
            }
            f.setMontoRecibido(montoRecibido);
        } else {
            f.setMontoRecibido(f.calcularTotal());
        }
        // Primero se revisa todo, asi no queda una venta a medias
        for (DetalleFactura d : f.getDetalles()) {
            inventario.verificarExistencias(d.getProducto().getCodigo(), d.getCantidad());
        }
        for (DetalleFactura d : f.getDetalles()) {
            inventario.rebajar(d.getProducto().getCodigo(), d.getCantidad());
        }
        f.emitir();
        facturas.add(f);
    }

    // Solo la usa el administrador: devuelve las existencias al inventario
    public void anular(int numero) throws ProductoNoEncontradoException {
        Factura f = buscarPorNumero(numero);
        if (f == null) {
            throw new IllegalArgumentException("No existe la factura " + numero);
        }
        if (f.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalArgumentException("La factura ya esta anulada");
        }
        for (DetalleFactura d : f.getDetalles()) {
            inventario.aumentar(d.getProducto().getCodigo(), d.getCantidad());
        }
        f.anular();
    }

    public Factura buscarPorNumero(int numero) {
        for (Factura f : facturas) {
            if (f.getNumero() == numero) {
                return f;
            }
        }
        return null;
    }

    public List<Factura> listarPorFecha(LocalDate fecha) {
        List<Factura> resultado = new ArrayList<>();
        for (Factura f : facturas) {
            if (f.getFecha().toLocalDate().equals(fecha)) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public List<Factura> listarTodas() {
        return new ArrayList<>(facturas);
    }

    // Numero mas alto guardado, para restaurar el consecutivo al cargar
    public int obtenerUltimoNumero() {
        int max = 0;
        for (Factura f : facturas) {
            max = Math.max(max, f.getNumero());
        }
        return max;
    }
}
