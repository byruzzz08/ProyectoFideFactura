package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import logica.ClienteNoEncontradoException;
import logica.GestorClientes;
import logica.GestorFacturas;
import logica.Inventario;
import logica.ProductoNoEncontradoException;
import logica.Sistema;
import logica.StockInsuficienteException;
import modelo.Cliente;
import modelo.DetalleFactura;
import modelo.Factura;
import modelo.MetodoPago;
import modelo.Producto;
import modelo.Usuario;

// HU-10, HU-11 y HU-12: armar la factura, cobrar y exportar el archivo
public class PanelFacturacion extends JPanel {

    private static final String CARPETA_FACTURAS = "facturas";

    private final Usuario cajero;
    private final GestorClientes gestorClientes = Sistema.getInstancia().getGestorClientes();
    private final Inventario inventario = Sistema.getInstancia().getInventario();
    private final GestorFacturas gestorFacturas = Sistema.getInstancia().getGestorFacturas();

    private Factura factura;

    private final JTextField txtCliente = new JTextField(22);
    private final JLabel lblClienteNombre = new JLabel("Ningun cliente seleccionado");
    private final JLabel lblClienteInfo = new JLabel(" ");
    private final JTextField txtCodigo = new JTextField(14);
    private final JTextField txtCantidad = new JTextField("1", 5);
    private final JLabel lblError = UtilVista.etiquetaError();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Codigo", "Producto", "Cant.", "Precio", "IVA", "Total linea"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    private final JComboBox<MetodoPago> cmbMetodo = new JComboBox<>(MetodoPago.values());
    private final JTextField txtMonto = new JTextField(12);
    private final JLabel lblVuelto = new JLabel("Vuelto: " + UtilVista.COLON + "-");
    private final JLabel lblSubtotal = valor();
    private final JLabel lblDescuento = valor();
    private final JLabel lblIva = valor();
    private final JLabel lblTotal = valor();
    private final JButton btnConfirmar = new JButton("Confirmar y exportar");

    public PanelFacturacion(Usuario cajero) {
        this.cajero = cajero;
        this.factura = gestorFacturas.crearFactura(null, cajero);
        construir();
        refrescar();
    }

    private static JLabel valor() {
        JLabel l = new JLabel(UtilVista.COLON + "-", SwingConstants.RIGHT);
        return l;
    }

    private void construir() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(construirSuperior(), BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(320); // columna Producto mas ancha
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        add(construirInferior(), BorderLayout.SOUTH);
    }

