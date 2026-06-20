/*
package dataStorage;

import apiClasses.TimetableResponse.Timetable.Route.StationInterval;
import apiClasses.TimetableResponse.Timetable.Route.StationInterval.Interval;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class CsvHandler {
    public void writeStoppingPattern(String lineId, String originator, String destination, int routeId, int stoppingPatternId, StationInterval stationInterval) {
        Interval[] intervals = stationInterval.getIntervals();
        ArrayList<String[]> csvList = new ArrayList<>();
        String[] workingArray = new String[2];

        for (Interval interval : intervals) {
            System.out.println(interval.getStopId());
            workingArray[0] = interval.getStopId();
            workingArray[1] = String.valueOf(interval.getTimeToArrival());
            System.out.println(Arrays.toString(workingArray));
            csvList.add(workingArray);
        }

        String filename = String.format("pattern_%s_%s_%s_%d_%d.csv", lineId, originator, destination, routeId, stoppingPatternId);

        for (String[] item : csvList) {
            System.out.println(Arrays.toString(item));
        }

        writeCsvArray(filename, csvList);
    }
    public void writeCsvArray(String filename, ArrayList<String[]> csvList) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeAll(csvList);
        } catch (IOException e) {
            System.out.println("An IOError occurred when trying to write a CSV file called " + filename);
            e.printStackTrace();
        }
    }
}
 */