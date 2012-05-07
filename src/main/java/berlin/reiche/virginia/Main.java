package berlin.reiche.virginia;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import berlin.reiche.virginia.model.Timeframe;
import berlin.reiche.virginia.model.User;

/**
 * @author Konrad Reiche
 * 
 */
public class Main {

    /**
     * The default port for the web server.
     */
    private static int port = 8080;

    /**
     * Path to the server property file.
     */
    private static final String SERVER_PROPERTIES_PATH = "site/resources/server.properties";

    /**
     * Path to the scheduler property file.
     */
    private static final String SCHEDULER_PROPERTIES_PATH = "site/resources/scheduler.properties";

    /**
     * The Jetty HTTP Servlet Server.
     */
    static Server server;

    /**
     * Launches the web server.
     * 
     * @throws IOException
     *             if there is a problem with the server properties file.
     */
    public static void main(String... args) throws IOException {

        checkServerProperties();
        checkSchedulerProperties();
        server = new Server(port);

        try {
            ServletContextHandler context = new ServletContextHandler(
                    ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            context.addServlet(new ServletHolder(AppServlet.getInstance()),
                    "/*");
            context.addServlet(new ServletHolder(UserServlet.getInstance()),
                    "/users/*");
            context.addServlet(new ServletHolder(ModuleServlet.getInstance()),
                    "/modules/*");
            context.addServlet(new ServletHolder(RoomServlet.getInstance()),
                    "/rooms/*");
            context.addServlet(
                    new ServletHolder(TimeframeServlet.getInstance()),
                    "/timeframe/*");
            context.addServlet(
                    new ServletHolder(SchedulerServlet.getInstance()),
                    "/scheduler/*");

            server.start();

        } catch (Exception e) {
            System.err.println("Server failed to start.");
            e.printStackTrace();
        }
    }

    /**
     * Applies the server configuration as defined in a file.
     * 
     * @throws IOException
     *             if an error occurred with the server property file.
     */
    private static void checkServerProperties() throws IOException {

        Properties serverProperties = new Properties();
        FileInputStream input = new FileInputStream(SERVER_PROPERTIES_PATH);
        serverProperties.load(input);
        port = Integer.valueOf(serverProperties.getProperty("server.port"));
        String adminUsername = serverProperties.getProperty("admin.username");
        String adminPassword = serverProperties.getProperty("admin.password");

        User admin = MongoDB.get(User.class, adminUsername);
        if (admin == null) {
            admin = new User(adminUsername, adminPassword, "", false, false);
            MongoDB.store(admin);
            System.out.println("Created default admin user.");
        }
        
        if (!admin.checkPassword(adminPassword)) {
            MongoDB.delete(User.class, adminUsername);
            admin = new User(adminUsername, adminPassword, "", false, false);
            MongoDB.store(admin);
            System.out.println("Assigned a new admin password.");
        }
    }

    /**
     * Applies the scheduler configuration as defined in a file.
     * 
     * @throws IOException
     *             if an error occurred with the scheduler property file.
     */
    private static void checkSchedulerProperties() throws IOException {

        if (MongoDB.getAll(Timeframe.class).isEmpty()) {

            Properties properties = new Properties();
            FileInputStream input = new FileInputStream(
                    SCHEDULER_PROPERTIES_PATH);
            properties.load(input);

            int days = Integer
                    .valueOf(properties.getProperty("timeframe.days"));
            int timeSlots = Integer.valueOf(properties
                    .getProperty("timeframe.timeSlots"));
            int startHour = Integer.valueOf(properties
                    .getProperty("timeframe.startHour"));
            String[] weekdays = properties.getProperty("timeframe.weekdays")
                    .replace(" ", "").split(",");

            Timeframe timeframe = new Timeframe(days, timeSlots, startHour,
                    Arrays.asList(weekdays));
            MongoDB.store(timeframe);
            System.out.println("Created default timeframe.");
        }

    }
}
