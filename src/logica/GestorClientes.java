package logica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import modelo.Cliente;

public class GestorClientes implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private ArrayList<Cliente> clientes;

    public GestorClientes() {
        clientes = new ArrayList<>();
    }

    public void agregar(Cliente cliente) {
        validar(cliente);
        for (Cliente c : clientes) {
            if (c.getIdentificacion().equalsIgnoreCase(cliente.getIdentificacion())) {
                throw new IllegalArgumentException("Ya existe un cliente con esa identificacion");
            }
        }
        clientes.add(cliente);
    }

    // Reemplaza los datos del cliente que tenga la misma identificacion
    public void editar(Cliente cliente) throws ClienteNoEncontradoException {
        validar(cliente);
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getIdentificacion().equalsIgnoreCase(cliente.getIdentificacion())) {
                clientes.set(i, cliente);
                return;
            }
        }
        throw new ClienteNoEncontradoException("Cliente no encontrado");
    }

    // No se borra: se marca inactivo para conservar su historial de facturas
    public void desactivar(String identificacion) throws ClienteNoEncontradoException {
        buscarPorIdentificacion(identificacion).setActivo(false);
    }

    public Cliente buscarPorIdentificacion(String identificacion) throws ClienteNoEncontradoException {
        for (Cliente c : clientes) {
            if (c.getIdentificacion().equalsIgnoreCase(identificacion.trim())) {
                return c;
            }
        }
        throw new ClienteNoEncontradoException("Cliente no encontrado");
    }

    // Filtra clientes activos por identificacion o nombre (contiene el texto)
    public List<Cliente> buscar(String texto) {
        String t = texto.trim().toLowerCase();
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : clientes) {
            if (c.isActivo() && (c.getIdentificacion().toLowerCase().contains(t)
                    || c.getNombreParaFactura().toLowerCase().contains(t))) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Cliente> listarActivos() {
        return buscar("");
    }

    public int siguienteId() {
        int max = 0;
        for (Cliente c : clientes) {
            max = Math.max(max, c.getIdCliente());
        }
        return max + 1;
    }

    private void validar(Cliente c) {
        if (c.getIdentificacion() == null || c.getIdentificacion().isBlank()) {
            throw new IllegalArgumentException("La identificacion es obligatoria");
        }
        if (c.getNombreParaFactura() == null || c.getNombreParaFactura().isBlank()) {
            throw new IllegalArgumentException("El nombre o la razon social es obligatorio");
        }
        if (c.getCorreo() != null && !c.getCorreo().isBlank()
                && !PATRON_CORREO.matcher(c.getCorreo()).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato valido");
        }
    }
}
