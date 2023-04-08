package com.corporate.online.learning.platform.utils;

import com.corporate.online.learning.platform.exception.report.ReportCheckDateIntervalException;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckIntervalUtils {

    public static boolean checkNumberInInterval(Integer low, Integer high, Integer number) {
        if (ObjectUtils.isEmpty(low) && !ObjectUtils.isEmpty(high)) {
            return number <= high;
        }
        if (!ObjectUtils.isEmpty(low) && ObjectUtils.isEmpty(high)) {
            return number >= low;
        }
        if (!ObjectUtils.isEmpty(low) && !ObjectUtils.isEmpty(high)) {
            return number <= high && number >= low;
        }
        return true;
    }

    public static boolean checkNumberInInterval(Long low, Long high, Long number) {
        if (ObjectUtils.isEmpty(low) && !ObjectUtils.isEmpty(high)) {
            return number <= high;
        }
        if (!ObjectUtils.isEmpty(low) && ObjectUtils.isEmpty(high)) {
            return number >= low;
        }
        if (!ObjectUtils.isEmpty(low) && !ObjectUtils.isEmpty(high)) {
            return number <= high && number >= low;
        }
        return true;
    }

    public static boolean checkNumberInInterval(Float low, Float high, Float number) {
        if (ObjectUtils.isEmpty(low) && !ObjectUtils.isEmpty(high)) {
            return number <= high;
        }
        if (!ObjectUtils.isEmpty(low) && ObjectUtils.isEmpty(high)) {
            return number >= low;
        }
        if (!ObjectUtils.isEmpty(low) && !ObjectUtils.isEmpty(high)) {
            return number <= high && number >= low;
        }
        return true;
    }

    public static boolean checkDateInInterval(String earliest, String latest, Timestamp timestamp, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date earliestDate, latestDate;
        Timestamp earlyTimestamp = null, lateTimeStamp = null;

        if (!ObjectUtils.isEmpty(earliest)) {
            try {
                earliestDate = dateFormat.parse(earliest);
            } catch (ParseException e) {
                throw new ReportCheckDateIntervalException("[Report Check Date Error] Earliest date parsing failed.");
            }
            assert earliestDate != null;
            earlyTimestamp = new Timestamp(earliestDate.getTime());
        }
        if (!ObjectUtils.isEmpty(latest)) {
            try {
                latestDate = dateFormat.parse(latest);
            } catch (ParseException e) {
                throw new ReportCheckDateIntervalException("[Report Check Date Error] Latest date parsing failed.");
            }
            assert latestDate != null;
            lateTimeStamp = new Timestamp(latestDate.getTime());
        }

        if (ObjectUtils.isEmpty(earliest) && !ObjectUtils.isEmpty(latest)) {
            return timestamp.before(lateTimeStamp);
        }
        if (!ObjectUtils.isEmpty(earliest) && ObjectUtils.isEmpty(latest)) {
            return timestamp.after(earlyTimestamp);
        }
        if (!ObjectUtils.isEmpty(earliest) && !ObjectUtils.isEmpty(latest)) {
            return timestamp.before(lateTimeStamp) && timestamp.after(earlyTimestamp);
        }
        return true;
    }
}
