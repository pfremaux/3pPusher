package logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class SimpleLogFormat extends Formatter {

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public SimpleLogFormat() {
        super();
    }

    @Override
    public String getHead(Handler h) {
        return super.getHead(h);
    }

    @Override
    public String getTail(Handler h) {
        return super.getTail(h);
    }

    @Override
    public synchronized String formatMessage(LogRecord record) {
        return super.formatMessage(record);
    }

    private String calcDate(long millisecs) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date resultdate = new Date(millisecs);
        return dateFormat.format(resultdate);
    }

    @Override
    public String format(LogRecord record) {
        StringBuffer buf = new StringBuffer(DATE_FORMAT.length() + 6 + 250);// date format length + log level + message

        buf.append(calcDate(record.getMillis()));
        buf.append(" ");
        buf.append(record.getLevel());
        buf.append(" ");
        buf.append(record.getMessage());
        buf.append("\n");
        return buf.toString();
    }
}
