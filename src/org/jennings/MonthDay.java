

package org.jennings;

/**
 *
 * @author sysdlj
 */
public class MonthDay {

    private int month;
    private int day;

    public MonthDay(int month, int day) {
        this.month = month;
        this.day = day;
    }

    public MonthDay() {
        this.month = 1;
        this.day = 1;
    }
        
        

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

        
}
