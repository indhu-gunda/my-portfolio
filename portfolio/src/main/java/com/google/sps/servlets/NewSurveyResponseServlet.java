package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that adds a new survey response to the Datastore.*/
@WebServlet("/new-survey-response")
public class NewSurveyResponseServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String favDessert = request.getParameter("fav-dessert");
    String state = request.getParameter("state");
    long timestamp = System.currentTimeMillis();

    Entity responseEntity = new Entity("Response");
    responseEntity.setProperty("fav-dessert", favDessert);
    responseEntity.setProperty("state", state);
    responseEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(responseEntity);

    response.sendRedirect("/index.html#dessert-map");
  
  }

}