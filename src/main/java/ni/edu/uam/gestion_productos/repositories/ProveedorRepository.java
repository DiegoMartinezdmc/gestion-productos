package ni.edu.uam.gestion_productos.repositories;

import ni.edu.uam.gestion_productos.models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
}
