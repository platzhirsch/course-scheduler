package berlin.reiche.virginia;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;

import berlin.reiche.virginia.model.Course;
import berlin.reiche.virginia.model.CourseModule;
import berlin.reiche.virginia.model.User;
import berlin.reiche.virginia.scheduler.CourseSchedule;

/**
 * The main servlet of the application which handles all incoming HTTP requests.
 * 
 * @author Konrad Reiche
 * 
 */
@SuppressWarnings("serial")
public class ModuleServlet extends HttpServlet {

    /**
     * File path to the web resources.
     */
    private static final String LIST_SITE = "ftl/modules/list.ftl";
    private static final String FORM_SITE = "ftl/modules/form.ftl";
    private static final String COURSES_SITE = "ftl/modules/course-list.ftl";
    private static final String RESPONSIBLITIES_SITE = "ftl/modules/responsibilities.ftl";
    
    /**
     * Further constants.
     */
    private static final String SELECTED_USER = "user";

    /**
     * Singleton instance.
     */
    private static final ModuleServlet INSTANCE = new ModuleServlet();

    public final static String root = "/modules";

    /**
     * The constructor is private in order to enforce the singleton pattern.
     */
    private ModuleServlet() {

    }

    /**
     * Parses the HTTP request and writes the response by using the template
     * engine.
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String path = request.getPathInfo();

        Map<String, Object> data = AppServlet.getDefaultData();
        Writer writer = response.getWriter();
        AppServlet.checkAccessRights(request, response, root
                + ((path == null) ? "" : path));

        if (path == null) {
            showCourseModules(response);
        } else if (path.equals("/")) {
            response.sendRedirect("/modules");
        } else if (path.matches("/" + AppServlet.ID_REGEX)) {
            ObjectId moduleId = new ObjectId(path.substring("/".length()));
            showCourses(response, moduleId);
        } else if (path.equals("/new")) {
            data.put(AppServlet.REQUEST_HEADLINE_VAR, "New Course Module");
            data.put("blankCourse", true);
            AppServlet.processTemplate(FORM_SITE, data, writer);
        } else if (path.matches("/delete/" + AppServlet.ID_REGEX)) {
            ObjectId id = new ObjectId(path.substring("/delete/".length()));
            MongoDB.delete(CourseModule.class, id);
            MongoDB.delete(CourseSchedule.class);
            response.sendRedirect("/modules");
        } else if (path.matches("/edit/" + AppServlet.ID_REGEX)) {
            ObjectId id = new ObjectId(path.substring("/edit/".length()));
            CourseModule module = MongoDB.get(CourseModule.class, id);
            handleModuleModification(request, response, module);
        } else if (path.equals("/responsibilities")) {
            List<CourseModule> modules = MongoDB.getAll(CourseModule.class);
            List<User> lecturers = MongoDB.createQuery(User.class)
                    .filter("lecturer =", true).asList();
            
            String selectedUser = request.getParameter(SELECTED_USER);
            if (selectedUser == null) {
                selectedUser = AppServlet.getCurrentUser(request).getName();
            }
            User user = MongoDB.get(User.class, selectedUser);
            data.put("user", user);
            data.put("modules", modules);
            data.put("lecturers", lecturers);
            AppServlet.processTemplate(RESPONSIBLITIES_SITE, data, writer);
        } else {
            AppServlet.processTemplate(AppServlet.NOT_FOUND_SITE, data, writer);
        }
    }

    /**
     * Parses all user HTML form requests and handles them.
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String path = request.getServletPath() + request.getPathInfo();

        if ("/modules/new".equals(path)) {
            handleModuleForm(request, response, null);
        } else if (path.matches("/modules/edit/" + AppServlet.ID_REGEX)) {
            ObjectId id = new ObjectId(
                    path.substring("/modules/edit/".length()));
            CourseModule module = MongoDB.get(CourseModule.class, id);
            handleModuleForm(request, response, module);
        } else if (path.equals("/modules/responsibilities")) {
            
                String[] ids = request.getParameterValues("responsibility");   
                String selectedUser = request.getParameter(SELECTED_USER);
                if (selectedUser == null) {
                    selectedUser = AppServlet.getCurrentUser(request).getName();
                }
                User user = MongoDB.get(User.class, selectedUser);
                user.getResponsibleCourses().clear();

                if (ids != null) {
                    for (String id : ids) {
                        Course course = MongoDB.get(Course.class, new ObjectId(
                                id));
                        user.addCourse(course);
                    }
                }
                MongoDB.store(user);
                response.sendRedirect(path + "?user=" + selectedUser);
        }
    }

    /**
     * Retrieves all course modules and displays them.
     * 
     * @param response
     *            provides HTTP-specific functionality in sending a response.
     * @throws IOException
     *             if an input or output exception occurs.
     */
    private void showCourseModules(HttpServletResponse response)
            throws IOException {

        Map<String, Object> data = AppServlet.getDefaultData();
        List<Map<String, String>> courseModuleDataList = new ArrayList<>();
        for (CourseModule module : MongoDB.getAll(CourseModule.class)) {
            Map<String, String> courseModuleData = new TreeMap<>();
            courseModuleData.put("id", String.valueOf(module.getId()));
            courseModuleData.put("name", module.getName());
            courseModuleData.put("assessment", module.getAssessment());
            courseModuleData
                    .put("credits", String.valueOf(module.getCredits()));
            courseModuleDataList.add(courseModuleData);
        }
        data.put("modules", courseModuleDataList);
        AppServlet.processTemplate(LIST_SITE, data, response.getWriter());
    }

