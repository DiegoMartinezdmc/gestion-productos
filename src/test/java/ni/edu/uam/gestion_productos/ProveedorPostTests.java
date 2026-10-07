package ni.edu.uam.gestion_productos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.util.UUID;
import ni.edu.uam.gestion_productos.models.Categoria;
import ni.edu.uam.gestion_productos.models.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Transactional
class ProveedorPostTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

    @Test
    void postCreaProveedorConDosProductosAsociados() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria para POST");
        entityManager.persist(categoria);
        String prefijo = UUID.randomUUID().toString().substring(0, 20);
        String body = """
                {
                  "nombre": "Proveedor con productos",
                  "activo": true,
                  "productos": [
                    {"codigo": "%s-A", "nombre": "Teclado", "categoria": {"id": %d},
                     "precioVenta": 25.00, "existencia": 20},
                    {"codigo": "%s-B", "nombre": "Mouse", "categoria": {"id": %d},
                     "precioVenta": 15.00, "existencia": 30}
                  ]
                }
                """.formatted(prefijo, categoria.getId(), prefijo, categoria.getId());
        String response = MockMvcBuilders.webAppContextSetup(context).build()
                .perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        assertFalse(response.contains("\"productos\""));
        entityManager.flush();
        entityManager.clear();
        var productos = entityManager.createQuery(
                "select p from Producto p where p.codigo in (:a, :b)", Producto.class)
                .setParameter("a", prefijo + "-A").setParameter("b", prefijo + "-B").getResultList();
        assertEquals(2, productos.size());
        assertEquals(productos.get(0).getProveedor().getId(), productos.get(1).getProveedor().getId());
        assertEquals("Proveedor con productos", productos.get(0).getProveedor().getNombre());
    }

    @Test
    void postRechazaProductosConId() throws Exception {
        MockMvcBuilders.webAppContextSetup(context).build()
                .perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Proveedor", "productos": [{"id": 1}]}
                                """))
                .andExpect(status().isBadRequest());
    }
}
