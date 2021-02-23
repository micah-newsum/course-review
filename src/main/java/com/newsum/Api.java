package com.newsum;

import static spark.Spark.after;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import com.google.gson.Gson;

import com.newsum.dao.CourseDao;
import com.newsum.dao.ReviewDao;
import com.newsum.dao.Sql2oCourseDao;
import com.newsum.dao.Sql2oReviewDao;
import com.newsum.exc.ApiError;
import com.newsum.exc.DaoException;
import com.newsum.model.Course;
import com.newsum.model.Review;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;

public class Api {
  public static void main(String[] args) {
    String datasource = "jdbc:h2:~/reviews.db";
    if (args.length > 0){
      if (args.length != 2){
        System.out.println("java API expected <port> <datasource>");
        System.exit(0);
      }
      port(Integer.parseInt(args[0]));
      datasource = args[1];
    }

    Sql2o sql2o = new Sql2o(
        String.format("%s;INIT=RUNSCRIPT from 'classpath:db/init.sql'",datasource),"","");

    // init dao's
    CourseDao courseDao = new Sql2oCourseDao(sql2o);
    ReviewDao reviewDao = new Sql2oReviewDao(sql2o);

    Gson gson = new Gson();

    post("/courses","application/json",(req,res) -> {
        Course course = gson.fromJson(req.body(),Course.class);
        courseDao.add(course);
        res.status(201);
        return course;
        },gson::toJson);

    get("/courses","application/json",(req,res) ->
      courseDao.findAll(),gson::toJson
    );

    get("/courses/:id","application/json",(req,res) ->{
      int id = Integer.parseInt(req.params("id"));
      //TODO:mjn what if this is not found
      Course course = courseDao.findById(id);
      if (course == null){
        throw new ApiError(404,String.format("Course not found with id of %d.",id));
      }
      return course;
    },gson::toJson);

    // reviews endpoints
    post("/courses/:courseId/reviews",(req,res) ->{
      int courseId = Integer.parseInt(req.params("courseId"));
      Review review = gson.fromJson(req.body(), Review.class);
      review.setCourseId(courseId);
      try{
        reviewDao.add(review);
      } catch (DaoException exception){
        throw new ApiError(500,exception.getMessage());
      }
      res.status(201);
      return review;
    },gson::toJson);

    get("/courses/:courseId/reviews",(req,res) ->{
            int courseId = Integer.parseInt(req.params("courseId"));
            return reviewDao.findByCourseId(courseId);
            }
            ,gson::toJson);

    //exception handler
    exception(ApiError.class,(exc,req,res) -> {
      ApiError err = (ApiError) exc;
      Map<String,Object> jsonMap = new HashMap<>();
      jsonMap.put("status",err.getStatus());
      jsonMap.put("errorMessage",err.getMessage());
      res.type("application/json"); // must set type because after() not run after exception handler
      res.status(err.getStatus());
      res.body(gson.toJson(jsonMap));
    });

    // add filter to ensure response type is always application/json
    after((req,res) -> {
      res.type("application/json");
    });
  }
}
