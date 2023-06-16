public class CuentaUsuario {
    private String nombre;
    private String hashClave;
    private double saldo;

    public CuentaUsuario(String nombre, String hashClave, double saldo) {
        this.nombre = nombre;
        this.hashClave = hashClave;
        this.saldo = saldo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getHashClave() {
        return hashClave;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}
