package com.example.projectpoker.model.statistics;

import java.util.Random;

public class SkewNormalSampler {
    private final Random random;

    public SkewNormalSampler() {
        this.random = new Random();
    }

    public SkewNormalSampler(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Samples from a skew-normal distribution.
     *
     * @param xi    location parameter
     * @param omega scale parameter (must be > 0)
     * @param alpha shape/skew parameter
     * @return one random sample from SN(xi, omega, alpha)
     */
    public int sample(double xi, double omega, double alpha) {
        if (omega <= 0) {
            throw new IllegalArgumentException("omega must be > 0");
        }

        double delta = alpha / Math.sqrt(1.0 + alpha * alpha);

        double u0 = random.nextGaussian();  // N(0,1)
        double v = random.nextGaussian();   // N(0,1)

        double z = delta * Math.abs(u0) + Math.sqrt(1.0 - delta * delta) * v;

        return safeRoundToInt(xi + omega * z);
    }

    public int[] sampleMany(int n, double xi, double omega, double alpha) {
        int[] samples = new int[n];
        for (int i = 0; i < n; i++) {
            samples[i] = sample(xi, omega, alpha);
        }
        return samples;
    }

    /**
     * Safely rounds a double to the nearest int.
     * Throws an exception if the value is outside int range.
     */
    public static int safeRoundToInt(double number) {
        long roundedLong = Math.round(number); // Rounds to nearest long
        if (roundedLong > Integer.MAX_VALUE || roundedLong < Integer.MIN_VALUE) {
            throw new ArithmeticException("Value out of int range after rounding.");
        }
        return (int) roundedLong;
    }
}
