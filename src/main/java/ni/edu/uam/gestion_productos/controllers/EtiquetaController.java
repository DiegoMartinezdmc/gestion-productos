package ni.edu.uam.gestion_productos.controllers;

import ni.edu.uam.gestion_productos.models.Etiqueta;
import ni.edu.uam.gestion_productos.repositories.EtiquetaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/etiquetas")
public class EtiquetaController {

    private final EtiquetaRepository etiquetaRepository;

    public EtiquetaController(EtiquetaRepository etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Etiqueta guardar(@RequestBody Etiqueta etiqueta) {
        if (etiqueta.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No indique id al crear una etiqueta");
        }
        if (etiqueta.getNombre() == null || etiqueta.getNombre().isBlank()
                || etiqueta.getNombre().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio y admite hasta 100 caracteres");
        }
        return etiquetaRepository.save(etiqueta);
    }
}
