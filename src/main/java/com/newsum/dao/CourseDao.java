package com.newsum.dao;

import com.newsum.exc.DaoException;
import com.newsum.model.Course;

import java.util.List;

public interface CourseDao {
  void add(Course course) throws DaoException;

  List<Course> findAll();
}