    private JPanel construirSuperior() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarCliente());
        txtCliente.addActionListener(e -> buscarCliente()); // Enter tambien busca
        txtCliente.setToolTipText("Identificacion o nombre");

        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregarLinea());
        txtCodigo.addActionListener(e -> agregarLinea());
        txtCantidad.addActionListener(e -> agregarLinea());
        txtCodigo.setToolTipText("Codigo de producto");

        lblClienteNombre.setFont(lblClienteNombre.getFont().deriveFont(Font.BOLD));

        g.gridx = 0; g.gridy = 0; p.add(new JLabel("Cliente"), g);
        g.gridx = 1; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL; p.add(txtCliente, g);
        g.gridx = 4; g.gridwidth = 1; g.fill = GridBagConstraints.NONE; p.add(btnBuscar, g);
        JPanel info = new JPanel(new GridLayout(2, 1));
        info.add(lblClienteNombre);
        info.add(lblClienteInfo);
        g.gridx = 5; g.weightx = 1; p.add(info, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0; p.add(new JLabel("Codigo"), g);
        g.gridx = 1; p.add(txtCodigo, g);
        g.gridx = 2; p.add(new JLabel("Cant."), g);
        g.gridx = 3; p.add(txtCantidad, g);
        g.gridx = 4; p.add(btnAgregar, g);
        g.gridx = 5; g.weightx = 1; p.add(lblError, g);
        return p;
    }

    private JPanel construirInferior() {
        // Izquierda: metodo de pago y vuelto
        JPanel pago = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(pago, 0, 0, "Metodo de pago", cmbMetodo);
        UtilVista.agregarCampo(pago, 0, 1, "Monto recibido", txtMonto);
        UtilVista.agregarCampo(pago, 0, 2, " ", lblVuelto);
        cmbMetodo.addActionListener(e -> {
            boolean efectivo = cmbMetodo.getSelectedItem() == MetodoPago.EFECTIVO;
            txtMonto.setEnabled(efectivo);
            if (!efectivo) {
                txtMonto.setText("");
            }
            actualizarVuelto();
        });
        UtilVista.alCambiarTexto(txtMonto, this::actualizarVuelto);

        // Derecha: totales
        JPanel totales = new JPanel(new GridLayout(5, 2, 12, 2));
        totales.add(new JLabel("Subtotal")); totales.add(lblSubtotal);
        totales.add(new JLabel("Descuento")); totales.add(lblDescuento);
        totales.add(new JLabel("IVA")); totales.add(lblIva);
        totales.add(new JSeparator()); totales.add(new JSeparator());
        JLabel lblTxtTotal = new JLabel("Total");
        lblTxtTotal.setFont(lblTxtTotal.getFont().deriveFont(Font.BOLD, 16f));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
        totales.add(lblTxtTotal); totales.add(lblTotal);

        JPanel medio = new JPanel(new BorderLayout());
        medio.add(pago, BorderLayout.WEST);
        medio.add(totales, BorderLayout.EAST);

        // Botones
        JButton btnQuitar = new JButton("Quitar linea");
        JButton btnCancelar = new JButton("Cancelar");
        btnQuitar.addActionListener(e -> quitarLinea());
        btnCancelar.addActionListener(e -> cancelar());
        btnConfirmar.addActionListener(e -> confirmar());
        btnConfirmar.setFont(btnConfirmar.getFont().deriveFont(Font.BOLD));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT));
        izq.add(btnQuitar);
        izq.add(btnCancelar);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        der.add(btnConfirmar);
        JPanel botones = new JPanel(new BorderLayout());
        botones.add(izq, BorderLayout.WEST);
        botones.add(der, BorderLayout.EAST);

        JPanel inferior = new JPanel(new BorderLayout(0, 8));
        inferior.add(medio, BorderLayout.CENTER);
        inferior.add(botones, BorderLayout.SOUTH);
        return inferior;
    }

    // Busca por identificacion exacta; si no, por coincidencia en nombre
    private void buscarCliente() {
        String texto = txtCliente.getText().trim();
        if (texto.isEmpty()) {
            lblError.setText("Digite la identificacion o el nombre del cliente");
            return;
        }
        Cliente elegido = null;
        try {
            Cliente c = gestorClientes.buscarPorIdentificacion(texto);
            if (c.isActivo()) {
                elegido = c;
            }
        } catch (ClienteNoEncontradoException ex) {
            // se intenta por nombre abajo
        }
        if (elegido == null) {
            List<Cliente> encontrados = gestorClientes.buscar(texto); // solo activos (HU-06)
            if (encontrados.isEmpty()) {
                lblError.setText("Cliente no encontrado");
                return;
            }
            if (encontrados.size() == 1) {
                elegido = encontrados.get(0);
            } else {
                String[] opciones = new String[encontrados.size()];
                for (int i = 0; i < opciones.length; i++) {
                    opciones[i] = encontrados.get(i).getIdentificacion() + " - "
                            + encontrados.get(i).getNombreParaFactura();
                }
                Object r = JOptionPane.showInputDialog(this, "Varios clientes coinciden:", "Elegir cliente",
                        JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
                if (r == null) {
                    return;
                }
                for (int i = 0; i < opciones.length; i++) {
                    if (opciones[i].equals(r)) {
                        elegido = encontrados.get(i);
                    }
                }
            }
        }
        factura.setCliente(elegido);
        lblError.setText(" ");
        refrescar();
        txtCodigo.requestFocus();
    }

    private void agregarLinea() {
        lblError.setText(" ");
        try {
            String codigo = txtCodigo.getText().trim();
            if (codigo.isEmpty()) {
                lblError.setText("Digite el codigo del producto");
                return;
            }
            int cantidad = UtilVista.leerEntero(txtCantidad, "cantidad");
            if (cantidad <= 0) {
                lblError.setText("La cantidad debe ser mayor que 0");
                return;
            }
            Producto p = inventario.buscarPorCodigo(codigo);
            // Se suma lo que ya esta en la factura para no vender de mas
            inventario.verificarExistencias(p.getCodigo(), factura.cantidadDe(p.getCodigo()) + cantidad);
            factura.agregarDetalle(p, cantidad);
            txtCodigo.setText("");
            txtCantidad.setText("1");
            txtCodigo.requestFocus();
            refrescar();
        } catch (StockInsuficienteException ex) {
            lblError.setText("Existencias insuficientes para esa cantidad");
        } catch (ProductoNoEncontradoException ex) {
            lblError.setText(ex.getMessage());
        } catch (NumberFormatException ex) {
            lblError.setText(ex.getMessage());
        }
    }

    private void quitarLinea() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            lblError.setText("Seleccione la linea que desea quitar");
            return;
        }
        factura.quitarDetalle(fila);
        refrescar();
    }

    private void cancelar() {
        if (!factura.getDetalles().isEmpty()) {
            int r = JOptionPane.showConfirmDialog(this, "Descartar la factura actual?", "Cancelar",
                    JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) {
                return;
            }
        }
        reiniciar();
    }

    private void reiniciar() {
        factura = gestorFacturas.crearFactura(null, cajero);
        txtCliente.setText("");
        txtCodigo.setText("");
        txtCantidad.setText("1");
        txtMonto.setText("");
        cmbMetodo.setSelectedItem(MetodoPago.EFECTIVO);
        lblError.setText(" ");
        refrescar();
    }

    // Recalcula la tabla y los totales cada vez que cambia algo (HU-10)
    private void refrescar() {
        Cliente c = factura.getCliente();
        if (c == null) {
            lblClienteNombre.setText("Ningun cliente seleccionado");
            lblClienteInfo.setText(" ");
        } else {
            lblClienteNombre.setText(c.getNombreParaFactura());
            lblClienteInfo.setText("Cliente " + c.getTipo().toLowerCase() + " - descuento "
                    + Factura.formatearPorcentaje(c.getPorcentajeDescuento()) + " %");
        }

        modelo.setRowCount(0);
        for (DetalleFactura d : factura.getDetalles()) {
            modelo.addRow(new Object[]{
                d.getProducto().getCodigo(),
                d.getProducto().getNombre(),
                d.getCantidad(),
                Factura.formatearMonto(d.getPrecioUnitario()),
                Factura.formatearPorcentaje(d.getTasaImpuesto() * 100) + " %",
                Factura.formatearMonto(d.calcularTotalLinea())
            });
        }

        lblSubtotal.setText(UtilVista.COLON + Factura.formatearMonto(factura.calcularSubtotal()));
        lblDescuento.setText("-" + UtilVista.COLON + Factura.formatearMonto(factura.calcularDescuento()));
        lblIva.setText(UtilVista.COLON + Factura.formatearMonto(factura.calcularImpuestos()));
        lblTotal.setText(UtilVista.COLON + Factura.formatearMonto(factura.calcularTotal()));
        actualizarVuelto();
    }

    // HU-11: en efectivo se calcula el vuelto mientras se digita el monto
    private void actualizarVuelto() {
        if (cmbMetodo.getSelectedItem() != MetodoPago.EFECTIVO || txtMonto.getText().isBlank()) {
            lblVuelto.setText("Vuelto: " + UtilVista.COLON + "-");
            lblVuelto.setForeground(lblSubtotal.getForeground());
            return;
        }
        try {
            double vuelto = UtilVista.leerDouble(txtMonto, "monto") - factura.calcularTotal();
            lblVuelto.setText("Vuelto: " + UtilVista.COLON + Factura.formatearMonto(vuelto));
            lblVuelto.setForeground(vuelto < 0 ? UtilVista.ROJO_ERROR : lblSubtotal.getForeground());
        } catch (NumberFormatException ex) {
            lblVuelto.setText("Monto invalido");
            lblVuelto.setForeground(UtilVista.ROJO_ERROR);
        }
    }

    // HU-11 + HU-12: confirma la venta y exporta el .txt en segundo plano
    private void confirmar() {
        MetodoPago metodo = (MetodoPago) cmbMetodo.getSelectedItem();
        try {
            double monto = 0;
            if (metodo == MetodoPago.EFECTIVO) {
                if (txtMonto.getText().isBlank()) {
                    UtilVista.mostrarError(this, "Digite el monto recibido");
                    return;
                }
                monto = UtilVista.leerDouble(txtMonto, "monto recibido");
            }
            gestorFacturas.confirmar(factura, metodo, monto);
        } catch (StockInsuficienteException | ProductoNoEncontradoException | IllegalArgumentException ex) {
            UtilVista.mostrarError(this, ex.getMessage());
            return;
        }

        Factura emitida = factura;
        btnConfirmar.setEnabled(false);
        btnConfirmar.setText("Exportando...");

        // Hilo en segundo plano: escribe el archivo y guarda los datos sin congelar la pantalla
        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                File carpeta = new File(CARPETA_FACTURAS);
                if (!carpeta.exists()) {
                    carpeta.mkdirs();
                }
                File archivo = new File(carpeta, emitida.getNombreArchivo());
                emitida.exportar(archivo.getPath());
                Sistema.getInstancia().guardar();
                return archivo;
            }

            @Override
            protected void done() {
                btnConfirmar.setEnabled(true);
                btnConfirmar.setText("Confirmar y exportar");
                try {
                    File archivo = get();
                    String vuelto = (emitida.getMetodoPago() == MetodoPago.EFECTIVO)
                            ? "\nVuelto: " + UtilVista.COLON + Factura.formatearMonto(emitida.calcularVuelto())
                            : "";
                    UtilVista.mostrarInfo(PanelFacturacion.this, String.format(
                            "Factura N.o %06d emitida.%s%nArchivo: %s",
                            emitida.getNumero(), vuelto, archivo.getAbsolutePath()));
                } catch (InterruptedException | ExecutionException ex) {
                    UtilVista.mostrarError(PanelFacturacion.this,
                            "La factura se emitio pero no se pudo exportar: " + ex.getMessage());
                }
                reiniciar();
            }
        }.execute();
    }
}
