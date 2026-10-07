package ni.edu.uam.gestion_productos;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import ni.edu.uam.gestion_productos.models.Proveedor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import ni.edu.uam.gestion_productos.dto.ProductoRequestDTO;
import ni.edu.uam.gestion_productos.models.Categoria;
import org.springframework.http.MediaType;
import java.util.List;
import ni.edu.uam.gestion_productos.repositories.EtiquetaRepository;
import ni.edu.uam.gestion_productos.repositories.CategoriaRepository;
import java.util.Optional;
import ni.edu.uam.gestion_productos.controllers.ProductoController;
import ni.edu.uam.gestion_productos.models.Producto;
import ni.edu.uam.gestion_productos.repositories.ProductoRepository;
import ni.edu.uam.gestion_productos.services.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProductoControllerTests {

    private ProductoRepository repository;
    private MockMvc mvc;
    private CategoriaRepository categoriaRepository;

    @BeforeEach
    void configurar() {
        repository = mock(ProductoRepository.class);
        categoriaRepository = mock(CategoriaRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(
                new ProductoController(new ProductoService(repository, categoriaRepository, mock(EtiquetaRepository.class)))).build();
    }

    @Test
    void listarProductos() throws Exception {
        Producto producto = new Producto();
        producto.setId(1);
        producto.setNombre("Teclado");
        when(repository.findAll()).thenReturn(List.of(producto));

        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Teclado"));
        verify(repository).findAll();
    }

    @Test
    void buscarProductoExistente() throws Exception {
        Producto producto = new Producto();
        producto.setId(7);
        when(repository.findById(7)).thenReturn(Optional.of(producto));

        mvc.perform(get("/api/productos/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
        verify(repository).findById(7);
    }

    @Test
    void buscarProductoInexistenteDevuelve404() throws Exception {
        when(repository.findById(999)).thenReturn(Optional.empty());

        mvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound());
    }
    @Test
    void guardarProductoDesdeDto() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setId(2);
        categoria.setNombre("Accesorios");
        when(categoriaRepository.findById(2)).thenReturn(Optional.of(categoria));
        when(repository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setId(10);
            return producto;
        });

        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("""
                {"codigo": "TEC-001", "nombre": "Teclado", "precioVenta": 25.50,
                 "existencia": 5, "categoriaId": 2}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.codigo").value("TEC-001"))
                .andExpect(jsonPath("$.nombre").value("Teclado"))
                .andExpect(jsonPath("$.precioVenta").value(25.50))
                .andExpect(jsonPath("$.existencia").value(5))
                .andExpect(jsonPath("$.categoria.id").value(2))
                .andExpect(jsonPath("$.categoria.nombre").value("Accesorios"));
        verify(categoriaRepository).findById(2);
        verify(repository).save(any(Producto.class));
    }

    @Test
    void noGuardaSiLaCategoriaNoExiste() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setCategoriaId(999);
        when(categoriaRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> new ProductoService(repository, categoriaRepository, mock(EtiquetaRepository.class)).guardar(dto));
        assertEquals("Categoría no encontrada", error.getMessage());
        verifyNoInteractions(repository);
    }
    @Test
    void actualizarProductoConservaIdYProveedor() throws Exception {
        Producto producto = new Producto();
        producto.setId(7);
        producto.setNombre("Nombre anterior");
        Proveedor proveedor = new Proveedor();
        proveedor.setId(3);
        producto.setProveedor(proveedor);
        Categoria categoria = new Categoria();
        categoria.setId(2);
        when(repository.findById(7)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(2)).thenReturn(Optional.of(categoria));
        when(repository.save(producto)).thenReturn(producto);

        mvc.perform(put("/api/productos/7").contentType(MediaType.APPLICATION_JSON).content("""
                {"codigo": "TEC-002", "nombre": "Teclado actualizado", "precioVenta": 35.50,
                 "existencia": 12, "categoriaId": 2}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.codigo").value("TEC-002"))
                .andExpect(jsonPath("$.nombre").value("Teclado actualizado"))
                .andExpect(jsonPath("$.precioVenta").value(35.50))
                .andExpect(jsonPath("$.existencia").value(12))
                .andExpect(jsonPath("$.categoria.id").value(2))
                .andExpect(jsonPath("$.proveedor.id").value(3));
        verify(repository).save(producto);
    }

    @Test
    void actualizarProductoInexistenteDevuelve404() throws Exception {
        when(repository.findById(999)).thenReturn(Optional.empty());

        mvc.perform(put("/api/productos/999").contentType(MediaType.APPLICATION_JSON).content("""
                {"codigo": "TEC-002", "nombre": "Teclado", "precioVenta": 35.50,
                 "existencia": 12, "categoriaId": 2}
                """))
                .andExpect(status().isNotFound());
        verify(repository, never()).save(any(Producto.class));
        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void actualizarConCategoriaInexistenteNoModificaProducto() {
        Producto producto = new Producto();
        producto.setId(7);
        producto.setNombre("Nombre anterior");
        when(repository.findById(7)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(999)).thenReturn(Optional.empty());
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setCategoriaId(999);
        dto.setNombre("Nombre nuevo");

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> new ProductoService(repository, categoriaRepository, mock(EtiquetaRepository.class)).actualizar(7, dto));
        assertEquals("Categoría no encontrada", error.getMessage());
        assertEquals("Nombre anterior", producto.getNombre());
        verify(repository, never()).save(any(Producto.class));
    }
    @Test
    void eliminarProductoDevuelve204SinContenido() throws Exception {
        mvc.perform(delete("/api/productos/7"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(repository).deleteById(7);
    }
}

