public class SmsGateway {
    public void enviar (String destinatario, String mensaje) {
        System.out.println("[SMS] Conectando al proveedor de mensajeria...");
        System.out.println("[SMS] Para " + destinatario + ": " + mensaje);
    }
}
