package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Log {

    static Logger logger = Logger.getLogger(Log.class.getName());
    
    public static void logInfo(String msg) {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("./logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
        logger.setLevel(Level.FINE);
        logger.addHandler(new LogHandler());
        
        try {
            //FileHandler file name with max size and number of log files limit
            Handler fileHandler = new FileHandler("./Module_Updater.log", true);
            fileHandler.setFormatter(new LogFormatter());
            //setting custom filter for FileHandler
            fileHandler.setFilter(new LogFilter());
            logger.addHandler(fileHandler);
            String printMsg = "  " + msg; //avoid reassigning parameters
            logger.log(Level.INFO, printMsg); 
            logger.log(Level.CONFIG, "Config data");
            fileHandler.close();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}