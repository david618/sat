
package org.jennings.mvnsat;

/*
Tested agains ISS apps online.
https://api.wheretheiss.at/v1/satellites/25544/tles
https://api.wheretheiss.at/v1/satellites/25544


These gave a different position for ISS than the first.
http://iss.astroviewer.net/

http://www.qsl.net/kd2bd/predict.html
http://www.gano.name/shawn/JSatTrak/

After some investigation I discovered the TLE used for each was different.

I suspect the TLE used on wheretheiss.at is wrong.


*/


/**
 * Sat is initialized with Satellite Name Ephemeris data or TLE (https://www.celestrak.com/)
 * 
 * @author david
 */
public class Sat {

    final double pi = 3.141592654;
    final double DTOR = pi / 180;
    final double RTOD = 1 / DTOR;
    final double mu = 3.986005E5;
    final double K1 = 2.06474E14; // 3/2*J2*Re^2*sqrt(mu)*86400*RTOD
    final double RE = 6378.137;  // Equitorial Radius Earth km (a)
    final double RP = 6356.752314; // Polar radis km (b)     
    final double EarthEcc = Math.sqrt((1 - RP / RE));
    final double G = 6.67408E-11;  // Gravitation Constant m3 kg-1 s-2
    final double M = 5.972E24;  // kg;
    final double GM = G * M;

    private String name;
    private DateTime epoch;
    private KeplerElements kep;
    private double Nu;
    private double E;
    private Vector pos;
    private Vector vel;
    private char mode;
    private double MM;
    private double K2;
    private double Period;
    private double AnomalPeriod;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    
    
    /**
     * Calculate Keplerian elements given Cartesian
     */
    private void Cart2Kep() {
        Vector h = pos.cross(vel);
        Vector K = new Vector(0, 0, 1);
        Vector n = K.cross(h);
        Vector e = pos.times(1 / mu * (vel.dot(vel) - mu / pos.mag())).minus(vel.times(pos.dot(vel)));
        kep.setEcc(e.mag());

        double p = h.dot(h) / mu;
        kep.setSma(p / (1 - kep.getEcc() * kep.getEcc()));
        kep.setIncl(Math.acos(h.Z() / h.mag()) * RTOD);

        kep.setLan(Math.acos(n.X() / n.mag()) * RTOD);
        if (n.Y() < 0) {
            kep.setLan(360 - kep.getLan());
        }

        kep.setArg(Math.acos(n.dot(e) / (n.mag() * e.mag())) * RTOD);
        if (e.Z() < 0) {
            kep.setArg(360 - kep.getArg());
        }

        Nu = Math.acos(e.dot(pos) / (e.mag() * pos.mag()));
        if (pos.dot(vel) < 0) {
            Nu = 2 * pi - Nu;
        }
        double A = kep.getEcc() + Math.cos(Nu);
        double B = 1 + kep.getEcc() * Math.cos(Nu);
        E = Math.acos(A / B);
        if (pos.dot(vel) < 0) {
            E = 2 * pi - E;
        }
        kep.setXmo(E - kep.getEcc() * Math.sin(E));
        kep.setXmo(kep.getXmo() * RTOD);
        E = E * RTOD;
        Nu = Nu * RTOD;

    }

    /**
     * Calculate Cartesian coordinates given Keplerian elements
     */
    private void Kep2Cart() {
        double cosIncl = Math.cos(kep.getIncl() * DTOR);
        double sinIncl = Math.sin(kep.getIncl() * DTOR);
        double cosLan = Math.cos(kep.getLan() * DTOR);
        double sinLan = Math.sin(kep.getLan() * DTOR);
        double cosArg = Math.cos(kep.getArg() * DTOR);
        double sinArg = Math.sin(kep.getArg() * DTOR);
        double cosNu = Math.cos(Nu * DTOR);
        double sinNu = Math.sin(Nu * DTOR);
        double p = kep.getSma() * (1 - kep.getEcc() * kep.getEcc());
        double R = p / (1 + kep.getEcc() * cosNu);
        double Rp = R * cosNu;
        double Rq = R * sinNu;
        double no = Math.sqrt(mu / p);
        double Vp = no * (-sinNu);
        double Vq = no * (kep.getEcc() + cosNu);
        double R11 = cosLan * cosArg - sinLan * sinArg * cosIncl;
        double R12 = -cosLan * sinArg - sinLan * cosArg * cosIncl;
        double R21 = sinLan * cosArg + cosLan * sinArg * cosIncl;
        double R22 = -sinLan * sinArg + cosLan * cosArg * cosIncl;
        double R31 = sinArg * sinIncl;
        double R32 = cosArg * sinIncl;
        double x = Rp * R11 + Rq * R12;
        double y = Rp * R21 + Rq * R22;
        double z = Rp * R31 + Rq * R32;
        double vx = Vp * R11 + Vq * R12;
        double vy = Vp * R21 + Vq * R22;
        double vz = Vp * R31 + Vq * R32;
        pos = new Vector(x, y, z);
        vel = new Vector(vx, vy, vz);
    }

