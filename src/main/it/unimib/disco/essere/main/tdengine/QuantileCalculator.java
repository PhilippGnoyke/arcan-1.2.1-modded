package it.unimib.disco.essere.main.tdengine;

import java.util.*;

// Modded from here
public class QuantileCalculator
{
    private static final int NUM_QUANTILES = 21;
    private NavigableMap<Double, Integer> udNud;
    private NavigableMap<Double, Integer> udPr;
    private NavigableMap<Double, Integer> hdNtd;
    private NavigableMap<Double, Integer> hdPr;
    private NavigableMap<Double, Integer> cdCNov;
    private NavigableMap<Double, Integer> cdCPr;
    private NavigableMap<Double, Integer> cdPNov;
    private NavigableMap<Double, Integer> cdPPr;

    // Store lower and upper value bounds to reduce tree traversals
    private double udNudFirst;
    private double udNudLast;
    private double udPrFirst;
    private double udPrLast;
    private double hdNtdFirst;
    private double hdNtdLast;
    private double hdPrFirst;
    private double hdPrLast;
    private double cdCNovFirst;
    private double cdCNovLast;
    private double cdCPrFirst;
    private double cdCPrLast;
    private double cdPNovFirst;
    private double cdPNovLast;
    private double cdPPrFirst;
    private double cdPPrLast;

    //----------------------------------------------------------------------------------------------------------------

    // Singleton
    private static QuantileCalculator instance;

    private QuantileCalculator()
    {
        // from https://boa.unimib.it/handle/10281/199005
        // (Roveda: Identifying and Evaluating Software Architecture Erosion)
        // Array initialisation for easy copy-pasting values
        final double[] UD_NUD_VALS = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 5, 6, 9, 42};
        final double[] UD_PR_VALS = {1.08, 2.85, 3.85, 4.77, 5.63, 6.71, 7.81, 8.94, 10.38, 12.14, 13.69, 15.83, 18.46, 22.85, 26.70, 32.64, 41.35, 51.60, 72.29, 117.32, 1766.90};
        final double[] HD_NTD_VALS = {21, 33, 39, 44, 52, 59, 66, 69, 74, 81, 85, 92, 96, 102, 108, 117, 129, 139, 165, 194, 893};
        final double[] HD_PR_VALS = {4.35, 9.08, 11.54, 15.44, 18.24, 22.38, 25.93, 30.04, 34.53, 37.71, 46.65, 51.08, 56.68, 64.48, 75.57, 82.23, 93.06, 114.51, 137.18, 167.26, 428.95};
        final double[] CD_C_NOV_VALS = {2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 4, 5, 7, 10, 14, 18, 24, 33, 54, 102};
        final double[] CD_C_PR_VALS = {0.78, 0.90, 0.96, 1.05, 1.16, 1.32, 1.53, 1.82, 2.20, 2.84, 3.97, 5.66, 8.59, 14.32, 23.62, 35.32, 51.64, 79.75, 139.14, 263.39, 418.22};
        final double[] CD_P_NOV_VALS = {2, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 8, 9, 11, 12, 15, 19, 27, 49, 79};
        final double[] CD_P_PR_VALS = {1.84, 8.62, 12.97, 17.61, 22.71, 28.53, 34.82, 42.11, 51.55, 62.94, 75.38, 90.00, 106.77, 126.19, 150.30, 185.52, 256.20, 380.28, 553.30, 836.93, 1599.88};

        // Keep values in tree maps for log2(n) traversal functions.
        // Binary search in original lists doesn't work as multiple entries can have the same value -> undefined behaviour.
        udNud = new TreeMap<>();
        udPr = new TreeMap<>();
        hdNtd = new TreeMap<>();
        hdPr = new TreeMap<>();
        cdCNov = new TreeMap<>();
        cdCPr = new TreeMap<>();
        cdPNov = new TreeMap<>();
        cdPPr = new TreeMap<>();

