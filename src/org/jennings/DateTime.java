/*
 * DateTime class is used to store a specific date and time to withing fractions
 * of a second.  
 */
package org.jennings;

import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTime {

    final private static String[] DAYNAMESLONG = {"Sunday", "Monday",
        "Tuesday", "Wednesday", "Thursday", "Friday",
        "Saturday"};

    final private static String[] DAYNAMESSHORT = {"Sun", "Mon",
        "Tue", "Wed", "Thu", "Fri",
        "Sat"};

    final private static String[] MONTHNAMES = {"JAN", "FEB", "MAR",
        "APR", "MAY", "JUN", "JUL", "AUG", "SEP",
        "OCT", "NOV", "DEC"};

    final private static int[] DAYSPERMONTH = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private int yearV;
    private int monthV;
    private int dayV;
    private int jdayV;
    private int hourV;
    private int minV;
    private double secV;

    /**
     * Default constructor sets date for Jan 1, 2000 at 0:00:00
     */
    DateTime() {

        this.yearV = 2000;
        this.monthV = 1;
        this.dayV = 1;
        date2jday();
        this.hourV = 0;
        this.minV = 0;
        this.secV = 0.0;
    }

    DateTime(double JD) {

        // This roughly cooresponds to years 1900 to 2200
        /*
             * Might be OK outside this range, but I wouldn't bet on it.
         */
        if (JD > 2524957.5 || JD < 2415020.5) {
            throw new IllegalArgumentException("JD must be between 2415020.5 and 2524957.5");
        }

        int Z = (int) (JD + 0.5);
        double F = (JD + 0.5) - Z;

        int A;

        if (Z < 2299161) {
            A = Z;
        } else {
            int alpha = (int) ((Z - 1867216.25) / 36524.25);
            A = Z + 1 + alpha - (int) (alpha / 4);
        }

        int B = A + 1524;

        int C = (int) ((B - 122.1) / 365.25);

        int D = (int) (365.25 * C);

        int E = (int) ((B - D) / 30.6001);

        double day = B - D - (int) (30.6001 * E) + F;

        int month;
        if (E < 14) {
            month = E - 1;
        } else {
            month = E - 13;
        }

        int year;
        if (month > 2) {
            year = C - 4716;
        } else {
            year = C - 4715;
        }

        int date = (int) day;

        double t = day - date;  // fractional day

        t = t * 24;  // hours        
        int hrs = (int) t;

        t = t - hrs;  // fractional hour
        t = t * 60;  // minutes

        int min = (int) t;

        t = t - min;  // fraction min
        t = t * 60;  // seconds;

        int sec = (int) Math.round(t);

        this.yearV = year;
        this.monthV = month;
        this.dayV = date;
        this.hourV = hrs;
        this.minV = min;
        this.secV = sec;

    }

    DateTime(int d, int m, int y, int h, int mn, double s) {

        // Check for invalid inputs and throw an InvalidDateTimeException
        if (m > 12 || m < 1) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        this.monthV = m;

        if (y > 2200 || y < 1900) {
            throw new IllegalArgumentException("Year must be between 1900 and 2200");
        }
        this.yearV = y;

        int iDays = DAYSPERMONTH[m - 1];
        if (m == 2) {
            iDays = iDays + leapyear();
        }

        if (d > iDays || d < 0) {
            throw new IllegalArgumentException("Days must be between 0 and " + String.valueOf(iDays));
        }
        this.dayV = d;

        if (h > 23 || h < 0) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        this.hourV = h;

        if (mn > 59 || mn < 0) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
        this.minV = mn;

        if (s < 0.0 || s >= 60.0) {
            throw new IllegalArgumentException("Seconds must be between 0 and 59.9999999");
        }
        this.secV = s;

        date2jday();

    }

    /**
     *
     * @param year Four Digit Year 2017
     * @param jday Jday if Double format (Line 1 of TLE fields 21-32)
     */
    DateTime(int year, double jdayFloat) {
        this.yearV = year;
        this.jdayV = (int) jdayFloat;

        jday2date();

        double partDay = jdayFloat - this.jdayV;
        // Convert the fraction of day into hour:min:sec
        double hourFloat = partDay * 24.0;
        this.hourV = (int) hourFloat;
        double partHour = hourFloat - this.hourV;

        double minFloat = partHour * 60.0;
        this.minV = (int) minFloat;
        double partMin = minFloat - this.minV;

        this.secV = partMin * 60.0;
    }

    DateTime(int jday, int y, int h, int mn, double s) {

        if (y > 2200 || y < 1900) {
            throw new IllegalArgumentException("Year must be between 1900 and 2200");
        }
        this.yearV = y;

        int iDays = 365 + leapyear();

        if (jday > iDays || jday < 0) {
            throw new IllegalArgumentException("JDay must be between 1 and " + String.valueOf(iDays));
        }
        this.jdayV = jday;

        jday2date();

        if (h > 23 || h < 0) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        this.hourV = h;

        if (mn > 59 || mn < 0) {
            throw new IllegalArgumentException("Minute must be between 0 and 59");
        }
        this.minV = mn;

        if (s < 0.0 || s >= 60.0) {
            throw new IllegalArgumentException("Seconds must be between 0 and 59.9999999");
        }
        this.secV = s;

    }

    private int leapyear() {
        /*
             * Returns 1 if its a leap year and 0 otherwise
         */

        int leap = 0;
        if (this.yearV % 4 == 0) {
            leap = 1;
            if (this.yearV % 100 == 0) {
                leap = 0;
                if (this.yearV % 400 == 0) {
                    leap = 1;
                }
            }
        }
        return leap;
    }

    private int round(double x) {
        int ix = (int) x;
        double fract = x - ix;
        return fract < 0.5 ? ix : ix + 1;
    }

    private void date2jday() {
        jdayV = dayV;
        int numdays[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        numdays[1] = numdays[1] + leapyear();
        for (int i = 1; i < monthV; i++) {
            jdayV = jdayV + numdays[i - 1];
        }
    }

    private void jday2date() {
        int jd = jdayV;
        monthV = 1;
        int numdays[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        numdays[1] = numdays[1] + leapyear();
        while (jd > numdays[monthV - 1]) {
            jd = jd - numdays[monthV - 1];
            monthV++;
        }
        dayV = jd;
    }

    /**
     * Get the Number of Milliseconds from Epoch Jan 1 1970.
     *
     * @return
     */
    public long epochTimeMillis() {
        // Find the number of seconds for beginning of day
        GregorianCalendar gc = new GregorianCalendar(this.yearV, this.monthV - 1, this.dayV, 0, 0, 0);
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        long toMilliSec = gc.getTimeInMillis();

        // Add in the remaining seconds for the day
        double secs = this.hourV * 3600.0;
        secs += this.minV * 60.0;
        secs += this.secV;
        toMilliSec += Math.round(secs * 1000.0);
        return toMilliSec;
    }

    /**
     * Get the Number of Milliseconds from Epoch Jan 1 1970.
     *
     * @return
     */
    public long epochTimeSecs() {
        // Find seconds for beginning of day
        GregorianCalendar gc = new GregorianCalendar(this.yearV, this.monthV - 1, this.dayV, 0, 0, 0);
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Convert milli to sec
        long toSec = gc.getTimeInMillis() / 1000;

        // Add in seconds for day rounded
        long secs = this.hourV * 3600;
        secs += this.minV * 60;
        secs += round(this.secV);

        return toSec + secs;

    }

    public MonthDay getEasterDay() {
        MonthDay ed = new MonthDay();

        int year = this.yearV;
        int a = year % 19;
        int c = year % 100;
        int b = year / 100;
        int e = b % 4;
        int d = b / 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int k = c % 4;
        int i = c / 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int n = (h + l - 7 * m + 114) / 31;
        int p = (h + l - 7 * m + 114) % 31;

        ed.setMonth(n);
        ed.setDay(p + 1);

        return ed;
    }

    public double getJD() {
        /*
             * 
             *  Gregorian Calendar values returned.  For dates in distant past
             *  use another procedure.
         */

        int Y = this.yearV;
        int M = this.monthV;
        double D = this.dayV + this.hourV / 24.0 + this.minV / 1440.0 + this.secV / 86400.0;

        if (M <= 2) {
            Y = Y - 1;
            M = M + 12;
        }

        int A = Y / 100;
        int B = 2 - A + A / 4;

        double JD = (int) (365.25 * (Y + 4716));

        JD = JD + (int) (30.6001 * (M + 1));

        JD = JD + D + B - 1524.5;

        return JD;
    }

    @Override
    public String toString() {
        StringBuffer strMin;
        StringBuffer strSec;

        if (this.minV < 10) {
            strMin = new StringBuffer("0" + this.minV);
        } else {
            strMin = new StringBuffer(Integer.toString(this.minV));
        }

        DecimalFormat formatter = new DecimalFormat("#.00");

        if (this.secV < 10) {
            strSec = new StringBuffer("0" + String.format("%1.2f", this.secV));
        } else {
            strSec = new StringBuffer(String.format("%2.2f", this.secV));
        }
        return this.dayV + "-" + DateTime.MONTHNAMES[this.monthV - 1] + "-" + this.yearV
                + " " + this.hourV + ":" + strMin + ":" + strSec;
    }

    public void delta(int dday, int dhour, int dmin, double dsec) {
        /*
             * Moves time forward by specified day,hour,min and seconds
         */

        double n = dday * 86400.0 + dhour * 3600.0 + dmin * 60 + dsec;
        delta(n);
    }

    public void delta(double n) {
        /*
             * Moves time forward n seconds
             * 
         */

        long dm = (long) ((this.secV + n) / 60);
        this.secV = ((this.secV + n) / 60.0 - dm) * 60;
        long dh = (long) ((this.minV + dm) / 60);
        this.minV = round(((this.minV + dm) / 60.0 - dh) * 60);
        long dj = (long) ((this.hourV + dh) / 24);
        this.hourV = round(((this.hourV + dh) / 24.0 - dj) * 24);
        int leap = leapyear();
        int dy = (int) ((this.jdayV + dj) / (365 + leap));
        this.jdayV = round((((this.jdayV + dj) / (double) (365 + leap)) - dy) * (365 + leap));
        this.yearV = this.yearV + dy;
        jday2date();
    }

    public String getMonthName() {
        return MONTHNAMES[monthV - 1];
    }

    public String getDayName(boolean blnLong) {
        if (blnLong) {
            return DAYNAMESLONG[dayV - 1];
        } else {
            return DAYNAMESSHORT[dayV - 1];
        }
    }

    public double minus(DateTime A) {
        int leap = leapyear();
        int yrDiff = this.yearV - A.yearV;
        double yrSec = yrDiff * 86400.0 * (leap + 365);
        double jdaySec = (this.jdayV - A.jdayV) * 86400.0;
        double hourSec = (this.hourV - A.hourV) * 3600.0;
        double minSec = (this.minV - A.minV) * 60.0;
        double secSec = (this.secV - A.secV);
        return yrSec + jdaySec + hourSec + minSec + secSec;
    }

    public DateTime plus(double n) {
        long dm = (long) ((this.secV + n) / 60);
        double tempSec = ((this.secV + n) / 60.0 - dm) * 60;
        long dh = (long) ((this.minV + dm) / 60);
        int tempMin = round(((this.minV + dm) / 60.0 - dh) * 60);
        long dj = (long) ((this.hourV + dh) / 24);
        int tempHour = round(((this.hourV + dh) / 24.0 - dj) * 24);
        int leap = leapyear();
        int dy = (int) ((this.jdayV + dj) / (365 + leap + 1));
        int tempJday = round((((this.jdayV + dj) / (double) (365 + leap)) - dy) * (365 + leap));
        int tempYear = this.yearV + dy;
        DateTime temp = new DateTime(tempJday, tempYear, tempHour, tempMin, tempSec);
        return temp;
    }

    public double GST() {

        double fday = (hourV * 3600.0 + minV * 60.0 + secV) / 86400.0;
        double dj = 365.0 * (yearV - 1900.0) + (yearV - 1901.0) / 4.0
                + jdayV + fday - 0.50;
        double t = dj / 36525.0;

        return (99.6910 + (36000.7689 + 0.0004 * t) * t
                + fday * 360.0) % 360.0;
    }

    public double GST2() {
        double julianDay = getJD();

        double T = (julianDay - 2451545.0) / 36525.0;

        double gst = (280.46061837 + 360.98564736629 * (julianDay - 2451545.0)
                + 0.000387933 * T * T - 1.0 / 38710000 * T * T * T) % 360;
        if (gst < 0) {
            gst = gst + 360.0;
        }
        return gst;

    }

    public static void main(String[] args) {
        try {

            DateTime dt = new DateTime(1, 1, 2017, 1, 1, 1);

            System.out.println(dt);
            System.out.println(dt.epochTimeMillis());
            System.out.println(dt.epochTimeSecs());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
