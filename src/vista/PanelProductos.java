package vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import logica.Inventario;
import logica.ProductoNoEncontradoException;
import logica.Sistema;
import modelo.Producto;
import modelo.ProductoAbarrote;
import modelo.ProductoElectrodomestico;
import modelo.ProductoLimpieza;
import modelo.Usuario;

// HU-07, HU-08 y HU-09: registrar, editar y vigilar productos bajo minimo
public class PanelProductos extends JPanel {

    private static final String ABARROTE = "Abarrote";
    private static final String LIMPIEZA = "Limpieza";
    private static final String ELECTRO = "Electrodomestico";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Inventario inventario = Sistema.getInstancia().getInventario();
    private final boolean soloConsulta; // el cajero solo consulta

    // Lista y filtros
    private final JTextField txtBuscar = new JTextField();
    private final JCheckBox chkBajoMinimo = new JCheckBox("Solo bajo minimo");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Codigo", "Nombre", "Tipo", "Existencias"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private List<Producto> visibles = new ArrayList<>();

    // Formulario comun
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{ABARROTE, LIMPIEZA, ELECTRO});
    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtPrecio = new JTextField();
    private final JTextField txtExistencias = new JTextField();
    private final JTextField txtMinima = new JTextField();

    // Datos segun el tipo
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel panelTipo = new JPanel(tarjetas);
    private final JTextField txtVencimiento = new JTextField();
    private final JCheckBox chkCanasta = new JCheckBox("Canasta basica (IVA 1 %)");
    private final JTextField txtContenidoMl = new JTextField();
    private final JCheckBox chkToxico = new JCheckBox("Es toxico");
    private final JTextField txtMarca = new JTextField();
    private final JTextField txtModelo = new JTextField();
    private final JTextField txtGarantia = new JTextField();

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnGuardar = new JButton("Guardar");

    private Producto seleccionado; // null = se esta creando uno nuevo

    public PanelProductos(Usuario usuario) {
        this.soloConsulta = !usuario.esAdministrador();
        construir();
        refrescarTabla();
        limpiarFormulario();
    }

    private void construir() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Izquierda: busqueda + tabla
        JPanel filtros = new JPanel(new BorderLayout(8, 0));
        txtBuscar.setToolTipText("Buscar por codigo o nombre");
        filtros.add(new JLabel("Buscar por codigo o nombre:"), BorderLayout.WEST);
        filtros.add(txtBuscar, BorderLayout.CENTER);
        filtros.add(chkBajoMinimo, BorderLayout.EAST);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setDefaultRenderer(Object.class, new RenderBajoMinimo());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        JPanel izquierda = new JPanel(new BorderLayout(0, 8));
        izquierda.add(filtros, BorderLayout.NORTH);
        izquierda.add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Derecha: formulario
        JPanel formulario = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(formulario, 0, 0, "Tipo de producto", cmbTipo);
        UtilVista.agregarCampo(formulario, 1, 0, "Codigo", txtCodigo);
        UtilVista.agregarCampo(formulario, 2, 0, "Nombre", txtNombre);

        JPanel precioExist = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(precioExist, 0, 0, "Precio unitario (" + UtilVista.COLON.trim() + ")", txtPrecio);
        UtilVista.agregarCampo(precioExist, 0, 1, "Existencias", txtExistencias);
        UtilVista.agregarCampo(formulario, 3, 0, "", precioExist);
        UtilVista.agregarCampo(formulario, 4, 0, "Existencia minima", txtMinima);

        panelTipo.add(crearPanelAbarrote(), ABARROTE);
        panelTipo.add(crearPanelLimpieza(), LIMPIEZA);
        panelTipo.add(crearPanelElectro(), ELECTRO);
        JLabel lblTipo = new JLabel("Datos segun el tipo");
        lblTipo.setFont(lblTipo.getFont().deriveFont(Font.BOLD));
        UtilVista.agregarCampo(formulario, 5, 0, " ", lblTipo);
        UtilVista.agregarCampo(formulario, 6, 0, "", panelTipo);

        cmbTipo.addActionListener(e -> tarjetas.show(panelTipo, (String) cmbTipo.getSelectedItem()));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnNuevo);
        botones.add(btnEliminar);
        botones.add(btnGuardar);
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnEliminar.addActionListener(e -> eliminar());
        btnGuardar.addActionListener(e -> guardar());

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(formulario, BorderLayout.NORTH);
        derecha.add(botones, BorderLayout.SOUTH);
        derecha.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, new JScrollPane(derecha));
        division.setResizeWeight(0.55);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);

        UtilVista.alCambiarTexto(txtBuscar, this::refrescarTabla);
        chkBajoMinimo.addActionListener(e -> refrescarTabla());

        if (soloConsulta) {
            for (Component c : new Component[]{cmbTipo, txtCodigo, txtNombre, txtPrecio, txtExistencias,
                txtMinima, txtVencimiento, chkCanasta, txtContenidoMl, chkToxico, txtMarca, txtModelo,
                txtGarantia, btnNuevo, btnEliminar, btnGuardar}) {
                c.setEnabled(false);
            }
        }
    }

    private JPanel crearPanelAbarrote() {
        JPanel p = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(p, 0, 0, "Fecha de vencimiento (dd/mm/aaaa)", txtVencimiento);
        UtilVista.agregarCampo(p, 1, 0, "", chkCanasta);
        return p;
    }

    private JPanel crearPanelLimpieza() {
        JPanel p = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(p, 0, 0, "Contenido (ml)", txtContenidoMl);
        UtilVista.agregarCampo(p, 1, 0, "", chkToxico);
        return p;
    }

    private JPanel crearPanelElectro() {
        JPanel p = new JPanel(new GridBagLayout());
        UtilVista.agregarCampo(p, 0, 0, "Marca", txtMarca);
        UtilVista.agregarCampo(p, 0, 1, "Modelo", txtModelo);
        UtilVista.agregarCampo(p, 1, 0, "Garantia (meses)", txtGarantia);
        return p;
    }

    private void refrescarTabla() {
        visibles = inventario.buscar(txtBuscar.getText(), chkBajoMinimo.isSelected());
        modelo.setRowCount(0);
        for (Producto p : visibles) {
            String existencias = p.estaBajoMinimo()
                    ? p.getExistencias() + "  (bajo minimo)"
                    : String.valueOf(p.getExistencias());
            modelo.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getTipo(), existencias});
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= visibles.size()) {
            return;
        }
        seleccionado = visibles.get(fila);
        cmbTipo.setSelectedItem(seleccionado.getTipo());
        cmbTipo.setEnabled(false);   // el tipo no se cambia al editar
        txtCodigo.setEnabled(false); // el codigo es la llave
        txtCodigo.setText(seleccionado.getCodigo());
        txtNombre.setText(seleccionado.getNombre());
        txtPrecio.setText(String.valueOf(seleccionado.getPrecioUnitario()));
        txtExistencias.setText(String.valueOf(seleccionado.getExistencias()));
        txtMinima.setText(String.valueOf(seleccionado.getExistenciaMinima()));

        // instanceof: se cargan los campos propios de cada subclase
        if (seleccionado instanceof ProductoAbarrote a) {
            txtVencimiento.setText(a.getFechaVencimiento() == null ? "" : a.getFechaVencimiento().format(FORMATO_FECHA));
            chkCanasta.setSelected(a.isCanastaBasica());
        } else if (seleccionado instanceof ProductoLimpieza l) {
            txtContenidoMl.setText(String.valueOf(l.getContenidoMl()));
            chkToxico.setSelected(l.isEsToxico());
        } else if (seleccionado instanceof ProductoElectrodomestico el) {
            txtMarca.setText(el.getMarca());
            txtModelo.setText(el.getModelo());
            txtGarantia.setText(String.valueOf(el.getGarantiaMeses()));
        }
        if (soloConsulta) {
            cmbTipo.setEnabled(false);
        }
    }

    private void limpiarFormulario() {
        seleccionado = null;
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtCodigo, txtNombre, txtPrecio, txtExistencias, txtMinima,
            txtVencimiento, txtContenidoMl, txtMarca, txtModelo, txtGarantia}) {
            t.setText("");
        }
        chkCanasta.setSelected(false);
        chkToxico.setSelected(false);
        cmbTipo.setSelectedIndex(0);
        cmbTipo.setEnabled(!soloConsulta);
        txtCodigo.setEnabled(!soloConsulta);
    }

    // Arma el objeto de la subclase correcta segun el tipo elegido
    private Producto construirProducto() {
        String codigo = txtCodigo.getText().trim().toUpperCase();
        String nombre = txtNombre.getText().trim();
        double precio = UtilVista.leerDouble(txtPrecio, "precio");
        int existencias = UtilVista.leerEntero(txtExistencias, "existencias");
        int minima = UtilVista.leerEntero(txtMinima, "existencia minima");
        String descripcion = (seleccionado != null) ? seleccionado.getDescripcion() : "";

        switch ((String) cmbTipo.getSelectedItem()) {
            case ABARROTE:
                LocalDate vence = null;
                if (!txtVencimiento.getText().isBlank()) {
                    vence = LocalDate.parse(txtVencimiento.getText().trim(), FORMATO_FECHA);
                }
                return new ProductoAbarrote(codigo, nombre, descripcion, precio, existencias, minima,
                        vence, chkCanasta.isSelected());
            case LIMPIEZA:
                return new ProductoLimpieza(codigo, nombre, descripcion, precio, existencias, minima,
                        UtilVista.leerEntero(txtContenidoMl, "contenido"), chkToxico.isSelected());
            default:
                return new ProductoElectrodomestico(codigo, nombre, descripcion, precio, existencias, minima,
                        txtMarca.getText().trim(), txtModelo.getText().trim(),
                        UtilVista.leerEntero(txtGarantia, "garantia"));
        }
    }

    private void guardar() {
        try {
            Producto p = construirProducto();
            if (seleccionado == null) {
                inventario.agregar(p);
                UtilVista.mostrarInfo(this, "Producto " + p.getCodigo() + " registrado.");
            } else {
                inventario.editar(p);
                UtilVista.mostrarInfo(this, "Producto " + p.getCodigo() + " actualizado.");
            }
            refrescarTabla();
            limpiarFormulario();
            UtilVista.guardarEnSegundoPlano(this);
        } catch (DateTimeParseException ex) {
            UtilVista.mostrarError(this, "La fecha debe tener el formato dd/mm/aaaa");
        } catch (IllegalArgumentException ex) { // incluye NumberFormatException
            UtilVista.mostrarError(this, ex.getMessage());
        } catch (ProductoNoEncontradoException ex) {
            UtilVista.mostrarError(this, ex.getMessage());
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            UtilVista.mostrarError(this, "Seleccione un producto de la tabla");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Eliminar el producto " + seleccionado.getCodigo() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            inventario.eliminar(seleccionado.getCodigo());
            refrescarTabla();
            limpiarFormulario();
            UtilVista.guardarEnSegundoPlano(this);
        } catch (ProductoNoEncontradoException ex) {
            UtilVista.mostrarError(this, ex.getMessage());
        }
    }

    // HU-09: pinta de amarillo las filas de productos bajo su existencia minima
    private class RenderBajoMinimo extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, columna);
            boolean bajo = fila < visibles.size() && visibles.get(fila).estaBajoMinimo();
            if (!sel) {
                c.setBackground(bajo ? UtilVista.AMARILLO_BAJO_MINIMO : t.getBackground());
                c.setForeground(bajo && columna == 3 ? UtilVista.NARANJA_BAJO_MINIMO : t.getForeground());
            }
            return c;
        }
    }
}