        int lastInd = NUM_QUANTILES - 1;
        udNudFirst = UD_NUD_VALS[0];
        udNudLast = UD_NUD_VALS[lastInd];
        udPrFirst = UD_PR_VALS[0];
        udPrLast = UD_PR_VALS[lastInd];
        hdNtdFirst = HD_NTD_VALS[0];
        hdNtdLast = HD_NTD_VALS[lastInd];
        hdPrFirst = HD_PR_VALS[0];
        hdPrLast = HD_PR_VALS[lastInd];
        cdCNovFirst = CD_C_NOV_VALS[0];
        cdCNovLast = CD_C_NOV_VALS[lastInd];
        cdCPrFirst = CD_C_PR_VALS[0];
        cdCPrLast = CD_C_PR_VALS[lastInd];
        cdPNovFirst = CD_P_NOV_VALS[0];
        cdPNovLast = CD_P_NOV_VALS[lastInd];
        cdPPrFirst = CD_P_PR_VALS[0];
        cdPPrLast = CD_P_PR_VALS[lastInd];

        for (int i = 0; i < NUM_QUANTILES; i++)
        {
            udNud.put(UD_NUD_VALS[i], i);
            udPr.put(UD_PR_VALS[i], i);
            hdNtd.put(HD_NTD_VALS[i], i);
            hdPr.put(HD_PR_VALS[i], i);
            cdCNov.put(CD_C_NOV_VALS[i], i);
            cdCPr.put(CD_C_PR_VALS[i], i);
            cdPNov.put(CD_P_NOV_VALS[i], i);
            cdPPr.put(CD_P_PR_VALS[i], i);
        }
    }

    public static QuantileCalculator getInstance()
    {
        if (QuantileCalculator.instance == null)
        {
            QuantileCalculator.instance = new QuantileCalculator();
        }
        return QuantileCalculator.instance;
    }

    //----------------------------------------------------------------------------------------------------------------

    public double quantileUdNud(int nud)
    {
        return quantileInList(nud, udNud, udNudFirst, udNudLast);
    }

    public double quantileUdPr(double pr)
    {
        return quantileInList(pr, udPr, udPrFirst, udPrLast);
    }

    public double quantileHdNtd(int ntd)
    {
        return quantileInList(ntd, hdNtd, hdNtdFirst, hdNtdLast);
    }

    public double quantileHdPr(double pr)
    {
        return quantileInList(pr, hdPr, hdPrFirst, hdPrLast);
    }

    public double quantileCdClassNov(int nov)
    {
        return quantileInList(nov, cdCNov, cdCNovFirst, cdCNovLast);
    }

    public double quantileCdClassPr(double pr)
    {
        return quantileInList(pr, cdCPr, cdCPrFirst, cdCPrLast);
    }

    public double quantileCdPackNov(int nov)
    {
        return quantileInList(nov, cdPNov, cdPNovFirst, cdPNovLast);
    }

    public double quantileCdPackPr(double pr)
    {
        return quantileInList(pr, cdPPr, cdPPrFirst, cdPPrLast);
    }

    //----------------------------------------------------------------------------------------------------------------

    // Returns p of the p-quantile [0,1] given an (interpolated) index within the lists above
    private double quantileFromIntPolIndex(double index)
    {
        return index / (NUM_QUANTILES - 1);
    }

    // Returns the linearly interpolated index within two given x and two y values
    private double interpolateIndex(double val, double lowVal, double highVal, int lowInd, int highInd)
    {
        return lowInd + (val - lowVal) * (highInd - lowInd) / (highVal - lowVal);
    }

    // Returns the interpolated quantile of a value in the respective list
    private double quantileInList(double val, NavigableMap<Double, Integer> map, double first, double last)
    {
        if (val < first)
        {
            return 0;
        }
        if (val >= last)
        {
            return 1;
        }

        double quantile;

        // Gives entry
        Map.Entry<Double, Integer> lowKv = map.floorEntry(val);
        Map.Entry<Double, Integer> highKv = map.ceilingEntry(val);
        int lowInd = lowKv.getValue();
        int highInd = highKv.getValue();

        if (lowInd == highInd)
        {
            quantile = quantileFromIntPolIndex(lowInd);
        }
        else
        {
            highInd = lowInd + 1; // This is necessary as multiple quantiles with the same value would skew results
            double lowVal = lowKv.getKey();
            double highVal = highKv.getKey();
            double intPolIndex = interpolateIndex(val, lowVal, highVal, lowInd, highInd);
            quantile = quantileFromIntPolIndex(intPolIndex);
        }
        return quantile;
    }
}
