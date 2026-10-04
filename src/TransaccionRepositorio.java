public interface TransaccionRepositorio {
    void guardarTransaccion(String origen, String destino, double monto, double comision);
}
