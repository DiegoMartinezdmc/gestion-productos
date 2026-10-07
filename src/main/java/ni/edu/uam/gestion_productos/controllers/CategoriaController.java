package ni.edu.uam.gestion_productos.controllers;

import java.util.List;
import ni.edu.uam.gestion_productos.models.Categoria;
import ni.edu.uam.gestion_productos.repositories.CategoriaRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository repository;

    public CategoriaController(CategoriaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Categoria> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Categoria guardar(@RequestBody Categoria categoria) {
        return repository.save(categoria);
    }
} 


