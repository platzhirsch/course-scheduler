package berlin.reiche.scheduler;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import berlin.reiche.scheduler.model.Timeframe;
import berlin.reiche.scheduler.model.User;

/**
 * The timeframe servlet is dedicated to access the settings for configurating
 * the available time for scheduling courses.
 * 
 * @author Konrad Reiche
 * 
 */
@SuppressWarnings("serial")
public class TimeframeServlet extends HttpServlet {

    /**
     * File path to the web resources.
     */
    private static final String TIMEFRAME_SITE = "ftl/timeframe/form.ftl";

    /**
     * Singleton instance.
     */
    private static final TimeframeServlet INSTANCE = new TimeframeServlet();

    /**
     * The constructor is private in order to enforce the singleton pattern.
     */
    private TimeframeServlet() {

    }

    /**
     * Parses the HTTP request and writes the response by using the template
     * engine.
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        Map<String, Object> data = AppServlet.getDefaultData();
        Writer writer = response.getWriter();

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(AppServlet.LOGIN_ATTRIBUTE);
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        Timeframe timeframe = MongoDB.getAll(Timeframe.class).get(0);
        data.put("days", timeframe.getDays());
        data.put("timeSlots", timeframe.getTimeSlots());
        AppServlet.processTemplate(TIMEFRAME_SITE, data, writer);
    }

    /**
     * Parses all user HTML form requests and handles them.
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        
        int days = Integer.valueOf(request.getParameter("days"));
        int timeSlots = Integer.valueOf(request.getParameter("timeSlots"));
        
        Timeframe timeframe = MongoDB.getAll(Timeframe.class).get(0);
        timeframe.setDays(days);
        timeframe.setTimeSlots(timeSlots);
        MongoDB.store(timeframe);
        response.sendRedirect("/");
    }

    /**
     * @return a singleton instance of {@link RoomServlet}.
     */
    public static TimeframeServlet getInstance() {
        return INSTANCE;
    }

}
