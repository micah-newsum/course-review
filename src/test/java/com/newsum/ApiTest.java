package com.newsum;

import static org.junit.Assert.assertEquals;
import static spark.Spark.stop;

import com.google.gson.Gson;

import com.newsum.dao.Sql2oCourseDao;
import com.newsum.dao.Sql2oReviewDao;
import com.newsum.model.Course;
import com.newsum.model.Review;
import com.newsum.testing.ApiClient;
import com.newsum.testing.ApiResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiTest {

  public static final String PORT = "4568";
  public static final String TEST_DATASOURCE = "jdbc:h2:mem:testing";
  private Connection conn;
  private ApiClient client;
  private Gson gson;
  private Sql2oCourseDao courseDao;
  private Sql2oReviewDao reviewDao;

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
    reviewDao = new Sql2oReviewDao(sql2o);
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

  @Test
  public void addingReviewReturnsCreatedStatusCode() throws Exception{
    Course course = newTestCourse();
    courseDao.add(course);

    Map<String, Object> values = new HashMap<>();
    values.put("rating", 5);
    values.put("comment", "test comment");
    values.put("courseId",course.getId());

    ApiResponse response = client.request("POST", String.format("/courses/%d/reviews",course.getId()), gson.toJson(values));
    assertEquals(201, response.getStatus());
  }

  @Test
  public void addingReviewToNonexistentCourseReturnsErrorStatusCode() throws Exception{
    // set course id that does not exist
    int courseId = 100;

    // create mock review
    Map<String, Object> values = new HashMap<>();
    values.put("rating", 5);
    values.put("comment", "test comment");
    values.put("courseId",100);

    // submit api request
    ApiResponse response = client.request("POST", String.format("/courses/%d/reviews",courseId), gson.toJson(values));

    // run test
    assertEquals(500, response.getStatus());
  }

  @Test
  public void multipleReviewsReturnedForCourse() throws Exception{
    // create course
    Course course = newTestCourse();
    courseDao.add(course);

    // create reviews for course
    Review firstReview = new Review(course.getId(),5,"Test comment");
    Review secondReview = new Review(course.getId(),1,"Test comment");

    // create reviews and persist to database
    reviewDao.add(firstReview);
    reviewDao.add(secondReview);

    ApiResponse response = client.request("GET", String.format("/courses/%d/reviews",course.getId()));
    List<Review> reviews = gson.fromJson(response.getBody(),List.class);
    assertEquals(2, reviews.size());
  }
}