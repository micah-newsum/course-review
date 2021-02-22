package com.newsum;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.post;

import com.google.gson.Gson;

import com.newsum.dao.CourseDao;
import com.newsum.dao.Sql2oCourseDao;
import com.newsum.model.Course;
import org.sql2o.Sql2o;

public class Api {
  public static void main(String[] args) {
    Sql2o sql2o = new Sql2o("jdbc:h2:~/reviews.db;INIT=RUNSCRIPT from 'classpath:db/init.sql'","","");
    CourseDao courseDao = new Sql2oCourseDao(sql2o);
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
      return course;
    },gson::toJson);

    // add filter to ensure response type is always application/json
    after((req,res) -> {
      res.type("application/json");
    });
  }
}
