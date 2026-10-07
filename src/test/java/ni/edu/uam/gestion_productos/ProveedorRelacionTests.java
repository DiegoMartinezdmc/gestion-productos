package ni.edu.uam.gestion_productos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import ni.edu.uam.gestion_productos.models.Categoria;
import ni.edu.uam.gestion_productos.models.Producto;
import ni.edu.uam.gestion_productos.models.Proveedor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProveedorRelacionTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void unProveedorTieneVariosProductosYElProveedorEsOpcional() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria de prueba");
        entityManager.persist(categoria);

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Proveedor de prueba");
        proveedor.setTelefono("+505 2222-3333");
        proveedor.setCorreo("proveedor@example.com");
        entityManager.persist(proveedor);

        crearProducto(categoria, proveedor);
        crearProducto(categoria, proveedor);
        Producto sinProveedor = crearProducto(categoria, null);
        Integer proveedorId = proveedor.getId();
        Integer productoId = sinProveedor.getId();
        entityManager.flush();
        entityManager.clear();

        Proveedor guardado = entityManager.find(Proveedor.class, proveedorId);
        assertTrue(guardado.isActivo());
        assertEquals("+505 2222-3333", guardado.getTelefono());
        assertEquals("proveedor@example.com", guardado.getCorreo());
        assertEquals(2, guardado.getProductos().size());
        assertTrue(guardado.getProductos().stream()
                .allMatch(producto -> proveedorId.equals(producto.getProveedor().getId())));
        assertNull(entityManager.find(Producto.class, productoId).getProveedor());
    }

    private Producto crearProducto(Categoria categoria, Proveedor proveedor) {
        Producto producto = new Producto();
        producto.setCodigo(UUID.randomUUID().toString().replace("-", "").substring(0, 30));
        producto.setNombre("Producto de prueba");
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setPrecioVenta(new BigDecimal("25.00"));
        entityManager.persist(producto);
        return producto;
    }
}
