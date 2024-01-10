import java.util.function.Function;

public class Profiler {

    public static double analyse(Function<Double, Double> oneMethod, double p) {
        long clock0 = timestamp();
        double result = oneMethod.apply(p);
        System.out.println("Temps d'exécution : " + timestamp(clock0));
        return result;
    }

    public static boolean analyse2(Function<Double, Boolean> oneMethod, double p) {
        long clock0 = timestamp();
        boolean result = oneMethod.apply(p);
        System.out.println("Temps d'exécution : " + timestamp(clock0));
        globalTime += (System.nanoTime() - clock0);
        count++;
        return result;
    }

    public static String timestamp(long clock0) {
        String result = null;

        if (clock0 > 0) {
            double elapsed = (System.nanoTime() - clock0) / 1e9;
            String unit = "s";
            if (elapsed < 1.0) {
                elapsed *= 1000.0;
                unit = "ms";
            }
            result = String.format("%.4g%s elapsed", elapsed, unit);
        }
        return result;
    }

    public static long timestamp() {
        return System.nanoTime();
    }

    public static long globalTime;
    public static Integer count;

    public static void init() {
        globalTime = 0;
        count = 0;
    }

    public static Long getGlobalTime() {
        return globalTime;
    }

    public static Integer getCount() {
        return count;
    }
}