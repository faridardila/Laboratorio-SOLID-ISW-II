import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Armado del sistema (Composition Root / Inyección de dependencias)
        ValidadorTransaccion validador = new ValidadorMontoTope();
        TransaccionRepositorio repositorio = new OracleRepositorio();
        ComprobanteService comprobante = new ComprobanteBancoAndino();
        NotificacionService notificador = new SmsNotificacionService();
        AuditoriaService auditoria = new AuditoriaCompuestaService(
                new ConsolaAuditoriaService(),
                new AntifraudeAuditoriaService()
        );

        TransaccionService servicio = new TransaccionService(
                validador,
                repositorio,
                comprobante,
                notificador,
                auditoria
        );

        CuentaOperativa ana = new CuentaAhorros ("001-1", "Ana", 2_000_000);
        CuentaOperativa luis = new CuentaAhorros ("001-2", "Luis", 500_000);
        Cuenta cdtAna = new CDT ("CDT-9", "Ana", 10_000_000, LocalDate.now().plusMonths(6));

        servicio.transferir (ana, luis, 150_000, new TransferenciaOtroBanco());

        PagoServiciosService pagoServicios = new PagoServiciosService(servicio);
        pagoServicios.pagar(ana, new FacturaServicioPublico("FAC-LUZ-778812", TipoServicioPublico.LUZ), 184_300);
        new CobroCuotaManejo(notificador).cobrarMensual (List.of (ana, luis));
        List<Extractable> productos = List.of(new TarjetaCredito(3_000_000), new CreditoVivienda (120_000_000));

        new GeneradorExtractos().imprimirExtractos(productos);
    }
}
