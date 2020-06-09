package com.google.sps.data;

import com.google.appengine.api.datastore.Entity;

/** A comment in the comments section. */
public final class Comment {

  private final long id;
  private final String name;
  private final String profile;
  private final String message;
  private final long timestamp;

  public Comment(long id, String name, String profile, String message, long timestamp) {
    this.id = id;
    this.name = name;
    this.profile = profile;
    this.message = message;
    this.timestamp = timestamp;
  }

  public static Comment entityToCommentConverter(Entity entity) {
    long id = entity.getKey().getId();
    String name = (String) entity.getProperty("name");
    String message = (String) entity.getProperty("message");
    long timestamp = (long) entity.getProperty("timestamp");
    return new Comment(id, name, message, timestamp);
  }
}
