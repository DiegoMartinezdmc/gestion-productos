package ni.edu.uam.gestion_productos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import ni.edu.uam.gestion_productos.models.Categoria;
import ni.edu.uam.gestion_productos.models.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class ProductosPorCategoriaTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WebApplicationContext context;

    @Test
    void filtraPorCategoriaSinReferenciasCirculares() throws Exception {
        Categoria categoria = crearCategoria("Accesorios de prueba");
        Categoria otra = crearCategoria("Otra categoria de prueba");
        crearProducto(categoria);
        crearProducto(categoria);
        crearProducto(otra);
        Integer id = categoria.getId();
        entityManager.flush();
        entityManager.clear();

        Categoria guardada = entityManager.find(Categoria.class, id);
        assertEquals(2, guardada.getProductos().size());
        MockMvcBuilders.webAppContextSetup(context).build()
                .perform(get("/api/productos/categoria/{categoriaId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoria.id").value(id))
                .andExpect(jsonPath("$[1].categoria.id").value(id))
                .andExpect(jsonPath("$[0].categoria.productos").doesNotExist());
    }

    @Test
    void categoriaSinProductosDevuelveListaVacia() throws Exception {
        Categoria categoria = crearCategoria("Categoria vacia de prueba");
        entityManager.flush();
        MockMvcBuilders.webAppContextSetup(context).build()
                .perform(get("/api/productos/categoria/{categoriaId}", categoria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Categoria crearCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        entityManager.persist(categoria);
        return categoria;
    }

    private void crearProducto(Categoria categoria) {
        Producto producto = new Producto();
        producto.setCodigo(UUID.randomUUID().toString().replace("-", "").substring(0, 30));
        producto.setNombre("Producto de prueba");
        producto.setPrecioVenta(new BigDecimal("25.00"));
        producto.setCategoria(categoria);
        entityManager.persist(producto);
    }
}
