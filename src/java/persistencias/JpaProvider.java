package persistencias;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class JpaProvider {

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("ProyectoPU");

    private JpaProvider() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }
}
