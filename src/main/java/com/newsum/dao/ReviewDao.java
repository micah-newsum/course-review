package com.newsum.dao;

import com.newsum.exc.DaoException;
import com.newsum.model.Review;

import java.util.List;

public interface ReviewDao {
  void add(Review review) throws DaoException;

  List<Review> findAll();

  List<Review> findByCourseId(int courseId);
}
