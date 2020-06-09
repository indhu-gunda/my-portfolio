package com.google.sps.data;

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
}
