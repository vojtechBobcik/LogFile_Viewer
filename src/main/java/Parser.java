import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    //Pattern Pri = Pattern.compile("^\\<\\d+\\>");

    private String[][] logs = new String[3][];
    int argModel;
    HtmlFileWriter fw = new HtmlFileWriter();

    /**
     * split log in two steps into a header part and log data part.
     * I dont know wich data i will get in header.
     * Thats the reason for using method CreateHeaderFromParts.
     * Next I dont know if in log will be only Sdata (Structured Data) or only message or both.
     * And how many arrays will be in Sdata.
     *
     */

    public void logParser(String filePath, String specifiedTime) {
        String data;
        String[] headerParts;
        String isoTimeStamp;
        String sDataAndMsgPart;
        String[] sDataAndMsg;

        try {
            File myObj = new File("src/main/log.txt");
            Scanner myReader = new Scanner(myObj);
            fw.PrepareHTMLFile();
            while (myReader.hasNextLine()) {
                data = myReader.nextLine();
                headerParts = data.split("\\s", 7);
                isoTimeStamp = headerParts[1];
                sDataAndMsgPart = headerParts[headerParts.length - 1];

                if (checkLine(isoTimeStamp, specifiedTime)) {
                    sDataAndMsg = parseDataPart(sDataAndMsgPart);
                    fw.htmlWriteLine(
                            createHeaderFromParts(headerParts),
                            sDataAndMsg[0],
                            sDataAndMsg[1]);
                }
            }

            fw.htmlFileEnd();
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred. File not found.");
            e.printStackTrace();
        }
    }

    String createHeaderFromParts(String[] parts) {
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            header.append(parts[i]);
            header.append(" ");
        }
        return header.toString();
    }

    String[] parseDataPart(String parts) {
        String[] SdataAndMsg = {"", ""};
        if (hasSdata(parts)) {
            Pattern SdataPattern = Pattern.compile("\\[.*\\]");
            Matcher match = SdataPattern.matcher(parts);
            if (match.find()) {
                SdataAndMsg[0] = match.group(0);
                String[] message = parts.split("\\[.*\\]", 2);
                SdataAndMsg[1] = message[1];
            }
        } else {
            SdataAndMsg[1] = parts;
        }
        return SdataAndMsg;
    }

    // check if log has data part. if it has provided string starts with [
    boolean hasSdata(String dataPart) {
        char startsWith = dataPart.charAt(0);
        return startsWith == '[';
    }

    // Simplified just compare parts of strings together.
    // Better to use java date functions.
    // solved ZonedDateTime used
    /**
     * @param logTime       time of each line in log file
     * @param specifiedTime time we want to get from log
     * @return if log time equals specified time
     * <p>
     * argModel presets. Method getArgModel gives specified number output
     * 1 - month - yyyy-MM
     * 2 - day --- yyyy-MM-dd
     * 3 - time -- hh:mm:ss
     * 4 - interval of month yyyy-MM - yyyy-MM
     * 5 - interval of day yyyy-MM-dd - yyyy-MM-dd
     * 6 - interval of time hh:mm:ss - hh:mm:ss
     * 7 - interval of zoned hh:mm:ss - hh:mm:ss
     */
    boolean checkLine(String logTime, String specifiedTime) {
        argModel = getArgumentModelSet(specifiedTime);
        ZonedDateTime zdt_logTime = ZonedDateTime.parse(logTime);
        String[] inter = specifiedTime.split("\\s\\-\\s",3);
        switch (argModel) {
            case 1: //yyyy-MM
                YearMonth ym = YearMonth.parse(specifiedTime);
                YearMonth comparison = YearMonth.of(zdt_logTime.getYear(),zdt_logTime.getMonth());
                return ym.equals(comparison);
            case 2: //yyyy-MM-dd
                LocalDate ld_specified = LocalDate.parse(specifiedTime);
                return (zdt_logTime.toLocalDate().equals(ld_specified));
            case 3: //HH:mm:ss
                /**
                 * variable comp is zonedDataTime converted to Localtime
                 * if nanosecs are in a value and we are comparing time only with hours:mins:secs
                 * in that case visibly same time are not evaluated as same. we need to use TruncateTo
                 * to take only part of time we need
                 * example
                 * we want log with time 20:30:15 (argument)
                 * log line from file with time 20:30:15 is not equal to argument real value is 20:30:15:003
                 */

                LocalTime lt_specified = LocalTime.parse(specifiedTime);
                LocalTime comp = zdt_logTime.toLocalTime();
                boolean ret = lt_specified.equals(comp.truncatedTo(ChronoUnit.SECONDS));
                return ret;

            case 4: // month interval
                return timeInIntervalMonth(logTime,inter[0],inter[1]);
            case 5: // day interval
                return timeInIntervalDay(logTime,inter[0],inter[1]);
            case 6: // time interval
                return timeInIntervalTime(logTime, inter[0],inter[1]);
            default:
                System.err.println("error in evaluating argument");
                return false;
        }
    }

    /**
     * @param timeArgument
     * @return predefined value used to control behavior of parser.
     * <p>
     * method will judge argument with predefined regex to get information about user input.
     */
    int getArgumentModelSet(String timeArgument) {
        Pattern month = Pattern.compile("^(\\d){4}\\-(\\d){2}$");
        Pattern day = Pattern.compile("^(\\d){4}\\-(\\d){2}\\-(\\d){2}$");
        Pattern hour = Pattern.compile("^\\d\\d\\:\\d\\d\\:\\d\\d$");
        Pattern intervalMonth = Pattern.compile("^(\\d){4}\\-(\\d){2}\\s\\-\\s(\\d){4}\\-(\\d){2}$");
        Pattern intervalDay = Pattern.compile("^(\\d){4}\\-(\\d){2}\\-(\\d){2}\\s\\-\\s(\\d){4}\\-(\\d){2}\\-(\\d){2}$");
        Pattern intervalTime = Pattern.compile("^\\d\\d(\\:\\d\\d){2}\\s\\-\\s\\d\\d(\\:\\d\\d){2}$");


        Matcher m_month = month.matcher(timeArgument);
        Matcher m_day = day.matcher(timeArgument);
        Matcher m_hour = hour.matcher(timeArgument);
        Matcher m_intervalMonth = intervalMonth.matcher(timeArgument);
        Matcher m_intervalDay = intervalDay.matcher(timeArgument);
        Matcher m_intervalTime = intervalTime.matcher(timeArgument);


        if (m_month.find()) {
            return 1;
        } else if (m_day.find()) {
            return 2;
        } else if (m_hour.find()) {
            return 3;
        } else if (m_intervalMonth.find()) {
            return 4;
        } else if (m_intervalDay.find()) {
            return 5;
        } else if (m_intervalTime.find()) {
            return 6;
        } else {
            System.out.println("Wrong Argument !!!");
            return 0;
        }
    }

    /**
     * @param logTime time in one log line
     * @param intervalStart
     * @param intervalEnd
     * @return true if log time between intervalStart and intervalEnd
     */
    boolean timeInIntervalMonth(String logTime, String intervalStart, String intervalEnd) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(logTime);

            YearMonth timestamp = YearMonth.of(zdt.getYear(), zdt.getMonth());
            YearMonth start = YearMonth.parse(intervalStart);
            YearMonth end = YearMonth.parse(intervalEnd);

            return (timestamp.isAfter(start) && timestamp.isBefore(end));

        } catch (DateTimeParseException dtpe) {
            System.err.println(dtpe);
            return false;
        }

    }
    //copy code:1
    boolean timeInIntervalDay(String logTime, String intervalStart, String intervalEnd) {
        try {
            ZonedDateTime timestamp = ZonedDateTime.parse(logTime);
            LocalDate start = LocalDate.parse(intervalStart);
            LocalDate end = LocalDate.parse(intervalEnd);

            if (timestamp.toLocalDate().isAfter(start) && timestamp.toLocalDate().isBefore(end)) {
                return true;
            } else {
                return false;
            }

        } catch (DateTimeParseException dtpe) {
            System.err.println(dtpe);
            return false;
        }

    }
    //copy code:2
    boolean timeInIntervalTime(String logTime, String intervalStart, String intervalEnd) {
        try {

            ZonedDateTime timestamp = ZonedDateTime.parse(logTime);
            LocalTime lc_timestamp= timestamp.toLocalTime().truncatedTo(ChronoUnit.MINUTES);


            LocalTime start = LocalTime.parse(intervalStart);
            LocalTime end = LocalTime.parse(intervalEnd);

            if (lc_timestamp.isAfter(start) &&
                    lc_timestamp.isBefore(end)) {
                return true;
            } else {
                return false;
            }

        } catch (DateTimeParseException dtpe) {
            System.err.println(dtpe);
            return false;
        }

    }

}
