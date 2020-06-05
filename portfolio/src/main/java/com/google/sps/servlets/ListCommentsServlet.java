// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/** Servlet responsible for listing comments. */
@WebServlet("/list-comments")
public class ListCommentsServlet extends HttpServlet {

  private static final Gson gson = new Gson();
  private static final int DEFAULT_MAX_NUM_COMMENTS = 5;
  private static final int DEFAULT_PAGE_NUM = 1;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int maxNumComments = getParameter(request, "num-comments", DEFAULT_MAX_NUM_COMMENTS);
    int pageNum = getParameter(request, "page-num", DEFAULT_PAGE_NUM);
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(query);
    List<Entity> results = pq.asList(FetchOptions.Builder
      .withOffset((pageNum-1) * maxNumComments)
      .limit(maxNumComments));

    List<Comment> comments =
      results.stream()
      .filter(entity -> entity.hasProperty("message"))
      .map(entity -> new Comment(entity.getKey().getId(), (String) entity.getProperty("name"), (String) entity.getProperty("message"), (long) entity.getProperty("timestamp")))
      .collect(Collectors.toList());

    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(comments));
  }

  /**
  *@return the request parameter or default value if invalid. 
  */
  private int getParameter(HttpServletRequest request, String parameterName, int defaultValue) {
    String parameterString = request.getParameter(parameterName);
    int parameter;
    try {
      parameter = Integer.parseInt(parameterString);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
    return parameter;
  }
}
