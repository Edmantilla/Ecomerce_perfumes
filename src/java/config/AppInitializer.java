package config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import persistencias.JpaProvider;

/**
 * AppInitializer — Listener del ciclo de vida de la aplicación web.
 *
 * Implementa ServletContextListener para ejecutar código en dos momentos clave:
 *   1. Cuando el servidor arranca (contextInitialized)
 *   2. Cuando el servidor se apaga (contextDestroyed)
 *
 * @WebListener registra esta clase automáticamente como listener,
 * sin necesidad de declaración en web.xml.
 */
@WebListener
public class AppInitializer implements ServletContextListener {

    /**
     * Se ejecuta automáticamente cuando GlassFish/Tomcat arranca la aplicación.
     *
     * Fuerza la creación del EntityManagerFactory (singleton de JpaProvider)
     * ANTES de que llegue cualquier petición HTTP. Esto tiene dos beneficios:
     *   - El primer usuario no sufre el retraso de inicialización de Hibernate (2-5 segundos).
     *   - Si hay un error de conexión a MySQL (credenciales incorrectas, BD apagada),
     *     el error aparece en los logs del servidor al arrancar, no en mitad de una petición.
     *
     * Internamente, este método hace que Hibernate lea persistence.xml ("ProyectoPU"),
     * configure el pool de conexiones y verifique la conexión con la BD Perfumeria_andreylpz.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JpaProvider.getEntityManagerFactory(); // abre el pool de conexiones a MySQL al arrancar
    }

    /**
     * Se ejecuta automáticamente cuando el servidor se apaga o la app se despliega de nuevo.
     *
     * Cierra limpiamente el EntityManagerFactory, lo que a su vez:
     *   - Libera todas las conexiones del pool de vuelta a MySQL.
     *   - Sin este cierre, las conexiones quedarían abiertas y MySQL podría
     *     quedarse sin conexiones disponibles en reinicios frecuentes del servidor.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaProvider.getEntityManagerFactory().close(); // cierra el pool de conexiones al apagar
    }
}
