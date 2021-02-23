package com.newsum;

import static org.junit.Assert.assertEquals;
import static spark.Spark.stop;

import com.google.gson.Gson;

import com.newsum.dao.Sql2oCourseDao;
import com.newsum.model.Course;
import com.newsum.testing.ApiClient;
import com.newsum.testing.ApiResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;

public class ApiTest {

  public static final String PORT = "4568";
  public static final String TEST_DATASOURCE = "jdbc:h2:mem:testing";
  private Connection conn;
  private ApiClient client;
  private Gson gson;
  private Sql2oCourseDao courseDao;

  @BeforeClass
  public static void startServer() {
    String[] args = {PORT, TEST_DATASOURCE};
    Api.main(args);
  }

  @AfterClass
  public static void stopServer() {
    stop();
  }

  @Before
  public void setUp() throws Exception {
    Sql2o
        sql2o =
        new Sql2o(TEST_DATASOURCE + ";INIT=RUNSCRIPT from 'classpath:db/init.sql'", "", "");
    courseDao = new Sql2oCourseDao(sql2o);
    conn = sql2o.open();
    client = new ApiClient("http://localhost:" + PORT);
    gson = new Gson();
  }

  @After
  public void tearDown() throws Exception {
    conn.close();
  }

  @Test
  public void addingCourseGeneratesCreatedStatus() throws Exception {
    Map<String, String> values = new HashMap<>();
    values.put("name", "Test");
    values.put("url", "http://test.com");

    ApiResponse response = client.request("POST", "/courses", gson.toJson(values));
    assertEquals(201, response.getStatus());
  }


  private Course newTestCourse() {
    return new Course("Test", "http://test.com");
  }

  @Test
  public void coursesCanBeAccessedById() throws Exception {
    Course course = newTestCourse();
    courseDao.add(course);

    ApiResponse res = client.request("GET", "/courses/" + course.getId());
    Course retrieved = gson.fromJson(res.getBody(), Course.class);
    assertEquals(course, retrieved);
  }

  @Test
  public void missingCoursesReturnNotFoundStatus() {
    ApiResponse res = client.request("GET","/courses/42");
    assertEquals(404,res.getStatus());
  }
}