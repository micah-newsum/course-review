package com.newsum.dao;

    import static org.junit.Assert.assertEquals;
    import static org.junit.Assert.assertNotEquals;

    import com.newsum.model.Course;
    import org.junit.After;
    import org.junit.Before;
    import org.junit.Test;
    import org.sql2o.Connection;
    import org.sql2o.Sql2o;

public class Sql2oCourseDaoTest {

  private Sql2oCourseDao dao;
  private Connection con;

  @Before
  public void setUp() {
    String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";
    Sql2o sql2o = new Sql2o(connectionString,"","");
    dao = new Sql2oCourseDao(sql2o);
    // keep connection open so that database is not wiped out
    con = sql2o.open();
  }

  @After
  public void name() {
    con.close();
  }

  @Test
  public void addingCourseSetsId() throws Exception{
    Course course = new Course("Test","http://test.com");
    int origCourseId = course.getId();

    dao.add(course);

    assertNotEquals(origCourseId,course.getId());
  }

  @Test
  public void addedCoursesAreReturnedFromFindAll() throws Exception{
    Course course = new Course("Test","http://test.com");
    dao.add(course);

    assertEquals(1,dao.findAll().size());
  }

  @Test
  public void noCoursesReturnsEmptyList() throws Exception{
    assertEquals(0, dao.findAll().size());
  }

}