    /**
     * Calculate some constants to be used for this satellite
     */
    private void SetConsts() {
        K2 = 1.0 / (Math.sqrt(Math.pow(kep.getSma(), 7)) * Math.sqrt(1 - Math.pow(kep.getEcc(), 2)));
        double no = Math.sqrt(mu / Math.pow(kep.getSma(), 3));
        Period = 2 * pi * no;
        MM = no * 86400.0 * RTOD
                + K1 * K2 / (1 - Math.pow(kep.getEcc(), 2)) * (1 - 3.0 / 2.0 * Math.pow(Math.sin(kep.getIncl() * DTOR), 2));
        AnomalPeriod = 2 * pi / (MM / 86400.0 * DTOR);
    }

    /**
     * Don't recall the orbital mechanics behind this one.
     */
    private void FindAnomalies() {
        if (kep.getXmo() == 0) {
            Nu = E = 0;
        } else {
            double E1 = kep.getXmo() * DTOR;
            int Count = 0;
            do {
                E = E1;
                E1 = E + (kep.getXmo() * DTOR + kep.getEcc() * Math.sin(E) - E) / (1 - kep.getEcc() * Math.cos(E));
            } while (Math.abs(E1 - E) > 1E-8 && Count++ < 25);
            double sin_Nu = (Math.sqrt(1 - Math.pow(kep.getEcc(), 2)) * Math.sin(E)) / (1 - kep.getEcc() * Math.cos(E));
            double cos_Nu = (Math.cos(E) - kep.getEcc()) / (1 - kep.getEcc() * Math.cos(E));
            E = E * RTOD;
            Nu = Math.atan2(sin_Nu, cos_Nu) * RTOD;
            if (Nu < 0) {
                Nu += 360;
            }
        }
    }

    /**
     * Initialize given TLE
     * @param tleHeader
     * @param tleLine1
     * @param tleLine2 
     */
    public Sat(String tleHeader, String tleLine1, String tleLine2) {
        
        // Header
        this.name = tleHeader.substring(0, 24).trim();

        // Line 1
        int epochYear = Integer.parseInt(tleLine1.substring(18, 20));
        double epochFday = Double.parseDouble(tleLine1.substring(20, 32));               
        this.epoch = new DateTime(2000 + epochYear, epochFday);
        
        // Line 2
        double incl = Double.parseDouble(tleLine2.substring(8, 16));
        double raan = Double.parseDouble(tleLine2.substring(17, 25));
        double ecc = Double.parseDouble("." + tleLine2.substring(26, 33));
        double ap = Double.parseDouble(tleLine2.substring(34, 42));
        double ma = Double.parseDouble(tleLine2.substring(43, 51));
        double mm = Double.parseDouble(tleLine2.substring(52, 63));

        // Convert Mean Motion to Radians/Sec
        mm = mm * Math.PI * 2 / 86400;  // radians/sec
        
        double a = Math.cbrt(this.GM / mm / mm);  // meters
        a = a / 1000.0;   // convert meter to km

        // Create Kep
        this.kep = new KeplerElements(a,incl,ecc,raan,ap,ma);
        this.mode = 'K';
        SetConsts();
        FindAnomalies();
        Kep2Cart();        

    }

    /**
     * Initialize Given position and velocity vectors
     * @param name
     * @param p
     * @param v
     * @param t 
     */
    public Sat(String name, Vector p, Vector v, DateTime t) {
        this.name = name.trim();
        this.epoch = t;
        this.pos = p;
        this.vel = v;
        this.mode = 'C';
        Cart2Kep();
        SetConsts();
        FindAnomalies();
    }

    /**
     * Initialized Given Keplerian Elements
     * @param name
     * @param a
     * @param t 
     */
    public Sat(String name, KeplerElements a, DateTime t) {
        this.name = name.trim();
        this.epoch = t;
        this.kep = new KeplerElements(a.getSma(), a.getIncl(), a.getEcc(), a.getLan(), a.getArg(), a.getXmo());
        this.mode = 'K';
        SetConsts();
        FindAnomalies();
        Kep2Cart();
    }

    public double GetPeriod() {
        return Period;
    }

    public double GetAnomalPeriod() {
        return AnomalPeriod;
    }

    public char GetMode() {
        return mode;
    }

    public void SetMode(char m) {
        mode = m;
    }

    public double GetLat() {
        return Math.atan(pos.Z() / Math.sqrt(pos.Y() * pos.Y() + pos.X() * pos.X())) * RTOD;
    }

