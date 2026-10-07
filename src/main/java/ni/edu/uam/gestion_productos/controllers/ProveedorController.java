package ni.edu.uam.gestion_productos.controllers;

import java.util.List;
import ni.edu.uam.gestion_productos.models.Producto;
import ni.edu.uam.gestion_productos.models.Proveedor;
import ni.edu.uam.gestion_productos.repositories.ProductoRepository;
import ni.edu.uam.gestion_productos.repositories.ProveedorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorRepository repository;
    private final ProductoRepository productoRepository;

    public ProveedorController(ProveedorRepository repository, ProductoRepository productoRepository) {
        this.repository = repository;
        this.productoRepository = productoRepository;
    }

    @GetMapping
    public List<Proveedor> listar() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Proveedor guardar(@RequestBody Proveedor proveedor) {
        if (proveedor.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No indique id al crear un proveedor");
        }
        if (proveedor.getNombre() == null || proveedor.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        for (Producto producto : proveedor.getProductos()) {
            if (producto == null || producto.getId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Los productos deben ser nuevos y no incluir id");
            }
            producto.setProveedor(proveedor);
        }
        return repository.save(proveedor);
    }

    @PutMapping("/{proveedorId}/productos/{productoId}")
    @Transactional
    public Producto asociarProducto(@PathVariable Integer proveedorId, @PathVariable Integer productoId) {
        Proveedor proveedor = repository.findById(proveedorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        producto.setProveedor(proveedor);
        return productoRepository.save(producto);
    }
}

