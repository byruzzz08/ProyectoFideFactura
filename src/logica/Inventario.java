package logica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import modelo.Producto;

public class Inventario implements Serializable {

    private static final long serialVersionUID = 1L;

    // Clave = codigo del producto, para buscar rapido
    private HashMap<String, Producto> productos;

    public Inventario() {
        productos = new HashMap<>();
    }

    public void agregar(Producto p) {
        validar(p);
        String clave = p.getCodigo().toUpperCase();
        if (productos.containsKey(clave)) {
            throw new IllegalArgumentException("Ya existe un producto con el codigo " + p.getCodigo());
        }
        productos.put(clave, p);
    }

    public void editar(Producto p) throws ProductoNoEncontradoException {
        validar(p);
        String clave = p.getCodigo().toUpperCase();
        if (!productos.containsKey(clave)) {
            throw new ProductoNoEncontradoException("Producto no encontrado: " + p.getCodigo());
        }
        productos.put(clave, p);
    }

    public void eliminar(String codigo) throws ProductoNoEncontradoException {
        if (productos.remove(codigo.trim().toUpperCase()) == null) {
            throw new ProductoNoEncontradoException("Producto no encontrado: " + codigo);
        }
    }

    public Producto buscarPorCodigo(String codigo) throws ProductoNoEncontradoException {
        Producto p = productos.get(codigo.trim().toUpperCase());
        if (p == null) {
            throw new ProductoNoEncontradoException("Producto no encontrado: " + codigo);
        }
        return p;
    }

    // Revisa si hay suficientes unidades sin rebajar nada
    public void verificarExistencias(String codigo, int cantidad)
            throws ProductoNoEncontradoException, StockInsuficienteException {
        Producto p = buscarPorCodigo(codigo);
        if (cantidad > p.getExistencias()) {
            throw new StockInsuficienteException("Existencias insuficientes para esa cantidad (disponibles: "
                    + p.getExistencias() + ")");
        }
    }

    // synchronized: preparado para cuando varias cajas facturen sobre el mismo inventario
    public synchronized void rebajar(String codigo, int cantidad)
            throws ProductoNoEncontradoException, StockInsuficienteException {
        verificarExistencias(codigo, cantidad);
        buscarPorCodigo(codigo).rebajarExistencias(cantidad);
    }

    public synchronized void aumentar(String codigo, int cantidad) throws ProductoNoEncontradoException {
        buscarPorCodigo(codigo).aumentarExistencias(cantidad);
    }

    public List<Producto> buscar(String texto, boolean soloBajoMinimo) {
        String t = texto.trim().toLowerCase();
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos.values()) {
            boolean coincide = p.getCodigo().toLowerCase().contains(t)
                    || p.getNombre().toLowerCase().contains(t);
            if (coincide && (!soloBajoMinimo || p.estaBajoMinimo())) {
                resultado.add(p);
            }
        }
        resultado.sort(Comparator.comparing(Producto::getCodigo));
        return resultado;
    }

    public List<Producto> listarTodos() {
        return buscar("", false);
    }

    public List<Producto> listarBajoMinimo() {
        return buscar("", true);
    }

    private void validar(Producto p) {
        if (p.getCodigo() == null || p.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio");
        }
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (p.getPrecioUnitario() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        }
        if (p.getExistencias() < 0 || p.getExistenciaMinima() < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas");
        }
    }
}
