/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jennings.mvnsat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author david
 */
public class Sats {

    private static HashMap<String, Sat> satNames = null;
    private static HashMap<String, Sat> satNums = null;
    private static HashMap<String, String> satTLEs = null;

    private String loadSats() {
        satNames = new HashMap<>();
        satNums = new HashMap<>();
        satTLEs = new HashMap<>();

        String message = "";

        try {
            //InputStream pis = getClass().getResourceAsStream("/sats.tle");
            //BufferedReader br = new BufferedReader(new InputStreamReader(pis, "UTF-8"));
            FileReader fr = new FileReader("sats.tle");
            BufferedReader br = new BufferedReader(fr);


            String tleHeader;
            while ((tleHeader = br.readLine()) != null) {
                String tleLine1 = br.readLine();
                String tleLine2 = br.readLine();
                Sat sat = new Sat(tleHeader, tleLine1, tleLine2);

                satNames.put(sat.getName(), sat);
                satNums.put(sat.getNum(), sat);
                satTLEs.put(sat.getNum(), tleHeader + "\n" + tleLine1 + "\n" + tleLine2);

            }

        } catch (Exception e) {
            message = "ERROR" + e.getClass() + ">>" + e.getMessage();
            System.out.println(message);
        }
        return message;
    }

    public boolean isSatNums() {

        if (satNums == null) {
            return false;
        } else {
            return true;
        }
    }

    public Sats() {
        try {
            loadSats();
        } catch (Exception e) {
            satNums = null;
            e.printStackTrace();
        }
    }

    public String getSatTLE(String num) {
        return satTLEs.get(num);
    }

    public Sat getSatNum(String num) {
        return satNums.get(num);
    }

    public Sat getSatName(String name) {
        return satNames.get(name);
    }

    /**
     * Input could be specific names or names ending with * Each name is check
     * and appended to the list
     *
     * @param name Comma Sep String of Names
     * @return
     */
    public HashSet<String> getSatsByName(String strNames) throws Exception {
        HashSet<String> sats = new HashSet<>();

        String[] names = strNames.split(",");
        String message = "";

        for (String name : names) {

            String nm = name.trim();
            if (nm.endsWith("*")) {
                String prefix = nm.substring(0, nm.length() - 1).toUpperCase();
                if (prefix.equalsIgnoreCase("")) {
                    // One of the values was * return all 
                    return getAllNums();
                } else {
                    for (String satNm : getAllNames()) {
                        if (satNm.toUpperCase().startsWith(prefix)) {
                            Sat s = satNames.get(satNm);
                            sats.add(s.getNum());
                        }
                    }
                }

            } else {

                Sat s = satNames.get(name);
                if (s == null) {
                    message += name + ",";
                } else {
                    sats.add(s.getNum());
                }
            }
        }
        if (!message.equalsIgnoreCase("")) {
            throw new Exception("Sats not found: " + message.substring(0, message.length() - 1));
        }

        return sats;

    }

    /**
     * Input is comma sep list of nums
     *
     * @param nums
     * @return
     */
    public HashSet<String> getSatsByNum(String strNums) throws Exception {
        HashSet<String> sats = new HashSet<>();

        String[] nums = strNums.split(",");
        String message = "";

        for (String num : nums) {
            Sat s = satNums.get(num);
            if (s == null) {
                message += num + ",";
            } else {
                sats.add(s.getNum());
            }

        }
        if (!message.equalsIgnoreCase("")) {
            throw new Exception("Sats not found: " + message.substring(0, message.length() - 1));
        }

        return sats;

    }

    public HashSet<String> getAllNames() {
        HashSet<String> sats = new HashSet<>();

        satNames.keySet().stream().forEach((s) -> {
            sats.add(s);
        });
        return sats;
    }

    public HashSet<String> getAllNums() {
        HashSet<String> sats = new HashSet<>();

        satNums.keySet().stream().forEach((s) -> {
            sats.add(s);
        });

        return sats;

    }


    private void createSimulationFile(int duration, int step, String fmt) {
        try {
            FileWriter fw = null;

            if (fmt.equalsIgnoreCase("geojson")) {
                fw = new FileWriter("satellites.json");
            } else {
                fw = new FileWriter("satellites.txt");
            }


            long st = System.currentTimeMillis();

            int durationSecs = duration;
            int stepSecs = step;
            HashSet<String> sats = new HashSet<>();
            sats = getAllNums();


            String strDel = ",";

            JSONArray results = new JSONArray();

            long t = st;
            while (t < (st + durationSecs * 1000)) {
                t += stepSecs * 1000;
                
                if (fmt.equalsIgnoreCase("geojson")) {

                    for (String sat : sats) {
                        Sat pos = getSatNum(sat).getPos(t);
                        JSONObject result = new JSONObject();
                        result.put("type", "Feature");

                        JSONObject properties = new JSONObject();
                        properties.put("name", pos.getName());
                        properties.put("num", pos.getNum());
                        properties.put("timestamp", pos.GetEpoch().epochTimeMillis());
                        properties.put("dtg", pos.GetEpoch());
                        properties.put("lon", pos.GetLon());
                        properties.put("lat", pos.GetParametricLat());
                        properties.put("alt", pos.getAltitude());
                        result.put("properties", properties);

                        JSONObject geom = new JSONObject();
                        geom.put("type", "Point");
                        JSONArray coord = new JSONArray("[" + pos.GetLon() + ", " + pos.GetParametricLat() + "]");
                        geom.put("coordinates", coord);
                        result.put("geometry", geom);

                        results.put(result);

                    }

                } else {

                    for (String sat : sats) {
                        Sat pos = getSatNum(sat).getPos(t);
                        String line = pos.getName() + strDel + pos.getNum() + strDel + pos.GetEpoch().epochTimeMillis()
                                + strDel + pos.GetEpoch() + strDel + pos.GetLon() + strDel + pos.GetParametricLat()
                                + strDel + pos.getAltitude() + "\n";
                        fw.write(line);
                    }
                }
            }

            if (fmt.equalsIgnoreCase("geojson")) {

                JSONObject featureCollection = new JSONObject();
                featureCollection.put("type", "FeatureCollection");

                featureCollection.put("features", results);
                fw.write(featureCollection.toString());
            }

            

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Sats t = new Sats();

        
//        t.createSimulationFile(1000, 1, "geojson");

        if (args.length != 2 && args.length != 3) {
            System.err.print("Usage: Sats <durationSecs> <stepSecs> (<format>)\n");
        } else if (args.length == 2) {
            t.createSimulationFile(Integer.parseInt(args[0]), Integer.parseInt(args[1]), "");
        } else {
            t.createSimulationFile(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
        }


    }

}
