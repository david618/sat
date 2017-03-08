
package org.jennings.mvnsat;

public class KeplerElements {

        private double sma; //semi-major axis (km)
        private double incl; //inclination (deg)
        private double ecc; //eccentricity (unitless)
        private double lan; //longitude of ascending node (deg)
        private double arg; //argument of perigee (deg)
        private double xmo; //mean anomaly (deg)

        public KeplerElements() {
            this.sma = 0;
            this.incl = 0;
            this.ecc = 0;
            this.lan = 0;
            this.arg = 0;
            this.xmo = 0;            
        }

        
        public KeplerElements(double sma, double incl, double ecc, double lan, double arg, double xmo) {
            this.sma = sma;
            this.incl = incl;
            this.ecc = ecc;
            this.lan = lan;
            this.arg = arg;
            this.xmo = xmo;
        }
        
        
        public double getArg() {
            return arg;
        }

        public void setArg(double arg) {
            this.arg = arg;
        }

        public double getEcc() {
            return ecc;
        }

        public void setEcc(double ecc) {
            this.ecc = ecc;
        }

        public double getIncl() {
            return incl;
        }

        public void setIncl(double incl) {
            this.incl = incl;
        }

        public double getLan() {
            return lan;
        }

        public void setLan(double lan) {
            this.lan = lan;
        }

        public double getSma() {
            return sma;
        }

        public void setSma(double sma) {
            this.sma = sma;
        }

        public double getXmo() {
            return xmo;
        }

        public void setXmo(double xmo) {
            this.xmo = xmo;
        }

        
	  


}
