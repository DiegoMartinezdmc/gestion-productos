package ni.edu.uam.gestion_productos;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ni.edu.uam.gestion_productos.controllers.CategoriaController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
class CategoriaControllerMappingTests {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void registraLasRutasDeCategorias() {
        for (RequestMethod method : new RequestMethod[] {RequestMethod.GET, RequestMethod.POST}) {
            assertTrue(handlerMapping.getHandlerMethods().entrySet().stream()
                    .anyMatch(entry -> entry.getValue().getBeanType().equals(CategoriaController.class)
                            && entry.getKey().getPatternValues().contains("/api/categorias")
                            && entry.getKey().getMethodsCondition().getMethods().contains(method)),
                    "No se registro " + method + " /api/categorias");
        }
    }
}
