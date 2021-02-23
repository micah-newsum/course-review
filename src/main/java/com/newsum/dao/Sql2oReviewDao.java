package com.newsum.dao;

import com.newsum.exc.DaoException;
import com.newsum.model.Review;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.ArrayList;
import java.util.List;

public class Sql2oReviewDao implements ReviewDao {
  private final Sql2o sql2o;

  public Sql2oReviewDao(Sql2o sql2o){
    this.sql2o = sql2o;
  }

  @Override
  public void add(Review review) throws DaoException {
    String sql = "INSERT INTO reviews (course_id,rating,comment) VALUES(:courseId,:rating,:comment)";
    try(Connection con = sql2o.open()){
      int id = (int) con.createQuery(sql)
                        .bind(review)
                        .executeUpdate()
                        .getKey();
      review.setId(id);
    }catch (Sql2oException exception){
      throw new DaoException(exception,"Problem adding review");
    }
  }

  @Override
  public List<Review> findAll() {
    List<Review> reviews = new ArrayList<>();
    try(Connection con = sql2o.open()){
      reviews = con.createQuery("SELECT * FROM reviews")
                    .addColumnMapping("COURSE_ID","courseId")
                    .executeAndFetch(Review.class);
    }
    return reviews;
  }

  @Override
  public List<Review> findByCourseId(int courseId) {
    List<Review> reviews = new ArrayList<>();
    try(Connection con = sql2o.open()){
      reviews = con.createQuery("SELECT * FROM reviews WHERE course_id = :courseId")
                    .addColumnMapping("COURSE_ID","courseId")
                    .addParameter("courseId",courseId)
                    .executeAndFetch(Review.class);
    }
    return reviews;
  }
}
