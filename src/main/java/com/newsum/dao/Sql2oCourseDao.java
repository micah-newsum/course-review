package com.newsum.dao;

import com.newsum.exc.DaoException;
import com.newsum.model.Course;

import org.sql2o.Sql2o;
import org.sql2o.Connection;
import org.sql2o.Sql2oException;

import java.util.ArrayList;
import java.util.List;

public class Sql2oCourseDao implements CourseDao{

  private final Sql2o sql2o;

  public Sql2oCourseDao(Sql2o sql2o){
    this.sql2o = sql2o;
  }

  @Override
  public void add(Course course) throws DaoException {
    String sql = "INSERT INTO courses (name,url) VALUES (:name,:url)";
    try (Connection con = sql2o.open()){
      int id = (int) con.createQuery(sql)
                        .bind(course)
                        .executeUpdate()
                        .getKey();
      course.setId(id);
    } catch (Sql2oException ex){
      throw new DaoException(ex,"Problem adding course.");
    }

  }

  @Override
  public List<Course> findAll() {
    String sql = "SELECT * FROM courses";
    List<Course> allCourses = new ArrayList<>();
    try (Connection con = sql2o.open()){
      allCourses = con.createQuery(sql)
                      .executeAndFetch(Course.class);
    }
    return allCourses;
  }
}
