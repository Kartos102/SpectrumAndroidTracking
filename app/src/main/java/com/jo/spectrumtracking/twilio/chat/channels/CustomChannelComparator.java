package com.jo.spectrumtracking.twilio.chat.channels;

import com.twilio.chat.Channel;
import java.util.Comparator;

public class CustomChannelComparator implements Comparator<Channel> {

  CustomChannelComparator() {

  }

  @Override
  public int compare(Channel lhs, Channel rhs) {
    if (lhs.getLastMessageDate() == null && rhs.getLastMessageDate() == null) {
      return 0;
    } else if (lhs.getLastMessageDate() == null) {
      return 1;
    } else if (rhs.getLastMessageDate() == null) {
      return -1;
    }
    return rhs.getLastMessageDate().compareTo(lhs.getLastMessageDate());
  }
}
