package jarvis;

import java.util.List;

import jarvis.EsgGeneratorService.GeneratedReport;
import jarvis.JarvisData.EsgDataset;
import jarvis.JarvisData.ForecastPoint;
import jarvis.JarvisData.ModelRun;
import jarvis.JarvisData.RunStatus;
import jarvis.JarvisData.TrendDataset;
import jarvis.JarvisData.TrendInsight;

/**
 * JarvisApp — entry point that demonstrates importing and using data from
 * the Dashboard, Trends, and ESG pages.
 *
 * Run:
 *   javac -d out src/main/java/jarvis/*.java
 *   java  -cp out jarvis.JarvisApp
 *
 * Or pass one of the following arguments to focus on a specific section:
 *   dashboard        — full dashboard summary
 *   queries          — per-day query drill-down (all 7 days)
 *   queries:Thu      — drill-down for a single day (Mon–Sun)
 *   trends           — cross-dataset trends summary
 *   trends:queries   — detailed report for Query Volume dataset
 *   trends:accuracy  — detailed report for Model Accuracy dataset
 *   trends:latency   — detailed report for Response Latency dataset
 *   trends:users     — detailed report for Active Users dataset
 *   anomalies        — all anomaly insights across every trends dataset
 *   models           — model run table + best/worst/average
 *   reports          — AI prediction over the 5 model run reports
 *   esg              — all ESG metrics with 12-week history + forecast
 *   esg:coverage     — detail for a single ESG metric (any key)
 *   esg:push         — build the Trends payload from default ESG selection
 *   esg:generate     — generate a full GRI report draft for "Acme Corporation / Technology"
 *   esg:generate:sasb — generate using a different standard (gri/sasb/tcfd/csrd/sdg)
 */
public final class JarvisApp {