    /**
     * Retrieves the courses of the given course module and displays them.
     * 
     * @param response
     *            provides HTTP-specific functionality in sending a response.
     * 
     * @param moduleId
     *            the id identifying the course module.
     * @throws IOException
     *             if an input or output exception occurs.
     */
    private void showCourses(HttpServletResponse response, ObjectId moduleId)
            throws IOException {

        Map<String, Object> data = AppServlet.getDefaultData();
        CourseModule module = MongoDB.get(CourseModule.class, moduleId);

        data.put("name", module.getName());
        List<Map<String, String>> courseDataList = new ArrayList<>();
        for (Course course : module.getCourses()) {
            Map<String, String> courseData = new TreeMap<>();
            courseData.put("type", course.getType());
            courseData.put("duration", String.valueOf(course.getDuration()));
            courseData.put("count", String.valueOf(course.getCount()));
            courseDataList.add(courseData);
        }
        data.put("courses", courseDataList);
        AppServlet.processTemplate(COURSES_SITE, data, response.getWriter());
    }

    /**
     * Handles a course module creation and modification request.
     * 
     * @param request
     *            provides request information for HTTP servlets.
     * @param response
     *            provides HTTP-specific functionality in sending a response.
     * @param module
     *            The course module object if it is present, if it is present
     *            this is an entity modification request, else it is an entity
     *            creation request.
     * @throws IOException
     *             if an input or output exception occurs.
     */
    private void handleModuleForm(HttpServletRequest request,
            HttpServletResponse response, CourseModule module)
            throws IOException {

        Map<String, Object> data = AppServlet.getDefaultData();
        List<Map<String, String>> courseDataList = new ArrayList<>();

        String name = request.getParameter("name");
        int credits = Integer.valueOf(request.getParameter("credits"));
        String assessment = request.getParameter("assessment");

        String[] courseTypes = request.getParameterValues("course-type");
        String[] courseDurations = request
                .getParameterValues("course-duration");
        String[] courseCounts = request.getParameterValues("course-count");

        String submitReason = request.getParameter("submit-reason");
        if (submitReason.equals("New Course")) {

            data.put("name", name);
            data.put("credits", credits);
            data.put("assessment", assessment);

            for (int i = 0; i < courseTypes.length; ++i) {
                Map<String, String> courseData = new TreeMap<>();
                courseData.put("type", courseTypes[i]);
                courseData.put("duration", courseDurations[i]);
                courseData.put("count", courseCounts[i]);
                courseDataList.add(courseData);
            }

            String requestHeadline = (module == null) ? "New Course Module"
                    : "Edit Course Module";

            data.put("courses", courseDataList);
            data.put(AppServlet.REQUEST_HEADLINE_VAR, requestHeadline);
            data.put("blankCourse", true);

            AppServlet.processTemplate(FORM_SITE, data, response.getWriter());
        } else if (submitReason.equals("Create")) {

            if (module == null) {
                module = new CourseModule(name, credits, assessment);
            } else {
                module.setName(name);
                module.setCredits(credits);
                module.setAssessment(assessment);
                module.getCourses().clear();
            }

            MongoDB.store(module);
            for (int i = 0; i < courseTypes.length; ++i) {
                Course course = new Course(courseTypes[i],
                        Integer.valueOf(courseDurations[i]),
                        Integer.valueOf(courseCounts[i]));
                course.setModule(module);
                MongoDB.store(course);
                module.getCourses().add(course);
            }
            MongoDB.store(module);

            response.sendRedirect("/modules");

        } else {
            throw new IllegalStateException(
                    "An unknown submit value was received.");
        }
    }

    /**
     * Handles a course module modification request.
     * 
     * @param request
     *            provides request information for HTTP servlets.
     * @param response
     *            provides HTTP-specific functionality in sending a response.
     * @param module
     *            the course module which is requested for modification.
     * @throws IOException
     *             if an input or output exception occurs.
     */
    private void handleModuleModification(HttpServletRequest request,
            HttpServletResponse response, CourseModule module)
            throws IOException {

        Map<String, Object> data = AppServlet.getDefaultData();
        List<Map<String, Object>> courseDataList = new ArrayList<>();

        data.put("name", module.getName());
        data.put("credits", module.getCredits());
        data.put("assessment", module.getAssessment());

        for (Course course : module.getCourses()) {
            Map<String, Object> courseData = new TreeMap<>();
            courseData.put("type", course.getType());
            courseData.put("duration", course.getDuration());
            courseData.put("count", course.getCount());
            courseDataList.add(courseData);
        }

        data.put("courses", courseDataList);
        data.put(AppServlet.REQUEST_HEADLINE_VAR, "Edit Course Module");
        AppServlet.processTemplate(FORM_SITE, data, response.getWriter());
    }

    /**
     * @return an singleton instance of {@link ModuleServlet}.
     */
    public static ModuleServlet getInstance() {
        return INSTANCE;
    }

}
