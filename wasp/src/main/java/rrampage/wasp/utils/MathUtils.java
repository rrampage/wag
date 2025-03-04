package rrampage.wasp.utils;

import java.math.BigInteger;

public class MathUtils {
    public static final int FLOAT_CANONICAL_NAN_BITS = 255<<23 |1<<22;
    public static final int FLOAT_CANONICAL_NEG_NAN_BITS = -FLOAT_CANONICAL_NAN_BITS;
    public static final float FLOAT_CANONICAL_NAN = Float.intBitsToFloat(FLOAT_CANONICAL_NAN_BITS);
    public static final float FLOAT_CANONICAL_NAN_NEG = Float.intBitsToFloat(FLOAT_CANONICAL_NEG_NAN_BITS);
    public static final long DOUBLE_CANONICAL_NAN_BITS = 2047L<<52 |1L<<51;
    public static final long DOUBLE_CANONICAL_NEG_NAN_BITS = -DOUBLE_CANONICAL_NAN_BITS;
    public static final double DOUBLE_CANONICAL_NAN = Double.longBitsToDouble(DOUBLE_CANONICAL_NAN_BITS);
    public static final BigInteger MAX_UNSIGNED_LONG = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
    public static final double MAX_UNSIGNED_LONG_DOUBLE_VAL = MAX_UNSIGNED_LONG.doubleValue();
    public static final float MAX_UNSIGNED_LONG_FLOAT_VAL = MAX_UNSIGNED_LONG.floatValue();
    public static long MAX_UNSIGNED_INT = Integer.MAX_VALUE * 2L + 1L;
    public static float MAX_ULP_FLOAT = 0x1.0p23f;
    public static float MIN_ULP_FLOAT = -0x1.0p23f;
    public static double MAX_ULP_DOUBLE = 0x1.0p52;
    public static double MIN_ULP_DOUBLE = -0x1.0p52;

    /**
     * Generates nearest integral number<br>
     * <a href="https://webassembly.github.io/spec/core/exec/numerics.html#op-fnearest">Spec</a><br>
     * Helpful articles:<ul>
     *     <li><a href="https://ciechanow.ski/exposing-floating-point/">Exposing floating point</a></li>
     *     <li><a href="https://frama-c.com/2013/05/02/Harder-than-it-looks-rounding-float-to-nearest-integer-part-1.html">Harder than it looks: rounding float to nearest integer, part 1</a></li>
     *     <li><a href="https://frama-c.com/2013/05/03/Rounding-float-to-nearest-integer-part-2.html">Rounding float to nearest integer, part 2</a></li>
     *     <li><a href="https://frama-c.com/2013/05/04/Rounding-float-to-nearest-integer-part-3.html">Rounding float to nearest integer, part 3</a></li>
     *     <li></li>
     * </ul>
     *
     * @param a
     * @return
     */
    public static float nearest(float a) {
        if (isCanonicalNaN(a) || Float.isNaN(a) || Float.isInfinite(a) || a == 0.0f) {return a;}
        if (Float.MAX_VALUE == a || -Float.MAX_VALUE == a) {return a;}
        if (a > 0.0f && a <= 0.5f) {return 0.0f;}
        if (a < 0.0f && a >= -0.5f) {return -0.0f;}
        if (a < MIN_ULP_FLOAT || a > MAX_ULP_FLOAT) {
            return a;
        }
        var b = (int) a;
        var c = a - b;
        if (c == 0.0f) {return a;}
        // check if frac is 0.5f and return nearest even int
        if (c == 0.5f) {
            return (b%2 == 0) ? b : b+1;
        }
        if (c == -0.5f) {
            return (b%2 == 0) ? b : b-1;
        }
        // return new BigDecimal(a).setScale(0, RoundingMode.HALF_EVEN).floatValue();
        if (c < 0) {
            return (c < -0.5f) ? b-1 : b;
        }
        return (c < 0.5f) ? b : b+1;
    }

    public static double nearest(double a) {
        if (isCanonicalNaN(a) || Double.isNaN(a) || Double.isInfinite(a) || a == 0.0) {return a;}
        if (Double.MAX_VALUE == a || -Double.MAX_VALUE == a) {return a;}
        if (a < MIN_ULP_DOUBLE || a > MAX_ULP_DOUBLE) {return  a;}
        if (a > 0.0 && a <= 0.5) {return 0.0;}
        if (a < 0.0 && a >= -0.5) {return -0.0;}
        // return new BigDecimal(a).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
        var b = (long) a;
        var c = a - b;
        if (c == 0.0) {return a;}
        // check if frac is 0.5 and return nearest even int
        if (c == 0.5) {
            return (b%2 == 0) ? b : b+1;
        }
        if (c == -0.5) {
            return (b%2 == 0) ? b : b-1;
        }
        if (c < 0) {
            return (c < -0.5) ? b-1 : b;
        }
        return (c < 0.5) ? b : b+1;
    }

    public static boolean isCanonicalNaN(float a) {
        return Float.floatToRawIntBits(a) == FLOAT_CANONICAL_NAN_BITS || Float.floatToRawIntBits(a) == FLOAT_CANONICAL_NEG_NAN_BITS;
    }

    public static boolean isCanonicalNaN(double a) {
        return Double.doubleToRawLongBits(a) == DOUBLE_CANONICAL_NAN_BITS || Double.doubleToRawLongBits(a) == DOUBLE_CANONICAL_NEG_NAN_BITS;
    }

    public static long truncateDoubleToUnsignedLong(double d) {
        System.out.println(d);
        if (Double.isNaN(d) || d <= 0.0) {
            return 0L;
        }
        if (d >= MAX_UNSIGNED_LONG_DOUBLE_VAL) {
            return -1L;
        }
        if (d < Long.MAX_VALUE) {
            return (long) d;
        }
        return (long)(Math.floor(d) - MAX_UNSIGNED_LONG_DOUBLE_VAL);
    }

    public static long truncateFloatToUnsignedLong(float f) {
        if (Float.isNaN(f) || f <= 0.0f) {
            return 0L;
        }
        if (f >= MAX_UNSIGNED_LONG_FLOAT_VAL) {
            return -1L;
        }
        if (f < Long.MAX_VALUE) {
            return (long) f;
        }
        return (long)(Math.floor(f) - MAX_UNSIGNED_LONG_FLOAT_VAL);
    }

    public static int truncateDoubleToUnsignedInt(double d) {
        return (Double.isNaN(d) || d <= 0.0) ? 0 :
                ((d >= MAX_UNSIGNED_INT) ? -1 :
                    d < Integer.MAX_VALUE ? (int) d : (int) ((long)d));
    }

    public static int truncateFloatToUnsignedInt(float f) {
        if (Float.isNaN(f) || f <= 0.0f) {
            return 0;
        }
        if (f >= MAX_UNSIGNED_INT) {
            return -1;
        }
        if (f < Integer.MAX_VALUE) {
            return (int) f;
        }
        return (int) ((long)f);
    }

    public static void main(String[] args) {
        int x = 1;
        for (int i = 1; i < 8; i++) {
            int n = x << (23+i);
            int m = n | 0x90000000;
            System.out.printf("Int: %x %d %s Float: %f\n", n, n, Integer.toUnsignedString(n), Float.intBitsToFloat(n));
            System.out.printf("Int: %x %d %s Float: %f\n", m, m, Integer.toUnsignedString(m), Float.intBitsToFloat(m));
        }
        System.out.println(nearest(-4.2f));
        System.out.println(nearest(-4.8f));
        System.out.println(nearest(-5.8f));
        System.out.println(nearest(-5.2f));
    }
}
