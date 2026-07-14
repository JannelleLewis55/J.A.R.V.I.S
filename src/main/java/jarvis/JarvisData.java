package jarvis;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JarvisData — static data models mirroring dashboard.html and trends.html.
 *
 * All inner classes are plain immutable value objects (no setters).
 * The companion static factory methods at the bottom of this file produce
 * the same numbers displayed in the UI so the two stay in sync.
 */
public final class JarvisData {

    private JarvisData() {}

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD MODELS
    // ─────────────────────────────────────────────────────────────────────────

    /** A single stat card shown at the top of the dashboard. */
    public static final class StatCard {
        public final String label;
        public final String value;
        public final String subText;
        /** "up", "down", or "neutral" */
        public final String direction;

        public StatCard(String label, String value, String subText, String direction) {
            this.label     = label;
            this.value     = value;
            this.subText   = subText;
            this.direction = direction;
        }

        @Override
        public String toString() {
            String arrow = "up".equals(direction) ? "↑" : "down".equals(direction) ? "↓" : "–";
            return String.format("[%s] %s  (%s %s)", label, value, arrow, subText);
        }
    }

    /** One bar in the daily query-volume bar chart. */
    public static final class DailyBar {
        public final String day;
        /** 0–100, percentage of chart height */
        public final int heightPercent;

        public DailyBar(String day, int heightPercent) {
            this.day           = day;
            this.heightPercent = heightPercent;
        }

        @Override
        public String toString() {
            return String.format("%s: %d%%", day, heightPercent);
        }
    }

    /** One slice of the query-category donut chart. */
    public static final class DonutSlice {
        public final String category;
        public final int percent;
        public final String color;

        public DonutSlice(String category, int percent, String color) {
            this.category = category;
            this.percent  = percent;
            this.color    = color;
        }

        @Override
        public String toString() {
            return String.format("%s: %d%%", category, percent);
        }
    }

    /** "Passed / Review / Failed" status values for model runs. */
    public enum RunStatus { PASSED, REVIEW, FAILED }

    /** One row in the Recent Model Reports table. */
    public static final class ModelRun {
        public final int    runId;
        public final String model;
        public final double accuracyPercent;
        public final RunStatus status;

        public ModelRun(int runId, String model, double accuracyPercent, RunStatus status) {
            this.runId           = runId;
            this.model           = model;
            this.accuracyPercent = accuracyPercent;
            this.status          = status;
        }

        @Override
        public String toString() {
            return String.format("#%d  %-10s  %.1f%%  %s",
                    runId, model, accuracyPercent, status);
        }
    }

    /** One item in the System Activity live feed. */
    public static final class ActivityItem {
        public final String title;
        public final String timeAgo;
        /** "info", "ok", "warn", "event" */
        public final String type;

