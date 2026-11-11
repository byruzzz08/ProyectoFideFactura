package vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import logica.ClienteNoEncontradoException;
import logica.GestorClientes;
import logica.Sistema;
import modelo.Cliente;
import modelo.ClienteFisico;
import modelo.ClienteJuridico;
import modelo.Factura;
import modelo.Usuario;

// HU-04, HU-05 y HU-06: registrar, buscar, editar y desactivar clientes
public class PanelClientes extends JPanel {

    private static final String FISICO = "Fisico";
    private static final String JURIDICO = "Juridico";

    private final GestorClientes gestor = Sistema.getInstancia().getGestorClientes();
    private final boolean esAdmin;

    private final JTextField txtBuscar = new JTextField();
    private final JLabel lblSinResultados = UtilVista.etiquetaError();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Identificacion", "Nombre", "Tipo", "Telefono", "Descuento"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private List<Cliente> visibles = new ArrayList<>();

    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{FISICO, JURIDICO});
    private final JTextField txtIdentificacion = new JTextField();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel panelTipo = new JPanel(tarjetas);
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtApellidos = new JTextField();
    private final JTextField txtRazonSocial = new JTextField();
    private final JTextField txtContacto = new JTextField();
    private final JTextField txtDescuento = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtDireccion = new JTextField();

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnDesactivar = new JButton("Desactivar");
    private final JButton btnGuardar = new JButton("Guardar");

    private Cliente seleccionado; // null = cliente nuevo

    public PanelClientes(Usuario usuario) {
        this.esAdmin = usuario.esAdministrador();
        construir();
        refrescarTabla();
        limpiarFormulario();
    }

    private void construir() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new BorderLayout(8, 4));
        filtros.add(new JLabel("Buscar por identificacion o nombre:"), BorderLayout.WEST);
        filtros.add(txtBuscar, BorderLayout.CENTER);
        filtros.add(lblSinResultados, BorderLayout.SOUTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(220); // columna Nombre mas ancha
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        JPanel izquierda = new JPanel(new BorderLayout(0, 8));
        izquierda.add(filtros, BorderLayout.NORTH);
        izquierda.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(formulario, 0, 0, "Tipo de cliente", cmbTipo);
        UtilVista.agregarCampo(formulario, 1, 0, "Identificacion", txtIdentificacion);

        JPanel pFisico = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(pFisico, 0, 0, "Nombre", txtNombre);
        UtilVista.agregarCampo(pFisico, 1, 0, "Apellidos", txtApellidos);
        JPanel pJuridico = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(pJuridico, 0, 0, "Razon social", txtRazonSocial);
        UtilVista.agregarCampo(pJuridico, 1, 0, "Nombre del contacto", txtContacto);
        UtilVista.agregarCampo(pJuridico, 2, 0, "Descuento mayorista (%)", txtDescuento);
        panelTipo.add(pFisico, FISICO);
        panelTipo.add(pJuridico, JURIDICO);

        JLabel lblTipo = new JLabel("Datos segun el tipo");
        lblTipo.setFont(lblTipo.getFont().deriveFont(Font.BOLD));
        UtilVista.agregarCampo(formulario, 2, 0, " ", lblTipo);
        UtilVista.agregarCampo(formulario, 3, 0, "", panelTipo);
        UtilVista.agregarCampo(formulario, 4, 0, "Telefono", txtTelefono);
        UtilVista.agregarCampo(formulario, 5, 0, "Correo", txtCorreo);
        UtilVista.agregarCampo(formulario, 6, 0, "Direccion", txtDireccion);

        cmbTipo.addActionListener(e -> tarjetas.show(panelTipo, (String) cmbTipo.getSelectedItem()));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnNuevo);
        botones.add(btnDesactivar);
        botones.add(btnGuardar);
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnDesactivar.addActionListener(e -> desactivar());
        btnGuardar.addActionListener(e -> guardar());
        btnDesactivar.setEnabled(esAdmin); // HU-06: solo el administrador desactiva

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(formulario, BorderLayout.NORTH);
        derecha.add(botones, BorderLayout.SOUTH);
        derecha.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, new JScrollPane(derecha));
        division.setResizeWeight(0.55);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);

        // HU-05: la tabla se filtra mientras se escribe
        UtilVista.alCambiarTexto(txtBuscar, this::refrescarTabla);
    }

    private void refrescarTabla() {
        visibles = gestor.buscar(txtBuscar.getText());
        modelo.setRowCount(0);
        for (Cliente c : visibles) {
            modelo.addRow(new Object[]{c.getIdentificacion(), c.getNombreParaFactura(), c.getTipo(),
                c.getTelefono(), Factura.formatearPorcentaje(c.getPorcentajeDescuento()) + " %"});
        }
        boolean sinResultados = visibles.isEmpty() && !txtBuscar.getText().isBlank();
        lblSinResultados.setText(sinResultados ? "Cliente no encontrado" : " ");
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= visibles.size()) {
            return;
        }
        seleccionado = visibles.get(fila);
        cmbTipo.setSelectedItem(seleccionado.getTipo());
        cmbTipo.setEnabled(false);
        txtIdentificacion.setText(seleccionado.getIdentificacion());
        txtIdentificacion.setEnabled(false);
        txtTelefono.setText(seleccionado.getTelefono());
        txtCorreo.setText(seleccionado.getCorreo());
        txtDireccion.setText(seleccionado.getDireccion());

        if (seleccionado instanceof ClienteFisico f) {
            txtNombre.setText(f.getNombre());
            txtApellidos.setText(f.getApellidos());
        } else if (seleccionado instanceof ClienteJuridico j) {
            txtRazonSocial.setText(j.getRazonSocial());
            txtContacto.setText(j.getNombreContacto());
            txtDescuento.setText(Factura.formatearPorcentaje(j.getDescuentoMayorista()));
        }
    }

    private void limpiarFormulario() {
        seleccionado = null;
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtIdentificacion, txtNombre, txtApellidos, txtRazonSocial,
            txtContacto, txtDescuento, txtTelefono, txtCorreo, txtDireccion}) {
            t.setText("");
        }
        cmbTipo.setSelectedIndex(0);
        cmbTipo.setEnabled(true);
        txtIdentificacion.setEnabled(true);
    }

    private Cliente construirCliente() {
        int id = (seleccionado != null) ? seleccionado.getIdCliente() : gestor.siguienteId();
        String identificacion = txtIdentificacion.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String direccion = txtDireccion.getText().trim();

        if (FISICO.equals(cmbTipo.getSelectedItem())) {
            return new ClienteFisico(id, identificacion, txtNombre.getText().trim(),
                    txtApellidos.getText().trim(), telefono, correo, direccion);
        }
        double descuento = txtDescuento.getText().isBlank() ? 0 : UtilVista.leerDouble(txtDescuento, "descuento");
        if (descuento < 0 || descuento > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        return new ClienteJuridico(id, identificacion, txtRazonSocial.getText().trim(),
                txtContacto.getText().trim(), descuento, telefono, correo, direccion);
    }

    private void guardar() {
        try {
            Cliente c = construirCliente();
            if (seleccionado == null) {
                gestor.agregar(c);
                UtilVista.mostrarInfo(this, "Cliente registrado.");
            } else {
                gestor.editar(c);
                UtilVista.mostrarInfo(this, "Cliente actualizado.");
            }
            refrescarTabla();
            limpiarFormulario();
            UtilVista.guardarEnSegundoPlano(this);
        } catch (IllegalArgumentException | ClienteNoEncontradoException ex) {
            UtilVista.mostrarError(this, ex.getMessage());
        }
    }

    private void desactivar() {
        if (seleccionado == null) {
            UtilVista.mostrarError(this, "Seleccione un cliente de la tabla");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Desactivar a " + seleccionado.getNombreParaFactura() + "?\nSus facturas anteriores se conservan.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            gestor.desactivar(seleccionado.getIdentificacion());
            refrescarTabla();
            limpiarFormulario();
            UtilVista.guardarEnSegundoPlano(this);
        } catch (ClienteNoEncontradoException ex) {
            UtilVista.mostrarError(this, ex.getMessage());
        }
    }
}
