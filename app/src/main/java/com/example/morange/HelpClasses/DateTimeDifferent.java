package com.example.morange.HelpClasses;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeDifferent {

    public static long[] ReturnDifferent(Date first,Date second){
            // Calucalte time difference
            // in milliseconds
            long difference_In_Time
                    = first.getTime() - second.getTime();

            // Calucalte time difference in
            // seconds, minutes, hours, years,
            // and days
            long difference_In_Seconds
                    = (difference_In_Time
                    / 1000)
                    % 60;

            long difference_In_Minutes
                    = (difference_In_Time
                    / (1000 * 60))
                    % 60;

            long difference_In_Hours
                    = (difference_In_Time
                    / (1000 * 60 * 60))
                    % 24;

            long difference_In_Years
                    = (difference_In_Time
                    / (1000l * 60 * 60 * 24 * 365));

            long difference_In_Days
                    = (difference_In_Time
                    / (1000 * 60 * 60 * 24))
                    % 365;
            long[] different = {difference_In_Years,difference_In_Days,difference_In_Hours,difference_In_Minutes,difference_In_Seconds};
        return different;
    }

    public static String dateDiffrent(Date date_time, String[] date_and_timeGet) throws ParseException {
        if(!date_time.equals("")){
            String currentDateTimeString = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm").format(System.currentTimeMillis());
            Date currentDate = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm").parse(currentDateTimeString);
            long different[];
            different = DateTimeDifferent.ReturnDifferent(currentDate,date_time);

            long hoursEnd = different[2];
            long minutesEnd = different[3];

            while (hoursEnd >= 10){
                hoursEnd -= 10;
            }
            while (minutesEnd >= 10){
                minutesEnd -= 10;
            }

            if(different.length > 0){
                if (different[1] == 1){
                    return ("вчера "+date_and_timeGet[0]);
                }
                else if(different[1] == 0 && different[2] == 1){
                    return ("1 час");
                }
                else if(different[1] == 0 && (hoursEnd > 1 && hoursEnd < 5)){
                    return (different[2]+" часа");
                }
                else if(different[1] == 0 && different[2] !=0 && (hoursEnd == 0 || hoursEnd > 4 || (different[2] >=10 && different[2] <=20))){
                    return (different[2]+" часов");
                }
                else if(different[1] == 0 && different[2] == 0 && different[3] == 1){
                    return ("1 минуту");
                }
                else if(different[1] == 0 && different[2] == 0 && (minutesEnd > 1 && minutesEnd < 5)){
                    return (different[3]+" минуты");
                }
                else if(different[1] == 0 && different[2] ==0 && different[3] !=0 && (minutesEnd == 0 || minutesEnd > 4 || (different[3] >=10 && different[3] <=20))){
                    return (different[3]+" минут");
                }
                else if(different[1] == 0 && different[2] == 0 && different[3] == 0){
                    return "только что";
                }
                else{
                    return (date_and_timeGet[1]+" " + date_and_timeGet[0]);
                }
            }
        }
        else{
            return ("недавно");
        }
        return null;
    }
}