        public ActivityItem(String title, String timeAgo, String type) {
            this.title   = title;
            this.timeAgo = timeAgo;
            this.type    = type;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s  (%s)", type.toUpperCase(), title, timeAgo);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRENDS MODELS
    // ─────────────────────────────────────────────────────────────────────────

    /** One AI-generated insight card for a dataset. */
    public static final class TrendInsight {
        public final String title;
        public final String description;
        /** "up", "down", "anomaly", "info", "ok" */
        public final String type;

        public TrendInsight(String title, String description, String type) {
            this.title       = title;
            this.description = description;
            this.type        = type;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s — %s", type.toUpperCase(), title, description);
        }
    }

    /** One row of the 4-week forecast table. */
    public static final class ForecastPoint {
        public final String period;
        public final double forecast;
        public final double confLow;
        public final double confHigh;
        public final int    confidencePercent;

        public ForecastPoint(String period, double forecast,
                             double confLow, double confHigh, int confidencePercent) {
            this.period            = period;
            this.forecast          = forecast;
            this.confLow           = confLow;
            this.confHigh          = confHigh;
            this.confidencePercent = confidencePercent;
        }

        @Override
        public String toString() {
            return String.format("%s  forecast=%.2f  95%%CI=[%.2f, %.2f]  conf=%d%%",
                    period, forecast, confLow, confHigh, confidencePercent);
        }
    }

    /** A mini stat card shown below the trend chart. */
    public static final class TrendStat {
        public final String label;
        public final String value;
        public final String subText;
        /** "up", "down", "pred", "neutral" */
        public final String cls;

        public TrendStat(String label, String value, String subText, String cls) {
            this.label   = label;
            this.value   = value;
            this.subText = subText;
            this.cls     = cls;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s — %s (%s)", cls, label, value, subText);
        }
    }

    /**
     * A full trends dataset: historical series, forecast, confidence bands,
     * stat cards, and AI insights — mirrors the DATASETS object in trends.html.
     */
    public static final class TrendDataset {
        public final String         key;
        public final String         label;
        public final String         unit;
        public final String         chartTitle;
        public final List<String>   weeks;
        public final List<Double>   history;
        public final List<Double>   forecast;
        public final List<Double>   confLow;
        public final List<Double>   confHigh;
        public final List<TrendStat>    stats;
        public final List<TrendInsight> insights;

        public TrendDataset(String key, String label, String unit, String chartTitle,
                            List<String> weeks, List<Double> history,
                            List<Double> forecast, List<Double> confLow, List<Double> confHigh,
                            List<TrendStat> stats, List<TrendInsight> insights) {
            this.key        = key;
            this.label      = label;
            this.unit       = unit;
            this.chartTitle = chartTitle;
            this.weeks      = Collections.unmodifiableList(weeks);
            this.history    = Collections.unmodifiableList(history);
            this.forecast   = Collections.unmodifiableList(forecast);
            this.confLow    = Collections.unmodifiableList(confLow);
            this.confHigh   = Collections.unmodifiableList(confHigh);
            this.stats      = Collections.unmodifiableList(stats);
            this.insights   = Collections.unmodifiableList(insights);
        }

        /** Build the 4 ForecastPoints (F1–F4) with standard confidence levels. */
        public List<ForecastPoint> forecastPoints() {
            int[] confs = {92, 88, 83, 77};
            ForecastPoint[] pts = new ForecastPoint[forecast.size()];
            for (int i = 0; i < forecast.size(); i++) {
                pts[i] = new ForecastPoint(
                        "F" + (i + 1),
                        forecast.get(i),
                        confLow.get(i),
                        confHigh.get(i),
                        confs[i]
                );
            }
            return Arrays.asList(pts);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD DATA FACTORY
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns the five stat cards shown at the top of dashboard.html. */
    public static List<StatCard> dashboardStats() {
        return Arrays.asList(
                new StatCard("Total Queries",      "48,302", "↑ 12% vs last week",    "up"),
                new StatCard("Avg. Response Time", "1.4s",   "↓ 0.2s improvement",    "up"),
                new StatCard("Active Users",        "1,847",  "↑ 8% this month",       "up"),
                new StatCard("Error Rate",          "0.3%",   "↑ 0.1% from baseline",  "down"),
                new StatCard("Model Accuracy",      "96.7%",  "↑ 1.2% vs last model",  "up")
        );
    }

    /** Returns the 7-day bar chart data shown in dashboard.html. */
    public static List<DailyBar> dailyQueryBars() {
        return Arrays.asList(
                new DailyBar("Mon", 52),
                new DailyBar("Tue", 68),
                new DailyBar("Wed", 61),
                new DailyBar("Thu", 80),
                new DailyBar("Fri", 74),
                new DailyBar("Sat", 40),
                new DailyBar("Sun", 35)
        );
    }

    /** Returns the four query-category donut slices shown in dashboard.html. */
    public static List<DonutSlice> queryCategories() {
        return Arrays.asList(
                new DonutSlice("Code & Dev",     35, "#0e7fa8"),
                new DonutSlice("Data Analysis",  25, "#8A00C4"),
                new DonutSlice("Writing",        20, "#016601"),
                new DonutSlice("Other",          20, "#b6aa02")
        );
    }

    /** Returns the five most recent model runs shown in the dashboard table. */
    public static List<ModelRun> recentModelRuns() {
        return Arrays.asList(
                new ModelRun(2048, "JARVIS-4", 97.1, RunStatus.PASSED),
                new ModelRun(2047, "JARVIS-4", 96.8, RunStatus.PASSED),
                new ModelRun(2046, "JARVIS-3", 94.2, RunStatus.REVIEW),
                new ModelRun(2045, "JARVIS-3", 93.9, RunStatus.REVIEW),
                new ModelRun(2044, "JARVIS-2", 89.1, RunStatus.FAILED)
        );
    }

    /** Returns the system activity feed shown in dashboard.html. */
    public static List<ActivityItem> systemActivity() {
        return Arrays.asList(
                new ActivityItem("Model JARVIS-4 deployed to production",    "2 minutes ago",  "info"),
                new ActivityItem("Weekly accuracy report generated",         "18 minutes ago", "ok"),
                new ActivityItem("Latency spike detected — resolved",        "1 hour ago",     "warn"),
                new ActivityItem("Training dataset updated (2.1M records)",  "3 hours ago",    "event"),
                new ActivityItem("Automated backup completed successfully",   "6 hours ago",    "ok")
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRENDS DATA FACTORY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all four trend datasets keyed by their dataset ID, matching the
     * DATASETS constant in trends.html. Use {@code allDatasets().get("queries")}
     * etc. to retrieve a specific dataset.
     */
    public static Map<String, TrendDataset> allDatasets() {
        Map<String, TrendDataset> map = new LinkedHashMap<>();
        map.put("queries",  queriesDataset());
        map.put("accuracy", accuracyDataset());
        map.put("latency",  latencyDataset());
        map.put("users",    usersDataset());
        return Collections.unmodifiableMap(map);
    }

    // ── Query Volume ──────────────────────────────────────────────────────────

    private static TrendDataset queriesDataset() {
        return new TrendDataset(
                "queries",
                "Query Volume",
                "k queries",
                "Query Volume — 12-Week History + 4-Week Forecast",
                weeks(),
                Arrays.asList(38.0, 41.0, 37.0, 44.0, 43.0, 47.0, 45.0, 50.0, 49.0, 53.0, 51.0, 55.0),
                Arrays.asList(57.0, 60.0, 59.0, 63.0),
                Arrays.asList(54.0, 56.0, 54.0, 57.0),
                Arrays.asList(60.0, 64.0, 64.0, 69.0),
                Arrays.asList(
                        new TrendStat("Current Value",   "55k",      "Week 12",         "neutral"),
                        new TrendStat("4-Week Forecast",  "63k",      "peak projection", "pred"),
                        new TrendStat("Growth Rate",      "+14.8%",   "last 12 weeks",   "up"),
                        new TrendStat("Trend Signal",     "Strong ↑", "upward trend",    "up")
                ),
                Arrays.asList(
                        new TrendInsight("Consistent Upward Trend",
                                "Query volume has grown 14.8% over 12 weeks with no sign of plateau. "
                              + "Week-over-week variance is decreasing, indicating a maturing growth pattern.",
                                "up"),
                        new TrendInsight("Mid-Week Volume Spike",
                                "Tuesdays and Thursdays consistently show 18–22% higher volume than adjacent days. "
                              + "Consider scaling capacity ahead of mid-week peaks.",
                                "anomaly"),
                        new TrendInsight("Seasonality Detected",
                                "A recurring 4-week cycle is visible in the data. "
                              + "The model predicts the next local peak at Week 14 (~63k), followed by a mild correction.",
                                "info"),
                        new TrendInsight("Forecast Confidence: High",
                                "4-week MAPE is estimated at 3.2%. "
                              + "The confidence band narrows over the first two weeks, widening only at the 4-week horizon.",
                                "ok")
                )
        );
    }

    // ── Model Accuracy ────────────────────────────────────────────────────────

    private static TrendDataset accuracyDataset() {
        return new TrendDataset(
                "accuracy",
                "Model Accuracy",
                "% accuracy",
                "Model Accuracy — 12-Week History + 4-Week Forecast",
                weeks(),
                Arrays.asList(89.1, 90.4, 91.0, 92.3, 92.8, 93.5, 93.9, 94.2, 95.0, 95.8, 96.4, 96.7),
                Arrays.asList(97.0, 97.2, 97.4, 97.5),
                Arrays.asList(96.5, 96.6, 96.7, 96.7),
                Arrays.asList(97.5, 97.8, 98.1, 98.3),
                Arrays.asList(
                        new TrendStat("Current Accuracy", "96.7%",  "Week 12",          "neutral"),
                        new TrendStat("Predicted Peak",   "97.5%",  "4-week horizon",   "pred"),
                        new TrendStat("Improvement",      "+7.6pp", "last 12 weeks",    "up"),
                        new TrendStat("Trend Signal",     "Stable ↑","marginal gains",  "up")
                ),
                Arrays.asList(
                        new TrendInsight("Steady Accuracy Gains",
                                "Accuracy improved 7.6 percentage points across 12 weeks. "
                              + "Rate of improvement is slowing (+0.3 pp/week → +0.15 pp/week), typical of a maturing model.",
                                "up"),
                        new TrendInsight("Approaching Asymptote",
                                "Predicted trajectory suggests accuracy will plateau near 97.6–98% within 6 weeks. "
                              + "Diminishing returns signal a need for architecture review.",
                                "info"),
                        new TrendInsight("JARVIS-3 Degradation",
                                "Runs #2045–2046 showed a 2.1% drop from the JARVIS-4 baseline. "
                              + "Monitor closely during rollouts.",
                                "anomaly"),
                        new TrendInsight("Forecast Confidence: High",
                                "4-week MAE estimated at 0.18%. "
                              + "The accuracy trajectory shows low volatility, making this the most predictable metric.",
                                "ok")
                )
        );
    }

    // ── Response Latency ──────────────────────────────────────────────────────

    private static TrendDataset latencyDataset() {
        return new TrendDataset(
                "latency",
                "Response Latency",
                "s avg latency",
                "Avg. Response Latency — 12-Week History + 4-Week Forecast",
                weeks(),
                Arrays.asList(2.4, 2.3, 2.5, 2.2, 2.1, 2.0, 1.9, 1.8, 1.7, 1.6, 1.5, 1.4),
                Arrays.asList(1.35, 1.3, 1.28, 1.25),
                Arrays.asList(1.2,  1.15, 1.12, 1.08),
                Arrays.asList(1.5,  1.45, 1.44, 1.42),
                Arrays.asList(
                        new TrendStat("Current Latency",   "1.4s",   "Week 12",           "neutral"),
                        new TrendStat("Predicted Latency", "1.25s",  "4-week target",     "pred"),
                        new TrendStat("Improvement",       "−41.7%", "last 12 weeks",     "up"),
                        new TrendStat("Trend Signal",      "Strong ↓","latency falling",  "up")
                ),
                Arrays.asList(
                        new TrendInsight("Sustained Latency Reduction",
                                "Average response time fell from 2.4s to 1.4s — a 41.7% improvement. "
                              + "Optimizations from the JARVIS-4 deployment account for the steepest drop in Weeks 8–10.",
                                "down"),
                        new TrendInsight("Spike Anomaly — Week 3",
                                "A 0.2s regression was observed in Week 3, correlating with a training dataset refresh. "
                              + "The model corrected within the same period; no action needed.",
                                "anomaly"),
                        new TrendInsight("Approaching Floor",
                                "Projected latency of 1.25s approaches hardware-bound limits. "
                              + "Further gains will likely require inference caching or edge deployment strategies.",
                                "info"),
                        new TrendInsight("Forecast Confidence: Medium",
                                "Confidence band widens at the 3-week horizon due to planned infrastructure changes. "
                              + "Recommend recomputing after the Week 13 deployment window.",
                                "ok")
                )
        );
    }

    // ── Active Users ──────────────────────────────────────────────────────────

    private static TrendDataset usersDataset() {
        return new TrendDataset(
                "users",
                "Active Users",
                "k users",
                "Active Users — 12-Week History + 4-Week Forecast",
                weeks(),
                Arrays.asList(1.1, 1.2, 1.15, 1.3, 1.35, 1.4, 1.45, 1.5, 1.6, 1.65, 1.75, 1.85),
                Arrays.asList(1.93, 2.01, 2.1, 2.2),
                Arrays.asList(1.85, 1.92, 1.98, 2.05),
                Arrays.asList(2.01, 2.1,  2.22, 2.35),
                Arrays.asList(
                        new TrendStat("Current Users",    "1.85k",  "Week 12",            "neutral"),
                        new TrendStat("Projected Users",  "2.2k",   "4-week forecast",    "pred"),
                        new TrendStat("Growth Rate",      "+68.2%", "last 12 weeks",      "up"),
                        new TrendStat("Trend Signal",     "Strong ↑","accelerating",      "up")
                ),
                Arrays.asList(
                        new TrendInsight("Accelerating User Growth",
                                "Active users grew 68.2% over 12 weeks with week-over-week growth accelerating "
                              + "from +9% to +14% in the last month. Viral or referral channels may be activating.",
                                "up"),
                        new TrendInsight("2k User Milestone Incoming",
                                "The model forecasts crossing 2,000 active users in Week 13–14. "
                              + "Proactively scaling session management and queue depth is recommended.",
                                "info"),
                        new TrendInsight("Week 3 Dip — Monitor Pattern",
                                "A minor dip in Week 3 mirrors the same week in latency and query datasets. "
                              + "A shared external factor may have suppressed engagement.",
                                "anomaly"),
                        new TrendInsight("Forecast Confidence: High",
                                "4-week MAPE is estimated at 4.1%. Growth trajectory is smooth and consistent, "
                              + "making the user metric the most reliable for capacity planning.",
                                "ok")
                )
        );
    }

    // ── Shared ───────────────────────────────────────────────────────────────

    private static List<String> weeks() {
        return Arrays.asList("W1","W2","W3","W4","W5","W6","W7","W8","W9","W10","W11","W12");
    }
}
