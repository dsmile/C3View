package com.mdiakonov.c3view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import android.util.Log;

public class DataConditioning {
    static final String NOT_AVAILABLE = "-";

    public static String properCase (String inputVal) {
        if (inputVal.length() == 0) return "";
        if (inputVal.length() == 1) return inputVal.toUpperCase();
        return inputVal.substring(0,1).toUpperCase()
                + inputVal.substring(1).toLowerCase();
    }

    public static String properAgeLabel (int age) {
        if (age > 0) {
            if ((age < 10) || (age > 19)) {
                int minorDigit = (age % 10);
                if (minorDigit == 1) {
                    return age + " год";
                } else if ((minorDigit > 1) && (minorDigit < 5)) {
                    return age + " года";
                } else if ((minorDigit >= 5) || (minorDigit == 0)) {
                    return age + " лет";
                } else return "";
            } else {
                return age + " лет";
            }
        }

        return "";
    }


    public static String properDate (String inputVal) {
        SimpleDateFormat format;

        // Определяем формат даты
        if (inputVal.length() > 4) {
            if (inputVal.substring(4,5).equals(String.valueOf("-"))) {
                format = new SimpleDateFormat("yyyy-MM-dd");
            } else {
                format = new SimpleDateFormat("dd-MM-yyyy");
            }
        } else {
            return NOT_AVAILABLE;
        }

        try {
            Date date = format.parse(inputVal);
            SimpleDateFormat properFormat = new SimpleDateFormat("dd.MM.yyyy");
            return properFormat.format(date);
        } catch (Exception ex) {
            return NOT_AVAILABLE;
        }
    }


    public static int getAge(Date dateOfBirth) {
        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        int age = 0;

        birthDate.setTime(dateOfBirth);
        if (birthDate.after(today)) {
            throw new IllegalArgumentException("Родился в будущем");
        }

        age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // If birth date is greater than todays date (after 2 days adjustment of leap year) then decrement age one year
        if ( (birthDate.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) > 3) ||
                (birthDate.get(Calendar.MONTH) > today.get(Calendar.MONTH ))){
            age--;

            // If birth date and todays date are of same month and birth day of month is greater than todays day of month then decrement age
        }else if ((birthDate.get(Calendar.MONTH) == today.get(Calendar.MONTH )) &&
                (birthDate.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH ))){
            age--;
        }

        return age;
    }

    public static int properAge (String inputVal) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Date date = format.parse(inputVal);
            return getAge(date);
        } catch (Exception ex) {
            return 0;
        }
    }
}