    public static void main(String[] args) {
        DashboardService    dashboard = new DashboardService();
        TrendsService       trends    = new TrendsService();
        EsgService          esg       = new EsgService();
        EsgGeneratorService generator = new EsgGeneratorService();

        String mode = args.length > 0 ? args[0].toLowerCase() : "all";

        switch (mode) {

            // ── Dashboard ──────────────────────────────────────────────────
            case "dashboard" -> dashboard.printSummary();

            // ── Daily query drill-down (all days) ─────────────────────────
            case "queries" -> dashboard.printDailyQueryBreakdown();

            // ── Daily query drill-down (single day) ───────────────────────
            case "queries:mon", "queries:tue", "queries:wed", "queries:thu",
                 "queries:fri", "queries:sat", "queries:sun" -> {
                String raw    = mode.substring("queries:".length());
                String dayKey = Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
                dashboard.queriesForDay(dayKey).ifPresentOrElse(d -> {
                    System.out.printf("── %s — %,d queries ───────────────────────────────────%n",
                            d.day, d.count);
                    d.queries.forEach(q -> System.out.println("  " + q));
                    var split = dashboard.categorySplitForDay(dayKey);
                    System.out.printf("  Categories: CODE=%d  DATA=%d  WRITING=%d  OTHER=%d%n",
                            split.getOrDefault(JarvisData.QueryCategory.CODE,    0L),
                            split.getOrDefault(JarvisData.QueryCategory.DATA,    0L),
                            split.getOrDefault(JarvisData.QueryCategory.WRITING, 0L),
                            split.getOrDefault(JarvisData.QueryCategory.OTHER,   0L));
                }, () -> System.err.println("Unknown day: " + dayKey));
            }

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

            // ── Report-based AI prediction (mirrors dashboard analyzeBtn) ─
            case "reports" -> {
                TrendDataset ds = JarvisData.reportsForecastDataset();
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("  J.A.R.V.I.S  —  Report Accuracy Forecast");
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("\n── Run History ─────────────────────────────────────────────");
                for (int i = 0; i < ds.weeks.size(); i++) {
                    System.out.printf("  %-6s  %.1f%%%n", ds.weeks.get(i), ds.history.get(i));
                }
                System.out.println("\n── 4-Run Forecast ──────────────────────────────────────────");
                ds.forecastPoints().forEach(fp -> System.out.println("  " + fp));
                System.out.println("\n── Stat Cards ──────────────────────────────────────────────");
                ds.stats.forEach(s -> System.out.println("  " + s));
                System.out.println("\n── AI Insights ─────────────────────────────────────────────");
                ds.insights.forEach(i -> System.out.println("  " + i));
                System.out.println("\n═══════════════════════════════════════════════════════════");
            }

            // ── ESG — all metrics ─────────────────────────────────────────
            case "esg" -> esg.printMetricSummary();

            // ── ESG — single metric detail ────────────────────────────────
            case "esg:coverage", "esg:completeness", "esg:critical",
                 "esg:major", "esg:emissions", "esg:social" -> {
                String key = mode.substring("esg:".length());
                esg.findMetric(key).ifPresentOrElse(m -> {
                    EsgDataset ds = new EsgDataset(m);
                    System.out.printf("═══ ESG: %s (%s) ═══%n", m.label, m.unit);
                    System.out.println("\nHistory:");
                    for (int i = 0; i < ds.weeks.size(); i++) {
                        System.out.printf("  %-4s  %.2f%n", ds.weeks.get(i), ds.history.get(i));
                    }
                    System.out.println("\nForecast:");
                    for (int i = 0; i < ds.forecast.forecast.size(); i++) {
                        System.out.printf("  F%d  %.2f  [%.2f – %.2f]%n",
                                i+1,
                                ds.forecast.forecast.get(i),
                                ds.forecast.confLow.get(i),
                                ds.forecast.confHigh.get(i));
                    }
                    System.out.printf("%nTrend: %s  |  Total change: %+.2f  |  Latest: %.2f%n",
                            ds.forecast.trendLabel(), ds.totalChange(), ds.latestValue());
                }, () -> System.err.println("Unknown ESG key: " + key));
            }

            // ── ESG — generate report draft ───────────────────────────────
            case "esg:generate",
                 "esg:generate:gri", "esg:generate:sasb", "esg:generate:tcfd",
                 "esg:generate:csrd", "esg:generate:sdg" -> {
                String std = mode.contains(":generate:") ? mode.substring(mode.lastIndexOf(':') + 1) : "gri";
                GeneratedReport report = generator.generateDefault(std, "Acme Corporation", "Technology");
                System.out.println(report.toPlainText());
            }

            // ── ESG — push to Trends (build payload) ──────────────────────
            case "esg:push" -> {
                TrendDataset payload = esg.buildTrendPayload();
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("  J.A.R.V.I.S  —  ESG → Trends Payload (default selection)");
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("  Primary metric : " + payload.label);
                System.out.println("  Unit           : " + payload.unit);
                System.out.println("  Chart title    : " + payload.chartTitle);
                System.out.println("\n  Stat cards:");
                payload.stats.forEach(s -> System.out.println("    " + s));
                System.out.println("\n  AI Insights:");
                payload.insights.forEach(i -> System.out.println("    " + i));
                System.out.println("\n  (Push this payload to localStorage key 'jarvis_report_dataset' to load it in trends.html)");
                System.out.println("\n═══════════════════════════════════════════════════════════");
            }

            // ── Default: run everything ───────────────────────────────────
            default -> {
                // 1. Dashboard overview
                dashboard.printSummary();

                // 2. Busiest day drill-down
                var peak = dashboard.peakQueryDay();
                System.out.println("\n── Peak Day Query Drill-Down: " + peak.day + " ─────────────────────────");
                peak.queries.forEach(q -> System.out.println("  " + q));

                // 3. Trends cross-dataset
                trends.printCrossDatasetSummary();

                // 4. Spotlight: Query Volume forecast
                System.out.println("\n── Query Volume — Forecast Detail ─────────────────────────");
                List<ForecastPoint> qForecast = trends.forecast("queries");
                qForecast.forEach(fp -> System.out.println("  " + fp));
                System.out.printf("  Peak forecast: %.0fk  |  Highest confidence: %s (%d%%)%n",
                        trends.peakForecast("queries"),
                        trends.highestConfidenceForecast("queries").period,
                        trends.highestConfidenceForecast("queries").confidencePercent);

                // 5. All anomalies
                System.out.println("\n── Anomaly Insights Across All Datasets ────────────────────");
                trends.allAnomalies().forEach(a -> System.out.println("  [ANOMALY] " + a.title + ": " + a.description));

                // 6. Warnings from activity feed
                System.out.println("\n── Dashboard Warnings ──────────────────────────────────────");
                dashboard.warnings().forEach(w -> System.out.println("  [WARN] " + w.title + " (" + w.timeAgo + ")"));

                // 7. Fastest-growing metric
                String fastest = trends.fastestGrowingDataset();
                System.out.println("\n── Fastest Growing Metric: " + fastest.toUpperCase() + " ─────────────────────────");
                trends.insights(fastest).forEach(i -> System.out.println("  " + i));

                // 8. ESG summary
                System.out.println();
                esg.printMetricSummary();

                // 9. Report-based forecast
                System.out.println("\n── Report Accuracy Forecast ────────────────────────────────");
                TrendDataset rds = JarvisData.reportsForecastDataset();
                rds.forecastPoints().forEach(fp -> System.out.println("  " + fp));
            }
        }
    }
}
