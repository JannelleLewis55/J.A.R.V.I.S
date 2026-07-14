package jarvis;

import java.util.List;

import jarvis.JarvisData.ForecastPoint;
import jarvis.JarvisData.ModelRun;
import jarvis.JarvisData.RunStatus;
import jarvis.JarvisData.TrendInsight;

/**
 * JarvisApp — entry point that demonstrates importing and using data from
 * both the Dashboard and the Trends pages.
 *
 * Run:
 *   javac -d out src/main/java/jarvis/*.java
 *   java  -cp out jarvis.JarvisApp
 *
 * Or pass one of the following arguments to focus on a specific section:
 *   dashboard        — full dashboard summary
 *   trends           — cross-dataset trends summary
 *   trends:queries   — detailed report for Query Volume dataset
 *   trends:accuracy  — detailed report for Model Accuracy dataset
 *   trends:latency   — detailed report for Response Latency dataset
 *   trends:users     — detailed report for Active Users dataset
 *   anomalies        — all anomaly insights across every trends dataset
 *   models           — model run table + best/worst/average
 */
public final class JarvisApp {

    public static void main(String[] args) {
        DashboardService dashboard = new DashboardService();
        TrendsService    trends    = new TrendsService();

        String mode = args.length > 0 ? args[0].toLowerCase() : "all";

        switch (mode) {

            // ── Dashboard ──────────────────────────────────────────────────
            case "dashboard" -> dashboard.printSummary();

            // ── Trends — cross-dataset ────────────────────────────────────
            case "trends" -> trends.printCrossDatasetSummary();

            // ── Trends — individual datasets ──────────────────────────────
            case "trends:queries"  -> trends.printDatasetReport("queries");
            case "trends:accuracy" -> trends.printDatasetReport("accuracy");
            case "trends:latency"  -> trends.printDatasetReport("latency");
            case "trends:users"    -> trends.printDatasetReport("users");

            // ── Anomalies across all datasets ─────────────────────────────
            case "anomalies" -> {
                List<TrendInsight> anomalies = trends.allAnomalies();
                System.out.println("All anomaly insights (" + anomalies.size() + " found):");
                anomalies.forEach(a -> System.out.println("  " + a));
            }

            // ── Model runs ────────────────────────────────────────────────
            case "models" -> {
                System.out.println("Recent model runs:");
                dashboard.getModelRuns().forEach(r -> System.out.println("  " + r));
                ModelRun best  = dashboard.bestRun();
                ModelRun worst = dashboard.worstRun();
                System.out.printf("%nBest  : #%d %s  %.1f%%%n", best.runId,  best.model,  best.accuracyPercent);
                System.out.printf("Worst : #%d %s  %.1f%%%n", worst.runId, worst.model, worst.accuracyPercent);
                System.out.printf("Avg   : %.2f%%%n", dashboard.averageAccuracy());

                List<ModelRun> failed = dashboard.runsByStatus(RunStatus.FAILED);
                if (!failed.isEmpty()) {
                    System.out.println("\nFailed runs:");
                    failed.forEach(r -> System.out.println("  " + r));
                }
            }

            // ── Default: run everything ───────────────────────────────────
            default -> {
                // 1. Dashboard overview
                dashboard.printSummary();

                // 2. Trends cross-dataset
                trends.printCrossDatasetSummary();

                // 3. Spotlight: Query Volume forecast
                System.out.println("\n── Query Volume — Forecast Detail ─────────────────────────");
                List<ForecastPoint> qForecast = trends.forecast("queries");
                qForecast.forEach(fp -> System.out.println("  " + fp));
                System.out.printf("  Peak forecast: %.0fk  |  Highest confidence: %s (%d%%)%n",
                        trends.peakForecast("queries"),
                        trends.highestConfidenceForecast("queries").period,
                        trends.highestConfidenceForecast("queries").confidencePercent);

                // 4. All anomalies
                System.out.println("\n── Anomaly Insights Across All Datasets ────────────────────");
                trends.allAnomalies().forEach(a -> System.out.println("  [ANOMALY] " + a.title + ": " + a.description));

                // 5. Warnings from activity feed
                System.out.println("\n── Dashboard Warnings ──────────────────────────────────────");
                dashboard.warnings().forEach(w -> System.out.println("  [WARN] " + w.title + " (" + w.timeAgo + ")"));

                // 6. Fastest-growing metric
                String fastest = trends.fastestGrowingDataset();
                System.out.println("\n── Fastest Growing Metric: " + fastest.toUpperCase() + " ─────────────────────────");
                trends.insights(fastest).forEach(i -> System.out.println("  " + i));
            }
        }
    }
}
