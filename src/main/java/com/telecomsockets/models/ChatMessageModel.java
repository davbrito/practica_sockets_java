package com.telecomsockets.models;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public record ChatMessageModel(String text, ChatUser sender, ChatUser receiver, long sendTimestamp)
        implements Serializable {
    public ChatMessageModel(String text, ChatUser sender, ChatUser receiver) {
        this(text, sender, receiver, System.currentTimeMillis());
    }

    private Date getSendDate() {
        return new Date(sendTimestamp);
    }

    public String getFormattedSendDate() {
        var date = getSendDate();

        if (isToday(date)) {
            return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        }

        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
    }

    private boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar specifiedDate = Calendar.getInstance();
        specifiedDate.setTime(date);

        return today.get(Calendar.DAY_OF_MONTH) == specifiedDate.get(Calendar.DAY_OF_MONTH)
                && today.get(Calendar.MONTH) == specifiedDate.get(Calendar.MONTH)
                && today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR);
    }
}
