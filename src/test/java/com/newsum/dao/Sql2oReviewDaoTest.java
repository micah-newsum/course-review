package com.newsum.dao;

import static org.junit.Assert.*;

import com.newsum.exc.DaoException;
import com.newsum.model.Course;
import com.newsum.model.Review;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;

public class Sql2oReviewDaoTest {
  private Sql2oReviewDao reviewDao;
  private Connection con;
  private Sql2oCourseDao courseDao;
  private Course course;

  @Before
  public void setUp() throws Exception {
    String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";
    Sql2o sql2o = new Sql2o(connectionString,"","");
    con = sql2o.open();
    reviewDao = new Sql2oReviewDao(sql2o);
    courseDao = new Sql2oCourseDao(sql2o);
    course = new Course("Test Course","http://test.com");
    courseDao.add(course);
    // keep connection open so that database is not wiped out
  }

  @After
  public void tearDown() throws Exception {
    con.close();
  }

  @Test
  public void addingReviewSetsId() throws Exception {
    Review review = newTestReview();
    review.setCourseId(course.getId());
    int origId = review.getId();
    reviewDao.add(review);
    assertNotEquals(origId,review.getId());
  }

  @Test
  public void addedReviewsAreReturnedFromFindAll() throws Exception{
    Review review = newTestReview();
    review.setCourseId(course.getId());
    reviewDao.add(review);
    Review otherReview = new Review(course.getId(),1,"Test comment");
    reviewDao.add(otherReview);
    List<Review> reviews = reviewDao.findAll();
    assertEquals(2,reviews.size());
  }

  @Test(expected = DaoException.class)
  public void addingAReviewToANonexistingCourseFails() throws Exception {
    Review review = newTestReview();
    reviewDao.add(review);
  }

  @Test
  public void existingReviewsCanBeFoundByCourseId() throws Exception{
    Review review = newTestReview();
    review.setCourseId(course.getId());
    reviewDao.add(review);

    List<Review> foundReviews = reviewDao.findByCourseId(review.getCourseId());
    assertEquals(1,foundReviews.size());
  }

  private Course newTestCourse() {
    return new Course("Test", "http://test.com");
  }

  private Review newTestReview() {return new Review(0,5,"I loved this course!");}
}