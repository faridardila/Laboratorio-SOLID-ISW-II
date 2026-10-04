import java.util.List;

public class GeneradorExtractos {
    public void imprimirExtracto(Extractable producto) {
        System.out.println(producto.generarExtracto());
    }

    public void imprimirExtractos(List<? extends Extractable> productos) {
        for (Extractable p : productos) {
            imprimirExtracto(p);
        }
    }
}
