package logica;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import modelo.Rol;
import modelo.Usuario;

public class GestorUsuarios implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Usuario> usuarios;
    private transient Usuario usuarioActual; // la sesion no se guarda en disco

    public GestorUsuarios() {
        usuarios = new ArrayList<>();
    }

    // Usuarios iniciales para poder entrar la primera vez
    public void cargarUsuariosIniciales() {
        if (usuarios.isEmpty()) {
            usuarios.add(new Usuario(1, "admin", "admin123", "Byron Ruiz", Rol.ADMINISTRADOR));
            usuarios.add(new Usuario(2, "cajero", "cajero123", "Cajero de prueba", Rol.CAJERO));
        }
    }

    public Usuario iniciarSesion(String nombreUsuario, String contrasena)
            throws CredencialesInvalidasException {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equalsIgnoreCase(nombreUsuario.trim())
                    && u.validarContrasena(contrasena)
                    && u.isActivo()) {
                usuarioActual = u;
                return u;
            }
        }
        // Mismo mensaje para todo: no se revela cual dato esta mal
        throw new CredencialesInvalidasException("Usuario o contrasena incorrectos");
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }

    public void agregarUsuario(Usuario nuevo) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equalsIgnoreCase(nuevo.getNombreUsuario())) {
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            }
        }
        usuarios.add(nuevo);
    }

    public int siguienteId() {
        int max = 0;
        for (Usuario u : usuarios) {
            max = Math.max(max, u.getIdUsuario());
        }
        return max + 1;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios);
    }
}
