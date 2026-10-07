package ni.edu.uam.gestion_productos.repositories;

import java.util.List;
import ni.edu.uam.gestion_productos.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByCategoriaId(Integer categoriaId);

    List<Producto> findByEtiquetasId(Integer etiquetaId);
}


