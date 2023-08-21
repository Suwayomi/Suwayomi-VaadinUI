package online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common;

import java.time.LocalDate;

public record MediaDate(Integer year, Integer month, Integer day) {

  public MediaDate(LocalDate date) {
    this(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }
}
