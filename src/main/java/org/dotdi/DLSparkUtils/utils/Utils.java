package org.dotdi.DLSparkUtils.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Utils {

    public static void saveToFile(Iterable<String> iterable, File file) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(file));
        for (String line : iterable)
             pw.println(line); // call toString() on club, like club.toString()
        pw.close();
    }
    
}
