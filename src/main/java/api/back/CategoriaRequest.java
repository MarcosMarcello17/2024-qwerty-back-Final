package api.back;

public class CategoriaRequest {
    private String nombre;
    private String iconPath;

    public CategoriaRequest(String nombre, String iconPath) {
        this.nombre = nombre;
        this.iconPath = iconPath;
    }
    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}
