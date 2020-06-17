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

package com.google.sps;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration())
      return new ArrayList<TimeRange>();
    List<TimeRange> freeTimes = queryWithOptionalAttendees(events, request);
    if (freeTimes.size() == 0 && request.getAttendees().size() > 0)
      freeTimes = queryWithOnlyMandatoryAttendees(events, request);
    return freeTimes;
   
  }

  private List<TimeRange> queryWithOptionalAttendees(Collection<Event> events, MeetingRequest request) {
    Set<String> allAttendees = new HashSet<String>() {{
      addAll(request.getAttendees());
      addAll(request.getOptionalAttendees());
    }};
    List<TimeRange> busyTimes = getBusyTimes(events, allAttendees);
    return getFreeTimes(busyTimes, request.getDuration());
  }

  private List<TimeRange> queryWithOnlyMandatoryAttendees(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> busyTimes = getBusyTimes(events, request.getAttendees());
    return getFreeTimes(busyTimes, request.getDuration());
  }

  private List<TimeRange> getBusyTimes(Collection<Event> events, Collection<String> requestAttendees) {
    List<TimeRange> busyTimes = new ArrayList<TimeRange>();
    busyTimes = 
      events.stream()
      .filter((event) -> containsAttendee(event, requestAttendees))
      .map((event) -> event.getWhen())
      .sorted(TimeRange.ORDER_BY_START)
      .collect(Collectors.toList());
    return busyTimes;
  }

  private List<TimeRange> getFreeTimes(List<TimeRange> busyTimes, long duration) {
    List<TimeRange> freeTimes = new ArrayList<TimeRange>();
    if(busyTimes.size() == 0) {
      freeTimes.add(TimeRange.WHOLE_DAY);
    } else {
      int index = 0;
      TimeRange first = busyTimes.get(index);
      if (first.start() - TimeRange.START_OF_DAY >= duration)
        freeTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, first.start(),false));
      int start = first.end();
      while(index < busyTimes.size() - 1) {
        first = busyTimes.get(index);
        TimeRange second = busyTimes.get(index + 1);
        if (first.overlaps(second)) {
          start = Math.max(start, Math.max(first.end(), second.end()));
        } else {
          if (second.start() - start >= duration) 
            freeTimes.add(TimeRange.fromStartEnd(start, second.start(), false));
          start = Math.max(start, second.end());
        }
        index++;
      }
      if (TimeRange.END_OF_DAY - start >= duration) 
        freeTimes.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }
    return freeTimes;
    
  }

  private boolean containsAttendee(Event e, Collection<String> requestAttendees) {
    Collection<String> eventAttendees = e.getAttendees();
    for(String attendee : requestAttendees) {
      if(eventAttendees.contains(attendee))
        return true;
    }
    return false;

  }
}
