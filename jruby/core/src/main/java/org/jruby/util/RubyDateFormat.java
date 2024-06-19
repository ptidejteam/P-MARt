/***** BEGIN LICENSE BLOCK *****
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2002, 2009 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2004 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.util;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;

import static org.jruby.util.RubyDateFormat.FieldType.*;

public class RubyDateFormat extends DateFormat {
    private static final long serialVersionUID = -250429218019023997L;

    private boolean ruby_1_9;
    private List<Token> compiledPattern;

    private final DateFormatSymbols formatSymbols;

    private static final int FORMAT_STRING = 0;
    private static final int FORMAT_WEEK_LONG = 1;
    private static final int FORMAT_WEEK_SHORT = 2;
    private static final int FORMAT_MONTH_LONG = 3;
    private static final int FORMAT_MONTH_SHORT = 4;
    private static final int FORMAT_DAY = 5;
    private static final int FORMAT_DAY_S = 6;
    private static final int FORMAT_HOUR = 7;
    private static final int FORMAT_HOUR_M = 8;
    private static final int FORMAT_HOUR_S = 9;
    private static final int FORMAT_DAY_YEAR = 10;
    private static final int FORMAT_MINUTES = 11;
    private static final int FORMAT_MONTH = 12;
    private static final int FORMAT_MERIDIAN = 13;
    private static final int FORMAT_MERIDIAN_LOWER_CASE = 14;
    private static final int FORMAT_SECONDS = 15;
    private static final int FORMAT_WEEK_YEAR_S = 16;
    private static final int FORMAT_WEEK_YEAR_M = 17;
    private static final int FORMAT_DAY_WEEK = 18;
    private static final int FORMAT_YEAR_LONG = 19;
    private static final int FORMAT_YEAR_SHORT = 20;
    private static final int FORMAT_COLON_ZONE_OFF = 21;
    private static final int FORMAT_ZONE_ID = 22;
    private static final int FORMAT_CENTURY = 23;
    private static final int FORMAT_HOUR_BLANK = 24;
    private static final int FORMAT_MILLISEC = 25;
    private static final int FORMAT_EPOCH = 26;
    private static final int FORMAT_DAY_WEEK2 = 27;
    private static final int FORMAT_WEEK_WEEKYEAR = 28;
    private static final int FORMAT_NANOSEC = 29;
    private static final int FORMAT_WEEKYEAR = 30;
    private static final int FORMAT_OUTPUT = 31;

    private static class Token {
        private final int format;
        private final Object data;
        
        public Token(int format) {
            this(format, null);
        }

        public Token(int format, Object data) {
            this.format = format;
            this.data = data;
        }
        
        /**
         * Gets the data.
         * @return Returns a Object
         */
        public Object getData() {
            return data;
        }

        /**
         * Gets the format.
         * @return Returns a int
         */
        public int getFormat() {
            return format;
        }
    }

    /**
     * Constructor for RubyDateFormat.
     */
    public RubyDateFormat() {
        this("", new DateFormatSymbols());
    }

    public RubyDateFormat(String pattern, Locale aLocale) {
        this(pattern, new DateFormatSymbols(aLocale));
    }

    public RubyDateFormat(String pattern, Locale aLocale, boolean ruby_1_9) {
        this(pattern, aLocale);
        this.ruby_1_9 = ruby_1_9;
    }
    
    public RubyDateFormat(String pattern, DateFormatSymbols formatSymbols) {
        super();

        this.formatSymbols = formatSymbols;
        applyPattern(pattern);
    }
    
    public void applyPattern(String pattern) {
        compilePattern(pattern);
    }
    
    private void compilePattern(String pattern) {
        compiledPattern = new LinkedList<Token>();
        
        int len = pattern.length();
        for (int i = 0; i < len;) {
            if (pattern.charAt(i) == '%') {
                i++;

                if(i == len) {
                    compiledPattern.add(new Token(FORMAT_STRING, "%"));
                } else {
                    i = addOutputFormatter(pattern, i);

                    switch (pattern.charAt(i)) {
                    case 'A' :
                        compiledPattern.add(new Token(FORMAT_WEEK_LONG));
                        break;
                    case 'a' :
                        compiledPattern.add(new Token(FORMAT_WEEK_SHORT));
                        break;
                    case 'B' :
                        compiledPattern.add(new Token(FORMAT_MONTH_LONG));
                        break;
                    case 'b' :
                    case 'h' :
                        compiledPattern.add(new Token(FORMAT_MONTH_SHORT));
                        break;
                    case 'C' :
                        compiledPattern.add(new Token(FORMAT_CENTURY));
                        break;
                    case 'c' :
                        compiledPattern.add(new Token(FORMAT_WEEK_SHORT));
                        compiledPattern.add(new Token(FORMAT_STRING, " "));
                        compiledPattern.add(new Token(FORMAT_MONTH_SHORT));
                        compiledPattern.add(new Token(FORMAT_STRING, " "));
                        compiledPattern.add(new Token(FORMAT_DAY));
                        compiledPattern.add(new Token(FORMAT_STRING, " "));
                        compiledPattern.add(new Token(FORMAT_HOUR));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_MINUTES));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_SECONDS));
                        compiledPattern.add(new Token(FORMAT_STRING, " "));
                        compiledPattern.add(new Token(FORMAT_YEAR_LONG));
                        break;
                    case 'D':
                        compiledPattern.add(new Token(FORMAT_MONTH));
                        compiledPattern.add(new Token(FORMAT_STRING, "/"));
                        compiledPattern.add(new Token(FORMAT_DAY));
                        compiledPattern.add(new Token(FORMAT_STRING, "/"));
                        compiledPattern.add(new Token(FORMAT_YEAR_SHORT));
                        break;
                    case 'd':
                        compiledPattern.add(new Token(FORMAT_DAY));
                        break;
                    case 'e':
                        compiledPattern.add(new Token(FORMAT_DAY_S));
                        break;
                    case 'F':
                        compiledPattern.add(new Token(FORMAT_YEAR_LONG));
                        compiledPattern.add(new Token(FORMAT_STRING, "-"));
                        compiledPattern.add(new Token(FORMAT_MONTH));
                        compiledPattern.add(new Token(FORMAT_STRING, "-"));
                        compiledPattern.add(new Token(FORMAT_DAY));
                        break;
                    case 'G':
                        compiledPattern.add(new Token(FORMAT_WEEKYEAR));
                        break;
                    case 'H':
                        compiledPattern.add(new Token(FORMAT_HOUR));
                        break;
                    case 'I':
                        compiledPattern.add(new Token(FORMAT_HOUR_M));
                        break;
                    case 'j':
                        compiledPattern.add(new Token(FORMAT_DAY_YEAR));
                        break;
                    case 'k':
                        compiledPattern.add(new Token(FORMAT_HOUR_BLANK));
                        break;
                    case 'L':
                        compiledPattern.add(new Token(FORMAT_MILLISEC));
                        break;
                    case 'l':
                        compiledPattern.add(new Token(FORMAT_HOUR_S));
                        break;
                    case 'M':
                        compiledPattern.add(new Token(FORMAT_MINUTES));
                        break;
                    case 'm':
                        compiledPattern.add(new Token(FORMAT_MONTH));
                        break;
                    case 'N':
                        compiledPattern.add(new Token(FORMAT_NANOSEC));
                        break;
                    case 'n':
                        compiledPattern.add(new Token(FORMAT_STRING, "\n"));
                        break;
                    case 'p':
                        compiledPattern.add(new Token(FORMAT_MERIDIAN));
                        break;
                    case 'P':
                        compiledPattern.add(new Token(FORMAT_MERIDIAN_LOWER_CASE));
                        break;
                    case 'R':
                        compiledPattern.add(new Token(FORMAT_HOUR));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_MINUTES));
                        break;
                    case 'r':
                        compiledPattern.add(new Token(FORMAT_HOUR_M));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_MINUTES));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_SECONDS));
                        compiledPattern.add(new Token(FORMAT_STRING, " "));
                        compiledPattern.add(new Token(FORMAT_MERIDIAN));
                        break;
                    case 's':
                        compiledPattern.add(new Token(FORMAT_EPOCH));
                        break;
                    case 'S':
                        compiledPattern.add(new Token(FORMAT_SECONDS));
                        break;
                    case 'T':
                        compiledPattern.add(new Token(FORMAT_HOUR));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_MINUTES));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_SECONDS));
                        break;
                    case 't':
                        compiledPattern.add(new Token(FORMAT_STRING,"\t"));
                        break;
                    case 'u':
                        compiledPattern.add(new Token(FORMAT_DAY_WEEK2));
                        break;
                    case 'U':
                        compiledPattern.add(new Token(FORMAT_WEEK_YEAR_S));
                        break;
                    case 'v':
                        compiledPattern.add(new Token(FORMAT_DAY_S));
                        compiledPattern.add(new Token(FORMAT_STRING, "-"));
                        compiledPattern.add(new Token(FORMAT_MONTH_SHORT));
                        compiledPattern.add(new Token(FORMAT_STRING, "-"));
                        compiledPattern.add(new Token(FORMAT_YEAR_LONG));
                        break;
                    case 'V':
                        compiledPattern.add(new Token(FORMAT_WEEK_WEEKYEAR));
                        break;
                    case 'W':
                        compiledPattern.add(new Token(FORMAT_WEEK_YEAR_M));
                        break;
                    case 'w':
                        compiledPattern.add(new Token(FORMAT_DAY_WEEK));
                        break;
                    case 'X':
                        compiledPattern.add(new Token(FORMAT_HOUR));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_MINUTES));
                        compiledPattern.add(new Token(FORMAT_STRING, ":"));
                        compiledPattern.add(new Token(FORMAT_SECONDS));
                        break;
                    case 'x':
                        compiledPattern.add(new Token(FORMAT_MONTH));
                        compiledPattern.add(new Token(FORMAT_STRING, "/"));
                        compiledPattern.add(new Token(FORMAT_DAY));
                        compiledPattern.add(new Token(FORMAT_STRING, "/"));
                        compiledPattern.add(new Token(FORMAT_YEAR_SHORT));
                        break;
                    case 'Y':
                        compiledPattern.add(new Token(FORMAT_YEAR_LONG));
                        break;
                    case 'y':
                        compiledPattern.add(new Token(FORMAT_YEAR_SHORT));
                        break;
                    case 'Z':
                        compiledPattern.add(new Token(FORMAT_ZONE_ID));
                        break;
                    case 'z':
                        compiledPattern.add(new Token(FORMAT_COLON_ZONE_OFF));
                        break;
                    case '%':
                        compiledPattern.add(new Token(FORMAT_STRING, "%"));
                        break;
                    default:
                        compiledPattern.add(new Token(FORMAT_STRING, "%" + pattern.charAt(i)));
                    }
                    i++;
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (;i < len && pattern.charAt(i) != '%'; i++) {
                    sb.append(pattern.charAt(i));
                }
                compiledPattern.add(new Token(FORMAT_STRING, sb.toString()));
            }
        }
    }

    private int addOutputFormatter(String pattern, int index) {
        TimeOutputFormatter outputFormatter = TimeOutputFormatter.getFormatter(pattern.substring(index - 1));
        if (outputFormatter != null) {
            index += outputFormatter.getFormat().length();
            compiledPattern.add(new Token(FORMAT_OUTPUT, outputFormatter));
        }
        return index;
    }

    private DateTime dt;
    private long nsec;

    public void setDateTime(final DateTime dt) {
        this.dt = dt;
    }

    public void setNSec(long nsec) {
        this.nsec = nsec;
    }

    static enum FieldType {
        NUMERIC('0', 0),
        NUMERIC2('0', 2),
        NUMERIC2BLANK(' ', 2),
        NUMERIC3('0', 3),
        NUMERIC4('0', 4),
        TEXT(' ', 0);

        char defaultPadder;
        int defaultWidth;
        FieldType(char padder, int width) {
            defaultPadder = padder;
            defaultWidth = width;
        }
    }

    /**
     * @see DateFormat#format(Date, StringBuffer, FieldPosition)
     */
    public StringBuffer format(Date ignored, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        TimeOutputFormatter formatter = TimeOutputFormatter.DEFAULT_FORMATTER;

        for (Token token: compiledPattern) {
            String output = null;
            long value = 0;
            FieldType type = TEXT;

            switch (token.getFormat()) {
                case FORMAT_OUTPUT:
                    formatter = (TimeOutputFormatter) token.getData();
                    continue; // go to next token
                case FORMAT_STRING:
                    output = token.getData().toString();
                    break;
                case FORMAT_WEEK_LONG:
                    // This is GROSS, but Java API's aren't ISO 8601 compliant at all
                    int v = (dt.getDayOfWeek()+1)%8;
                    if(v == 0) {
                        v++;
                    }
                    output = formatSymbols.getWeekdays()[v];
                    break;
                case FORMAT_WEEK_SHORT:
                    // This is GROSS, but Java API's aren't ISO 8601 compliant at all
                    v = (dt.getDayOfWeek()+1)%8;
                    if(v == 0) {
                        v++;
                    }
                    output = formatSymbols.getShortWeekdays()[v];
                    break;
                case FORMAT_MONTH_LONG:
                    output = formatSymbols.getMonths()[dt.getMonthOfYear()-1];
                    break;
                case FORMAT_MONTH_SHORT:
                    output = formatSymbols.getShortMonths()[dt.getMonthOfYear()-1];
                    break;
                case FORMAT_DAY:
                    type = NUMERIC2;
                    value = dt.getDayOfMonth();
                    break;
                case FORMAT_DAY_S:
                    type = NUMERIC2BLANK;
                    value = dt.getDayOfMonth();
                    break;
                case FORMAT_HOUR:
                    type = NUMERIC2;
                    value = dt.getHourOfDay();
                    break;
                case FORMAT_HOUR_BLANK:
                    type = NUMERIC2BLANK;
                    value = dt.getHourOfDay();
                    break;
                case FORMAT_HOUR_M:
                case FORMAT_HOUR_S:
                    value = dt.getHourOfDay();
                    if (value == 0)
                        value = 12;
                    else if (value > 12)
                        value -= 12;

                    if (token.getFormat() == FORMAT_HOUR_M)
                        type = NUMERIC2;
                    else
                        type = NUMERIC2BLANK;
                    break;
                case FORMAT_DAY_YEAR:
                    type = NUMERIC3;
                    value = dt.getDayOfYear();
                    break;
                case FORMAT_MINUTES:
                    type = NUMERIC2;
                    value = dt.getMinuteOfHour();
                    break;
                case FORMAT_MONTH:
                    type = NUMERIC2;
                    value = dt.getMonthOfYear();
                    break;
                case FORMAT_MERIDIAN:
                    output = dt.getHourOfDay() < 12 ? "AM" : "PM";
                    break;
                case FORMAT_MERIDIAN_LOWER_CASE:
                    output = dt.getHourOfDay() < 12 ? "am" : "pm";
                    break;
                case FORMAT_SECONDS:
                    type = NUMERIC2;
                    value = dt.getSecondOfMinute();
                    break;
                case FORMAT_WEEK_YEAR_M:
                    type = NUMERIC2;
                    value = formatWeekYear(java.util.Calendar.MONDAY);
                    break;
                case FORMAT_WEEK_YEAR_S:
                    type = NUMERIC2;
                    value = formatWeekYear(java.util.Calendar.SUNDAY);
                    break;
                case FORMAT_DAY_WEEK:
                    type = NUMERIC;
                    value = dt.getDayOfWeek() % 7;
                    break;
                case FORMAT_DAY_WEEK2:
                    type = NUMERIC;
                    value = dt.getDayOfWeek();
                    break;
                case FORMAT_YEAR_LONG:
                    type = NUMERIC4;
                    value = dt.getYear();
                    break;
                case FORMAT_YEAR_SHORT:
                    type = NUMERIC2;
                    value = dt.getYear() % 100;
                    break;
                case FORMAT_COLON_ZONE_OFF:
                    // custom logic because this is so weird
                    value = dt.getZone().getOffset(dt.getMillis()) / 1000;
                    int colons = formatter.getNumberOfColons();
                    output = formatZone(colons, (int) value, formatter);
                    break;
                case FORMAT_ZONE_ID:
                    output = dt.getZone().getShortName(dt.getMillis());
                    break;
                case FORMAT_CENTURY:
                    type = NUMERIC;
                    value = dt.getCenturyOfEra();
                    break;
                case FORMAT_MILLISEC:
                    type = NUMERIC3;
                    value = dt.getMillisOfSecond();
                    break;
                case FORMAT_EPOCH:
                    type = NUMERIC;
                    value = dt.getMillis() / 1000;
                    break;
                case FORMAT_WEEK_WEEKYEAR:
                    type = NUMERIC2;
                    value = dt.getWeekOfWeekyear();
                    break;
                case FORMAT_NANOSEC:
                    value = dt.getMillisOfSecond() * 1000000;
                    if (ruby_1_9) value += nsec;
                    int width = ruby_1_9 ? 9 : 3;
                    if (formatter.width > 0)
                        width = formatter.width;
                    output = TimeOutputFormatter.formatNumber(value, 9, '0');
                    if (width < 9) {
                        output = output.substring(0, width);
                    } else {
                        while(output.length() < width)
                            output += "0";
                    }
                    formatter = TimeOutputFormatter.DEFAULT_FORMATTER; // no more formatting
                    break;
                case FORMAT_WEEKYEAR:
                    type = NUMERIC4;
                    value = dt.getWeekyear();
                    break;
            }

            output = formatter.format(output, value, type);
            // reset formatter
            formatter = TimeOutputFormatter.DEFAULT_FORMATTER;
            toAppendTo.append(output);
        }

        return toAppendTo;
    }

    private int formatWeekYear(int firstDayOfWeek) {
        java.util.Calendar dtCalendar = dt.toGregorianCalendar();
        dtCalendar.setFirstDayOfWeek(firstDayOfWeek);
        dtCalendar.setMinimalDaysInFirstWeek(7);
        int value = dtCalendar.get(java.util.Calendar.WEEK_OF_YEAR);
        if ((value == 52 || value == 53) &&
                (dtCalendar.get(Calendar.MONTH) == Calendar.JANUARY )) {
            // MRI behavior: Week values are monotonous.
            // So, weeks that effectively belong to previous year,
            // will get the value of 0, not 52 or 53, as in Java.
            value = 0;
        }
        return value;
    }

    private String formatZone(int colons, int value, TimeOutputFormatter formatter) {
        int seconds = Math.abs(value);
        int hours = seconds / 3600;
        seconds %= 3600;
        int minutes = seconds / 60;
        seconds %= 60;

        if (value < 0 && hours != 0) // see below when hours == 0
            hours = -hours;

        String mm = TimeOutputFormatter.formatNumber(minutes, 2, '0');
        String ss = TimeOutputFormatter.formatNumber(seconds, 2, '0');

        char padder = formatter.getPadder('0');
        int defaultWidth = -1;
        String after = null;

        switch (colons) {
            case 0: // %z -> +hhmm
                defaultWidth = 5;
                after = mm;
                break;
            case 1: // %:z -> +hh:mm
                defaultWidth = 6;
                after = ":" + mm;
                break;
            case 2: // %::z -> +hh:mm:ss
                defaultWidth = 9;
                after = ":" + mm + ":" + ss;
                break;
            case 3: // %:::z -> +hh[:mm[:ss]]
                if (minutes == 0) {
                    if (seconds == 0) { // +hh
                        defaultWidth = 3;
                        after = "";
                    } else { // +hh:mm
                        return formatZone(1, value, formatter);
                    }
                } else { // +hh:mm:ss
                    return formatZone(2, value, formatter);
                }
                break;
        }

        int minWidth = defaultWidth - 1;
        int width = formatter.getWidth(defaultWidth);
        if (width < minWidth)
            width = minWidth;
        width -= after.length();
        String before = TimeOutputFormatter.formatSignedNumber(hours, width, padder);

        if (value < 0 && hours == 0) // the formatter could not handle this case
            before = before.replace('+', '-');
        return before + after;
    }

    /**
     * @see DateFormat#parse(String, ParsePosition)
     */
    public Date parse(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }
}
