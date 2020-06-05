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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for deleting comments. */
@WebServlet("/delete-comment")
public class DeleteCommentServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long id = getCommentID(request, response);

    Key commentEntityKey = KeyFactory.createKey("Comment", id);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      datastore.delete(commentEntityKey);
    } catch(IllegalArgumentException e) {
      response.sendError(400, "Invalid comment ID.");
    }
  }

  /**
  *@return the comment id parameter of the request or -1 if invalid and set error code. 
  */
  private long getCommentID(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentIDString = request.getParameter("id");
    long commentID;
    try {
      commentID = Long.parseLong(commentIDString);
    } catch (NumberFormatException e) {
      response.sendError(400, "Invalid comment ID format.");
      return -1;
    }
    return commentID;
  }
}
