package persistencias;

import javax.persistence.EntityManagerFactory;  // fábrica que crea EntityManagers para operaciones JPA
import javax.persistence.Persistence;           // clase estática de JPA que crea EntityManagerFactory

/**
 * JpaProvider — Singleton que provee una única instancia de EntityManagerFactory.
 * 
 * Patrón Singleton: garantiza que solo exista UNA fábrica de conexiones a la BD
 * durante toda la vida de la aplicación. Esto es eficiente porque crear un EMF
 * es costoso (lee persistence.xml, conecta a MySQL, valida entidades).
 * 
 * Todos los JpaControllers y servlets obtienen su EMF desde aquí.
 * "ProyectoPU" es el nombre de la unidad de persistencia definida en persistence.xml.
 * La clase es final (no se puede heredar) y el constructor es privado (no se puede instanciar).
 */
public final class JpaProvider {

    // Se crea UNA SOLA VEZ al cargar la clase (static final)
    // Lee persistence.xml, conecta a MySQL y prepara el pool de conexiones
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("ProyectoPU");

    // Constructor privado: impide crear instancias de esta clase (patrón Singleton)
    private JpaProvider() {
    }

    // Método estático que retorna la fábrica de EntityManagers
    // Usado por todos los JpaControllers: new ProductoJpaController(JpaProvider.getEntityManagerFactory())
    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }
}
