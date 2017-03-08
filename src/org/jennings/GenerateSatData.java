
package org.jennings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

/**
 *  This create satellite dat file for all satellites in the predict.tle file.
 * 
 *  You can adjust to get samples at a different rate.
 * 
 *  The predict.tle was a file where I combined about 230 TLE's into one file.
 * 
 * @author david
 */
public class GenerateSatData {

    public void run() {
        try {

            FileWriter fw = new FileWriter("satellites2.dat");
            
            HashMap<String, Sat> sats = new HashMap<>();

            BufferedReader br = new BufferedReader(new FileReader("predict.tle"));
            String tleHeader = null;
            while ((tleHeader = br.readLine()) != null) {
                String tleLine1 = br.readLine();
                String tleLine2 = br.readLine();
                Sat sat = new Sat(tleHeader, tleLine1, tleLine2);
                
                sats.put(sat.getName(), sat);

            }
            
            long t = System.currentTimeMillis();
            
            for (int n=0; n<300; n++ ) {
                for (String sat: sats.keySet()) {
                    //System.out.println(sat);
                    Sat pos = sats.get(sat).getPos(t);
                    String strLine = pos.getName() + "|" + pos.GetEpoch() + "|"
                                + pos.GetLon() + "|" + pos.GetParametricLat()
                                + "|" + pos.getAltitude();
                    fw.write(strLine + "\n");
                }
                t += 1000;
                
                
            }
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        GenerateSatData t = new GenerateSatData();

        t.run();
    }

}
