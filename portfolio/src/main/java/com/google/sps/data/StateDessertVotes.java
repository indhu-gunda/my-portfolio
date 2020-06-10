package com.google.sps.data;


/** Dessert vote totals for a state. */
public final class StateDessertVotes {

  private final String state;
  private final int cheesecakeVotes;
  private final int applePieVotes;
  private final int chocolateChipCookiesVotes;
  private final int tiramisuVotes;
  private final int chocolateCakeVotes;

  public StateDessertVotes(String state, int cheesecakeVotes, int applePieVotes, 
    int chocolateChipCookiesVotes, int tiramisuVotes, int chocolateCakeVotes) {

    this.state = state;
    this.cheesecakeVotes = cheesecakeVotes;
    this.applePieVotes = applePieVotes;
    this.chocolateChipCookiesVotes = chocolateChipCookiesVotes;
    this.tiramisuVotes = tiramisuVotes;
    this.chocolateCakeVotes = chocolateCakeVotes;

  }
}