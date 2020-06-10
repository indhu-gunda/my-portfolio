package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.gson.Gson;
import com.google.sps.data.StateDessertVotes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/** Servlet responsible for listing dessert vote totals per state. */
@WebServlet("/list-state-dessert-votes")
public class ListStateDessertVotesServlet extends HttpServlet {

  private static final Gson gson = new Gson();
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final String [] states = new String [] {"AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC",  
    "DE", "FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA",  
    "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE",  
    "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "RI", "SC",  
    "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY"};

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<StateDessertVotes> stateDessertVotesList =
      Stream.of(states)
      .map(state -> createStateDessertVotes(state))
      .collect(Collectors.toList());

    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(stateDessertVotesList));
  }

  private StateDessertVotes createStateDessertVotes(String state) {
    int cheesecakeVotes = dessertVotesCountPerState("cheesecake", state);
    int applePieVotes = dessertVotesCountPerState("apple pie", state);
    int chocolateChipCookiesVotes = dessertVotesCountPerState("chocolate chip cookies", state);
    int tiramisuVotes = dessertVotesCountPerState("tiramisu", state);
    int chocolateCakeVotes = dessertVotesCountPerState("chocolate cake", state);
    return new StateDessertVotes(state, cheesecakeVotes, applePieVotes, 
      chocolateChipCookiesVotes, tiramisuVotes, chocolateCakeVotes);
  }

  private int dessertVotesCountPerState(String targetDessert, String targetState) {
    Filter dessertFilter = new FilterPredicate("fav-dessert", FilterOperator.EQUAL, targetDessert);
    Filter stateFilter = new FilterPredicate("state", FilterOperator.EQUAL, targetState);
    Filter dessertAndStateFilter = CompositeFilterOperator.and(dessertFilter, stateFilter);

    Query query = new Query("Response").setFilter(dessertAndStateFilter);

    return datastore.prepare(query).countEntities();
  }
}
