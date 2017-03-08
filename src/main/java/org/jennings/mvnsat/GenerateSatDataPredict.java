
package org.jennings.mvnsat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author david
 */
public class GenerateSatDataPredict {

    public void iss_data() {
        try {
            FileWriter fout = new FileWriter("issdata.txt");

            String message = "GET_SAT ISS";
            byte[] sendData = message.getBytes();

            InetAddress address = InetAddress.getByName("localhost");

            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, 1210);

            DatagramSocket datagramSocket = new DatagramSocket();

            int i = 0;
            while (i < 86400) {
                datagramSocket.send(packet);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                datagramSocket.receive(receivePacket);

                String line = new String(receivePacket.getData());
                line = line.substring(0, receivePacket.getLength() - 1);

                line = line.replace('\n', ',');
                fout.write(System.currentTimeMillis() + "," + line + "\n");

                if (i % 1000 == 0) {
                    fout.flush();
                }
                Thread.sleep(1000);

            }
            fout.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        /*
        Download a bunch of TLE files from https://www.celestrak.com/NORAD/documentation/tle-fmt.asp
        I downloaded GPS satellites, Space Stations, and Earth Resource satellites (about 250 satellites)
        
        Split them into files with 24 satellites each.  That is the most predict server can provide per server. http://iss.astroviewer.net/
        
        Ran several instances of predict 
        predict -n 1201 -t predict1.tle -s
        predict -n 1202 -t predict2.tle -s
        ...
        predict -n 1210 -t predict2.tle -s
        
        I had to open each in its own shell.  Argh!!!
        
        My satellite program has issues.  The coordinates were off the ones from the web.
        Note the one we use in the example is also off.  Predict matches this web site
        
        */
        
        
        try {
            FileReader fr = new FileReader("predict.tle");
            BufferedReader br = new BufferedReader(fr);

            ArrayList<String> satellites = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("1 ")) {
                    satellites.add(line.substring(2,8));                    
                } 
            }

            for (String satellite: satellites) {
                System.out.println(satellite);
            }
            
            
            
            String baseFilePrefix = "satellite_data";
            int baseFileIndex = 0;

            InetAddress address = InetAddress.getByName("localhost");

            FileWriter fout = null;
            
            int i = 0;
            while (i < 15000) {
                
                if (i % 300 == 0) {
                    try {
                        fout.close();
                    } catch (Exception e) {
                        // Ok to ignore
                    }                    
                    baseFileIndex += 1;
                    System.out.println("baseFileIndex: " + baseFileIndex);
                    fout = new FileWriter("satellite_data" + String.valueOf(baseFileIndex) + ".txt");                    
                }
                
                
                i += 1;
                System.out.println(i);
                int k = 0;
                int prt = 0;
                for (String satellite : satellites) {                                        
                    
                    
                    if (k % 24 == 0) {
                        prt += 1;
                    }
                    
                    k += 1;
                    
                    String message = "GET_SAT " + satellite;
                    //System.out.println(message);
                    
                    
                    
                    byte[] sendData = message.getBytes();
                    
                    DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, 1200 + prt);

                    DatagramSocket datagramSocket = new DatagramSocket();

                    datagramSocket.send(packet);

                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    datagramSocket.receive(receivePacket);

                    line = new String(receivePacket.getData());
                    line = line.substring(0, receivePacket.getLength() - 1);

                    
                    String[] parts = line.split("\n");
                    String outline = String.valueOf(System.currentTimeMillis());
                    for (String part: parts) {           
                        // Get rid of pluses and trim
                        outline += "," + part.replace('+',' ').trim();
                    }
                    
                    fout.write(outline + "\n");
                    
                    datagramSocket.close();
//
//                    if (i % 10000 == 0) {
//                        fout.flush();
//                    }
                    
                }
                Thread.sleep(1000);
            }
            fout.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
