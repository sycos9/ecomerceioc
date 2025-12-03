package randomecommerce.randomecommerce.domain;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Carrito implements Serializable {

    private final Map<Long, CarritoItem> items = new LinkedHashMap<>();

    /**
     * Afegeix un producte al carro controlant el stock.
     * @param producto Producte a afegir
     * @param cantidad Quantitat que es vol afegir
     * @return true si s'ha afegit tota la quantitat, false si no hi havia suficient stock
     */
    public boolean agregarProducto(Producto producto, int cantidad) {
        if (producto == null || producto.getId() == null || cantidad <= 0) {
            return false;
        }

        CarritoItem existente = items.get(producto.getId());
        int cantidadActualEnCarrito = existente != null ? existente.getCantidad() : 0;

        int cantidadMaxima = producto.getStock() - cantidadActualEnCarrito;
        if (cantidadMaxima <= 0) {
            // No queda stock disponible
            return false;
        }

        int cantidadFinal = Math.min(cantidad, cantidadMaxima);

        if (existente == null) {
            items.put(
                producto.getId(),
                new CarritoItem(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    producto.getImagen(),
                    cantidadFinal
                )
            );
        } else {
            existente.incrementarCantidad(cantidadFinal);
        }

        // Retorna true si s'ha afegit tota la quantitat sol·licitada
        return cantidadFinal == cantidad;
    }

    public Collection<CarritoItem> getItems() {
        return items.values();
    }

    public double getTotal() {
        return items.values().stream()
                .mapToDouble(CarritoItem::getSubtotal)
                .sum();
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public void vaciar() {
        items.clear();
    }

    public void eliminarProducto(Long productoId) {
        items.remove(productoId);
    }

    public int getCantidadEnCarrito(Long productoId) {
        CarritoItem item = items.get(productoId);
        return item != null ? item.getCantidad() : 0;
    }

    public static class CarritoItem implements Serializable {
        private final Long productoId;
        private final String nombre;
        private final double precioUnitario;
        private final String imagen;
        private int cantidad;

        public CarritoItem(Long productoId, String nombre, double precioUnitario, String imagen, int cantidad) {
            this.productoId = productoId;
            this.nombre = nombre;
            this.precioUnitario = precioUnitario;
            this.imagen = imagen;
            this.cantidad = cantidad;
        }

        public Long getProductoId() { return productoId; }
        public String getNombre() { return nombre; }
        public double getPrecioUnitario() { return precioUnitario; }
        public String getImagen() { return imagen; }
        public int getCantidad() { return cantidad; }

        public double getSubtotal() {
            return precioUnitario * cantidad;
        }

        public void incrementarCantidad(int cantidadExtra) {
            if (cantidadExtra > 0) {
                this.cantidad += cantidadExtra;
            }
        }
    }
}
