public class NotificacionService {
    private final SmsGateway sms = new SmsGateway();

    public void notificarTransferencia(Cuenta origen, Cuenta destino, double monto) {
        sms.enviar(origen.getTitular(), "Transferiste $" + monto + " a la cuenta " + destino.getNumero());
    }

    public void notificarCobroCuota(String numeroCuenta) {
        System.out.println("Cuota de manejo cobrada a " + numeroCuenta);
    }
}
