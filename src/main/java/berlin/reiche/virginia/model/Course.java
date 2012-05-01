package berlin.reiche.virginia.model;

import java.util.Map;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Reference;

/**
 * Represents a course unit which belongs to a {@link CourseModule}.
 * 
 * @author Konrad Reiche
 * 
 */
@Embedded
public class Course {

    String type;
    int duration;
    int count;

    @Reference
    Map<String, Integer> features;

    /**
     * This constructor is used by Morphia via Java reflections.
     */
    @SuppressWarnings("unused")
    private Course() {

    }

    /**
     * Creates a new course by assigning the parameters directly, except the id
     * which is generated by the database after saving the object.
     * 
     * @param type
     *            the course type.
     * @param duration
     *            the duration.
     * @param count
     *            the number of times the course should take place per week.
     */
    public Course(String type, int duration, int count) {
        super();
        this.type = type;
        this.duration = duration;
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public int getCount() {
        return count;
    }

}
