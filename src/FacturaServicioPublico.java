public class FacturaServicioPublico extends Cuenta {
    private final TipoServicioPublico servicio;

    public FacturaServicioPublico(String referencia, TipoServicioPublico servicio) {
        super(referencia, servicio.name(), 0);
        this.servicio = servicio;
    }

    public String getReferencia() {
        return numero;
    }

    public TipoServicioPublico getServicio() {
        return servicio;
    }

    @Override
    public String generarExtracto() {
        return "Factura " + servicio + " " + numero + " pagado: $" + saldo;
    }
}
