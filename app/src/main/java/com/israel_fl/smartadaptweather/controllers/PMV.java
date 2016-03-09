package com.israel_fl.smartadaptweather.controllers;

import android.content.Context;
import android.util.Log;
import java.lang.Math;

/**
 * This class will handle all the data to the PMV formula
 */
public class PMV {

    private double pmv; // predicted mean vote
    private double ppd; // predicted percentage dissatisfied

    public PMV() {
        // required empty constructor
    }

    // Constructor
    public PMV(double temperature, double humidity, double meanTemp, double clothing) {

        double met = 1.1; // metabolic rate
        double wme = 0; // external work
        double ta = temperature; // air temperature average
        double tr = meanTemp; // mean radiant temperature
        double vel = 0.1; // wind velocity
        double rh = humidity; // relative humidity average
        double clo = clothing;// clothing
        double pa = 0; // water vapor pressure, set to 0 to allow compilation
        double fnps; // saturated vapor in kPA
        double icl; // thermal insulation of clothing
        double m; // metabolic rate in w/m2
        double w; // external work in w/m2
        double mw; // external heat production of the human body
        double fcl; // clothing area factor
        double hcf; // heat transfer coefficient by forced convection
        double hc = 0;
        double taa; // air temperature in Kelvin
        double tra; // mean radiant temperature in kelvin
        double tcla; // first guess for surface temperature of clothing
        double p1; // calculation terms
        double p2; // calculation terms
        double p3; // calculation terms
        double p4; // calculation terms
        double p5; // calculation terms
        double xn;
        double xf; // heat transfer by natural convection
        int n; // number of iterations
        double eps; // stop criteria in iteration
        double hcn;
        double tcl; // surface temperature of clothing
        double hl1; // heat loss diff. through skin
        double hl2; // heat loss by sweating (comfort)
        double hl3; // latent respiration loss
        double hl4; // dry respiration heat loss
        double hl5; // heat loss by radiation
        double hl6; // heat loss by convection
        double ts; // thermal sensation trans. coeff

        fnps = Math.exp(16.6536-4030.183 / (ta + 235)); // pressure in kPa

        if (pa == 0) { // FIXME: 3/6/2016
            pa = rh * 10 * fnps; // Pa
        }

        icl = 0.155 * clo; // thermal insulation of clothing

        m = met * 58.15; // metabolic rate in w/m2

        w = wme * 58.15; // external work in w/m2

        mw = m - w; // internal heat production of the human body

        // clothing area factor
        if (icl < 0.078) {
            fcl = 1 + (1.29 * icl);
        }
        else {
            fcl = 1.05 + (0.645 * icl);
        }

        hcf = 12.1 * Math.sqrt(vel); // heat transfer coefficient by forced convection

        taa = ta + 273; // air temperature in Kelvin
        tra = tr + 273; // mean radiant temperature in kelvin

        /* Calculate surface temperature of clothing by iteration */
        // first guess for surface temperature of clothing
        tcla = taa + (35.5 - ta) / (3.5 * ((6.45 * icl) + 0.1));

        // calculation terms
        p1 = icl * fcl;
        p2 = p1 * 3.96;
        p3 = p1 * 100;
        p4 = p1 * taa;
        p5 = 308.7 - (0.028 * mw) + (p2 * (Math.pow((tra/100), 4)));

        xn = tcla / 100;
        xf = xn;

        n = 0; // number of iterations
        eps = 0.00015; // stop criteria in iteration

        do {
            xf = (xf + xn) / 2; // heat transfer by natural convection

            hcn = 2.38 * Math.pow((Math.abs((100 * xf) - taa)), 0.25);

            if (hcf > hcn) {
                hc = hcf;
            } else {
                hc = hcn;
            }

            xn = (p5 + (p4 * hc) - (p2 * (Math.pow(xf, 4)))) / (100 + (p3 * hc));
            n++;

            if (n > 150) {
                break; // exit while loop
            }

        } while (Math.abs(xn - xf) < eps);

        tcl = (100 * xn) - 273; // surface temperature of clothing

        hl1 = 3.05 * .001 * (5733 - (6.99 * mw) - pa); // heat loss diff. through skin

        if (mw > 58.15) {
            hl2 = 0.42 * (mw - 58.15); // heat loss by sweating (comfort)
        } else {
            hl2 = 0; // heat loss by sweating (comfort)
        }

        hl3 = 1.7 * .00001 * m * (5867 - pa); // latent respiration loss
        hl4 = 0.0014 * m * (34 - ta); // dry respiration heat loss
        hl5 = 3.96 * fcl * (Math.pow(xn, 4) - (Math.pow((tra / 100), 4))); // heat loss by radiation
        hl6 = fcl * hc * (tcl - ta); // heat loss by convection

            /* Calculate PMV and PPD */
        ts = (0.303 * Math.exp(-0.036 * m)) + 0.028; // thermal sensation trans. coeff

        if (n > 150) {
            pmv = 99999;
            ppd = 100;
        }
        else {
            /* This is the calculated Predicted Mean Vote */
            pmv = ts * (mw - hl1 - hl2 - hl3 - hl4 - hl5 - hl6); // predicted mean vote
            /* Predicted Percentage dissatisfied */
            ppd = 100 - (95 * Math.exp((-0.03353 * (Math.pow(pmv, 4))) - (0.2179 * (Math.pow(pmv, 2)))));
        }
        Log.e("com.israel-fl.pmv", "Calculated PMV: " + pmv + " Calculated PPD: " + ppd);

    }

    public double getPmv() {
        return pmv;
    }

}
