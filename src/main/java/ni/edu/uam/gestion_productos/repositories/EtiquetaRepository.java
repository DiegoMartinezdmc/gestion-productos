package ni.edu.uam.gestion_productos.repositories;

import ni.edu.uam.gestion_productos.models.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Integer> {
}
