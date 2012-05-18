<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
        <head>
                <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
                <title>${title}</title>
        </head>
        <body>
                <h1>Course Responsiblities</h1>
                <div class="navigation">
                        <ol>
                                <li><a href="/modules">Back</a></li>
                        </ol>
                </div>
                <div class="content">
                        <form action="" method="post"> 
                                <table>
                                        <thead>
                                                <tr>
                                                        <td>Responsibility</td>
                                                        <td>Name</td>
                                                        <td>Credits</td>
                                                        <td>Assessment</td>
                                                </tr>
                                        </thead>
                                        <tbody>
                                                <#list modules as module>
                                                <tr>   
                                                        <td></td>
                                                        <td><strong>${module.name}</strong></td>
                                                        <td>${module.credits}</td>
                                                        <td>${module.assessment}</td>
                                                </tr>
                                                <#list module.courses as course>
                                                <tr>
                                                        <#assign isResponsible = ""/>
                                                        <#if user.responsibleCourses?seq_contains(course)>
                                                        <#assign isResponsible = "checked"/>
                                                        </#if>
                                                        <td><input name="responsibility" value="${course.id}" type="checkbox" ${isResponsible}/></td>
                                                        <td>${course.type}</td>
                                                </tr>
                                                </#list>
                                                </#list>
                                        </tbody>
                                </table>
                                <input type="submit" value="Save"/>
                        </form>
                </div>
        </body>
</html>
