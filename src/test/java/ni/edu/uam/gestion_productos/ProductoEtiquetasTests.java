package ni.edu.uam.gestion_productos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import ni.edu.uam.gestion_productos.services.ProductoService;
import java.util.UUID;
import ni.edu.uam.gestion_productos.models.Categoria;
import ni.edu.uam.gestion_productos.models.Etiqueta;
import ni.edu.uam.gestion_productos.models.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProductoEtiquetasTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProductoService productoService;

    @Test
    void productosCompartenEtiquetasYEliminarProductoConservaEtiquetas() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria de prueba");
        entityManager.persist(categoria);
        Etiqueta oferta = crearEtiqueta();
        Etiqueta nuevo = crearEtiqueta();
        Producto primero = crearProducto(categoria);
        Producto segundo = crearProducto(categoria);
        productoService.agregarEtiqueta(primero.getId(), oferta.getId());
        productoService.agregarEtiqueta(primero.getId(), nuevo.getId());
        productoService.agregarEtiqueta(primero.getId(), oferta.getId());
        productoService.agregarEtiqueta(segundo.getId(), oferta.getId());
        entityManager.flush();
        entityManager.clear();

        Producto guardado = entityManager.find(Producto.class, primero.getId());
        assertEquals(2, guardado.getEtiquetas().size());
        Producto compartido = entityManager.find(Producto.class, segundo.getId());
        assertEquals(1, compartido.getEtiquetas().size());
        assertEquals(oferta.getId(), compartido.getEtiquetas().iterator().next().getId());

        entityManager.remove(guardado);
        entityManager.flush();
        entityManager.clear();
        assertNotNull(entityManager.find(Etiqueta.class, oferta.getId()));
        assertNotNull(entityManager.find(Etiqueta.class, nuevo.getId()));
        assertEquals(1, entityManager.find(Producto.class, segundo.getId()).getEtiquetas().size());
    }

    private Etiqueta crearEtiqueta() {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre("Prueba-" + UUID.randomUUID());
        entityManager.persist(etiqueta);
        return etiqueta;
    }

    private Producto crearProducto(Categoria categoria) {
        Producto producto = new Producto();
        producto.setCodigo(UUID.randomUUID().toString().replace("-", "").substring(0, 30));
        producto.setNombre("Producto de prueba");
        producto.setCategoria(categoria);
        producto.setPrecioVenta(new BigDecimal("25.00"));
        entityManager.persist(producto);
        return producto;
    }
    @Test
    void eliminaSoloLaAsociacionYPermiteFiltrarPorEtiqueta() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria para etiquetas");
        entityManager.persist(categoria);
        Etiqueta etiqueta = crearEtiqueta();
        Producto asociado = crearProducto(categoria);
        Producto sinAsociar = crearProducto(categoria);
        productoService.agregarEtiqueta(asociado.getId(), etiqueta.getId());
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, productoService.listarPorEtiqueta(etiqueta.getId()).size());
        productoService.eliminarEtiqueta(asociado.getId(), etiqueta.getId());
        entityManager.flush();
        entityManager.clear();

        assertNotNull(entityManager.find(Producto.class, asociado.getId()));
        assertNotNull(entityManager.find(Etiqueta.class, etiqueta.getId()));
        assertTrue(entityManager.find(Producto.class, asociado.getId()).getEtiquetas().isEmpty());
        assertEquals(0, productoService.listarPorEtiqueta(etiqueta.getId()).size());
        assertNotNull(entityManager.find(Producto.class, sinAsociar.getId()));
    }
}