    public double GetParametricLat() {
        // Returns the reduced or parametric Latitude (based on WGS84 Radius)
        double lat = Math.atan(pos.Z() / Math.sqrt(pos.Y() * pos.Y() + pos.X() * pos.X()));
        lat = Math.atan(Math.sqrt(1.0 - EarthEcc * EarthEcc) * Math.tan(lat));
        lat = lat * RTOD;
        return lat;
    }

    public double getAltitude() {
        double alt = 0.0;
        // Assume equitorial Radius of Earth
        //alt = pos.mag() - RE;

        // Use Ellipse Model
        double Coslat = Math.cos(GetParametricLat() * DTOR);

        double r = RP / (Math.sqrt(1.0 - EarthEcc * EarthEcc * Coslat * Coslat));

        alt = pos.mag() - r;

        return alt;
    }

    public double GetLon() {
        double GST = epoch.GST();
        //System.out.println(GST);

        //System.out.println(epoch.GST());
        //System.out.println(epoch.GST2());
        double theta = Math.atan2(pos.Y(), pos.X()) * RTOD;

        double longg = theta - GST;
        if (longg < -180) {
            longg += 360;
        }
        return longg;
    }

    public DateTime GetEpoch() {
        return epoch;
    }

    /**
     * Gets the current time from the system and finds satellite position at current time
     * @return
     * @throws Exception 
     */
    public Sat getPos() throws Exception {
        return getPos(null);
    }

    /**
     * Returns Sat for GMT time specified in milliseconds
     *
     * @param timeMillis
     * @return
     */
    public Sat getPos(Long timeMillis) throws Exception {

        long t;
        if (timeMillis == null) {
            // If null return current pos
            t = System.currentTimeMillis();
        } else {
            t = timeMillis;
        }
        long numMillisecondsSinceEpoch = t - this.epoch.epochTimeMillis();
        if (numMillisecondsSinceEpoch < 0) {
            throw new Exception("Can't find points prior to Epoch");
        }
        
        

        return this.propagate(numMillisecondsSinceEpoch / 1000.0);
    }

