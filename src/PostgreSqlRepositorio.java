public class PostgreSqlRepositorio implements TransaccionRepositorio {
    @Override
    public void guardarTransaccion(String origen, String destino, double monto, double comision) {
        System.out.println("[POSTGRES] Conectando a jdbc:postgresql://prod-db:5432/BANCO ...");
        System.out.println("[POSTGRES] INSERT INTO transacciones VALUES ('" + origen + "', '" + destino + "', " + monto + ", " + comision + ")");
    }
}
