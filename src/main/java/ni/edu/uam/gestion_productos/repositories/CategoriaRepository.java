package ni.edu.uam.gestion_productos.repositories;

import ni.edu.uam.gestion_productos.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
