package modelo;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idUsuario;
    private String nombreUsuario;
    private String contrasena; // se guarda el hash SHA-256, nunca el texto plano
    private String nombreCompleto;
    private Rol rol;
    private boolean activo;

    public Usuario(int idUsuario, String nombreUsuario, String contrasenaPlana,
            String nombreCompleto, Rol rol) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = hashear(contrasenaPlana);
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.activo = true;
    }

    // Compara el hash de la contrasena digitada con el hash guardado
    public boolean validarContrasena(String contrasenaPlana) {
        return contrasena.equals(hashear(contrasenaPlana));
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    // Convierte un texto a su hash SHA-256 en hexadecimal
    public static String hashear(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public void cambiarContrasena(String nuevaContrasena) {
        this.contrasena = hashear(nuevaContrasena);
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
