package logica;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import modelo.ClienteFisico;
import modelo.ClienteJuridico;
import modelo.Factura;
import modelo.ProductoAbarrote;
import modelo.ProductoElectrodomestico;
import modelo.ProductoLimpieza;

// Agrupa todos los gestores y se encarga de guardarlos y cargarlos (serializacion)
public class Sistema implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String ARCHIVO_DATOS = "fidefactura.dat";

    private static Sistema instancia;

    private final GestorUsuarios gestorUsuarios;
    private final GestorClientes gestorClientes;
    private final Inventario inventario;
    private final GestorFacturas gestorFacturas;

    private Sistema() {
        gestorUsuarios = new GestorUsuarios();
        gestorClientes = new GestorClientes();
        inventario = new Inventario();
        gestorFacturas = new GestorFacturas(inventario);
    }

    public static synchronized Sistema getInstancia() {
        if (instancia == null) {
            instancia = cargar();
        }
        return instancia;
    }

    // Lee el archivo de datos; si no existe o esta danado, arranca con datos de ejemplo
    private static Sistema cargar() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ARCHIVO_DATOS))) {
            Sistema s = (Sistema) ois.readObject();
            Factura.setConsecutivo(s.gestorFacturas.obtenerUltimoNumero());
            return s;
        } catch (IOException | ClassNotFoundException e) {
            Sistema s = new Sistema();
            s.cargarDatosIniciales();
            return s;
        }
    }

    // synchronized porque se puede llamar desde un hilo en segundo plano (SwingWorker)
    public synchronized void guardar() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            oos.writeObject(this);
        }
    }

    private void cargarDatosIniciales() {
        gestorUsuarios.cargarUsuariosIniciales();

        inventario.agregar(new ProductoAbarrote("AB-001", "Arroz 1 kg", "Arroz blanco 99 % grano entero",
                1250, 200, 50, LocalDate.now().plusMonths(8), true));
        inventario.agregar(new ProductoAbarrote("AB-002", "Galletas surtidas", "Paquete de 400 g",
                1800, 30, 40, LocalDate.now().plusMonths(4), false));
        inventario.agregar(new ProductoLimpieza("LI-014", "Detergente 3 L", "Detergente liquido",
                4900, 80, 20, 3000, false));
        inventario.agregar(new ProductoLimpieza("LI-020", "Cloro 1 L", "Cloro concentrado",
                950, 10, 25, 1000, true));
        inventario.agregar(new ProductoElectrodomestico("EL-007", "Licuadora", "Licuadora 10 velocidades",
                28000, 15, 5, "Oster", "BLSTPB", 12));

        gestorClientes.agregar(new ClienteFisico(1, "1-1111-1111", "Juan", "Perez Mora",
                "8888-1111", "juan.perez@correo.com", "San Jose"));
        gestorClientes.agregar(new ClienteJuridico(2, "3-101-123456", "Comercial Ejemplo S.A.",
                "Maria Solano", 5, "2222-3333", "compras@comercialejemplo.com", "Cartago"));
    }

    public GestorUsuarios getGestorUsuarios() { return gestorUsuarios; }
    public GestorClientes getGestorClientes() { return gestorClientes; }
    public Inventario getInventario() { return inventario; }
    public GestorFacturas getGestorFacturas() { return gestorFacturas; }
}