    /**
     * Returns Sat t seconds from Epoch
     *
     * @param t
     * @return
     */
    public Sat propagate(double t) {

        //double SecPerDay = 86400.0;  // 24 hour day
        double SecPerDay = 23.9344696 * 60.0 * 60.0;  // Sidereal Day

        double dt = t / SecPerDay;
        double no = Math.sqrt(mu / Math.pow(kep.getSma(), 3)) * SecPerDay * RTOD;
        double n = no + K1 * K2 * (1 - 3.0 / 2.0 * Math.pow(Math.sin(kep.getIncl() * DTOR), 2));
        KeplerElements NewEl = new KeplerElements();
        NewEl.setSma(kep.getSma());
        NewEl.setIncl(kep.getIncl());
        NewEl.setEcc(kep.getEcc());
        NewEl.setXmo((kep.getXmo() + n * dt) % 360.0);
        NewEl.setLan((kep.getLan() - K1 * K2 * Math.cos(kep.getIncl() * DTOR) * dt) % 360.0);
        NewEl.setArg((kep.getArg() + K1 * K2 * (2 - 5.0 / 2.0 * Math.pow(Math.sin(kep.getIncl() * DTOR), 2)) * dt) % 360.0);
        Sat NewEphm = new Sat(this.name, NewEl, epoch.plus(t));
        return NewEphm;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {

            DateTime ep = new DateTime(26, 1, 2001, 19, 0, 0.0);

            int yr = 2008;
            double d = 77.5;

            int jday = (int) d;
            double hrs = (d - jday) * 24;
            int h = (int) hrs;
            double mns = (hrs - h) * 60;
            int mn = (int) mns;
            double s = (mns - mn) * 60;

            ep = new DateTime(jday, yr, h, mn, s);

            KeplerElements tempKE = new KeplerElements();

            //tempKE.setSma(8800.045);
            //tempKE.setEcc(0.001);
            //tempKE.setIncl(98.689);
            //tempKE.setXmo(77.136);
            //tempKE.setLan(136.826);
            //tempKE.setArg(112.246);
//1 14780U 84021A   08077.50000000 -.00000172  00000-0 -38123-4 0 00008
//2 14780 098.1567 146.0324 0007808 081.1129 059.0976 14.57134434278754
//        | incl | | raan | | ecc | | ap   | | ma   | | mm      |                      
//            double incl = 98.1567;
//            double raan = 146.0324;
//            double ecc = 0.0007808;            
//            double ap = 81.1129;
//            double ma = 59.0976;
//            double mm = 14.57134434;  // Revs/Day
// Epoch DateTime is on line 1 field 21-32            
// https://api.wheretheiss.at/v1/satellites/25544/tles
/*
{
  "requested_timestamp": 1488816645,
  "tle_timestamp": 1487978973,
  "id": "25544",
  "name": "iss",
  "header": "ISS (ZARYA)",
  "line1": "1 25544U 98067A   17055.97885185  .00018774  00000-0  28647-3 0  9996",
  "line2": "2 25544  51.6412 237.6632 0006911 227.6655 268.1045 15.54462621 44303"
}
             */
//        String tleHeader = "ISS (ZARYA)";
//        String tleLine1 = "1 25544U 98067A   17055.97885185  .00018774  00000-0  28647-3 0  9996";
//        String tleLine2 = "2 25544  51.6412 237.6632 0006911 227.6655 268.1045 15.54462621 44303";
            String issTLE = "ISS (ZARYA)\n"
                    + "1 25544U 98067A   17067.61042603  .00003916  00000-0  66287-4 0  9995\n"
                    + "2 25544  51.6429 179.6566 0006893 265.0145 197.4047 15.54161233 46113";

            String[] tleParts = issTLE.split("\n");
            String tleHeader = tleParts[0];
            String tleLine1 = tleParts[1];
            String tleLine2 = tleParts[2];

            // Epoch Year
            System.out.println(tleLine1.substring(18, 20));
            System.out.println(tleLine1.substring(20, 32));
            int epochYear = Integer.parseInt(tleLine1.substring(18, 20));
            double epochFday = Double.parseDouble(tleLine1.substring(20, 32));

            ep = new DateTime(2000 + epochYear, epochFday);
            //ep = new DateTime(2000+epochYear, epochFday + 260.0/86400.0);
            //ep = new DateTime(2000+epochYear, epochFday + 4.0/1440.0);

            long now = System.currentTimeMillis() / 1000;
//        int eptime = 1487978973;
//        
//        
//
//        Date dt = new Date();
//        // Use Epoch Time from TLE
//        dt.setTime(eptime * 1000);
//
//        GregorianCalendar gc = new GregorianCalendar();
//        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
//        gc.setTimeInMillis(eptime * 1000L);
//
//        int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
//        int mon = gc.get(GregorianCalendar.MONTH) + 1;  //Add one so Jan is 1
//        int year = gc.get(GregorianCalendar.YEAR);
//        int hour = gc.get(GregorianCalendar.HOUR_OF_DAY);
//        int min = gc.get(GregorianCalendar.MINUTE);
//        double sec = gc.get(GregorianCalendar.MILLISECOND) / 1000.0 + gc.get(GregorianCalendar.SECOND);
//
//        System.out.println(day);
//        System.out.println(mon);
//        System.out.println(year);
//
//        ep = new DateTime(day, mon, year, hour, min, sec);

            double incl = Double.parseDouble(tleLine2.substring(8, 16));
            double raan = Double.parseDouble(tleLine2.substring(17, 25));
            double ecc = Double.parseDouble("." + tleLine2.substring(26, 33));
            double ap = Double.parseDouble(tleLine2.substring(34, 42));
            double ma = Double.parseDouble(tleLine2.substring(43, 51));
            double mm = Double.parseDouble(tleLine2.substring(52, 63));

            System.out.println(issTLE);
            System.out.println(incl);
            System.out.println(raan);
            System.out.println(ecc);
            System.out.println(ap);
            System.out.println(ma);
            System.out.println(mm);

            mm = mm * Math.PI * 2 / 86400;  // radians/sec
            double G = 6.67408E-11;  // Gravitation Constant m3 kg-1 s-2
            double M = 5.972E24;  // kg;
            double GM = G * M;

            //GM = 398105.8E9; // From web site
            double a = Math.cbrt(GM / mm / mm);  // meters
            a = a / 1000.0;   // km

            System.out.println("sma: " + a);

            //System.out.println(a);  //7080.6847070823205
            //a = 7077.578800899062;
            //a = 7077.6485;
            tempKE.setSma(a);
            tempKE.setEcc(ecc);
            tempKE.setIncl(incl);
            tempKE.setXmo(ma);
            tempKE.setLan(raan);
            tempKE.setArg(ap);

            System.out.println("Epoch Time: " + ep);

            Sat Sat = new Sat("ISS", tempKE, ep);

            long firstStep = now - ep.epochTimeSecs();
            System.out.println("Seconds from epoch: " + firstStep);
            Sat pos = Sat.getPos();

            String strLine;
            try {

                while (true) {
                    strLine = pos.GetEpoch() + " "
                            + pos.GetLon() + " " + pos.GetParametricLat()
                            + " " + pos.getAltitude() + "\n";
                    System.out.print(strLine);
                    Thread.sleep(1000);
                    pos = Sat.getPos();
                    //fw.write(strLine);
                }

                //fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
