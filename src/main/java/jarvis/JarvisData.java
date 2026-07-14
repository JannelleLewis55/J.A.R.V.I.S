package jarvis;

import java.util.Arrays;
import java.util.ArrayList;
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
        /** Absolute number of queries submitted that day. */
        public final int queryCount;

        public DailyBar(String day, int heightPercent, int queryCount) {
            this.day           = day;
            this.heightPercent = heightPercent;
            this.queryCount    = queryCount;
        }

        @Override
        public String toString() {
            return String.format("%s: %d%%  (%,d queries)", day, heightPercent, queryCount);
        }
    }

    /** Category values for a submitted query. */
    public enum QueryCategory { CODE, DATA, WRITING, OTHER }

    /** A single query submitted on a given day — mirrors the drill-down list in dashboard.html. */
    public static final class DailyQuery {
        public final int           number;
        public final QueryCategory category;
        public final String        text;

        public DailyQuery(int number, QueryCategory category, String text) {
            this.number   = number;
            this.category = category;
            this.text     = text;
        }

        @Override
        public String toString() {
            return String.format("%2d. [%-7s] %s", number, category, text);
        }
    }

    /** All drill-down data for one day: total count + the query list. */
    public static final class DailyQueryData {
        public final String           day;
        public final int              count;
        public final List<DailyQuery> queries;

        public DailyQueryData(String day, int count, List<DailyQuery> queries) {
            this.day     = day;
            this.count   = count;
            this.queries = Collections.unmodifiableList(queries);
        }

        @Override
        public String toString() {
            return String.format("%s — %,d queries submitted", day, count);
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
                new DailyBar("Mon", 52, 6420),
                new DailyBar("Tue", 68, 8391),
                new DailyBar("Wed", 61, 7532),
                new DailyBar("Thu", 80, 9874),
                new DailyBar("Fri", 74, 9132),
                new DailyBar("Sat", 40, 4939),
                new DailyBar("Sun", 35, 4314)
        );
    }

    /**
     * Returns the full per-day drill-down data for all 7 days, keyed by day name.
     * Mirrors the {@code DAILY_QUERIES} constant in dashboard.html.
     */
    public static Map<String, DailyQueryData> allDailyQueryData() {
        Map<String, DailyQueryData> map = new LinkedHashMap<>();
        map.put("Mon", monQueries());
        map.put("Tue", tueQueries());
        map.put("Wed", wedQueries());
        map.put("Thu", thuQueries());
        map.put("Fri", friQueries());
        map.put("Sat", satQueries());
        map.put("Sun", sunQueries());
        return Collections.unmodifiableMap(map);
    }

    private static DailyQueryData monQueries() {
        return new DailyQueryData("Mon", 6420, Arrays.asList(
            new DailyQuery(1,  QueryCategory.CODE,    "How do I reverse a linked list in Python?"),
            new DailyQuery(2,  QueryCategory.DATA,    "Analyze the sales dataset and show me weekly trends"),
            new DailyQuery(3,  QueryCategory.WRITING, "Draft an executive summary for the Q3 report"),
            new DailyQuery(4,  QueryCategory.CODE,    "Fix the null pointer exception in my Java service"),
            new DailyQuery(5,  QueryCategory.OTHER,   "What is the capital of New Zealand?"),
            new DailyQuery(6,  QueryCategory.DATA,    "Generate a bar chart from this CSV data"),
            new DailyQuery(7,  QueryCategory.CODE,    "Explain the difference between async/await and promises"),
            new DailyQuery(8,  QueryCategory.WRITING, "Rewrite this paragraph to sound more professional"),
            new DailyQuery(9,  QueryCategory.OTHER,   "Summarize the latest AI news"),
            new DailyQuery(10, QueryCategory.CODE,    "Write a SQL query to find duplicate rows")
        ));
    }

    private static DailyQueryData tueQueries() {
        return new DailyQueryData("Tue", 8391, Arrays.asList(
            new DailyQuery(1,  QueryCategory.CODE,    "Build a REST API endpoint in Node.js for user authentication"),
            new DailyQuery(2,  QueryCategory.DATA,    "What are the top 5 products by revenue this quarter?"),
            new DailyQuery(3,  QueryCategory.CODE,    "Why is my Docker container crashing on startup?"),
            new DailyQuery(4,  QueryCategory.WRITING, "Write a LinkedIn post announcing our product launch"),
            new DailyQuery(5,  QueryCategory.DATA,    "Compare model accuracy across the last 5 training runs"),
            new DailyQuery(6,  QueryCategory.CODE,    "Implement binary search in TypeScript"),
            new DailyQuery(7,  QueryCategory.OTHER,   "Translate this email to Spanish"),
            new DailyQuery(8,  QueryCategory.WRITING, "Generate meeting notes from this transcript"),
            new DailyQuery(9,  QueryCategory.CODE,    "How do I debounce a search input in React?"),
            new DailyQuery(10, QueryCategory.DATA,    "Show me a pivot table of user signups by region"),
            new DailyQuery(11, QueryCategory.CODE,    "Explain SOLID principles with examples"),
            new DailyQuery(12, QueryCategory.OTHER,   "What is the weather like in London today?")
        ));
    }

    private static DailyQueryData wedQueries() {
        return new DailyQueryData("Wed", 7532, Arrays.asList(
            new DailyQuery(1,  QueryCategory.DATA,    "Run anomaly detection on the latency logs"),
            new DailyQuery(2,  QueryCategory.CODE,    "Convert this class component to a React hook"),
            new DailyQuery(3,  QueryCategory.WRITING, "Create a project status update for stakeholders"),
            new DailyQuery(4,  QueryCategory.CODE,    "Set up a CI/CD pipeline with GitHub Actions"),
            new DailyQuery(5,  QueryCategory.DATA,    "Cluster these customer records by purchase behaviour"),
            new DailyQuery(6,  QueryCategory.OTHER,   "What does ESG stand for and why does it matter?"),
            new DailyQuery(7,  QueryCategory.CODE,    "Add rate limiting to my Express API"),
            new DailyQuery(8,  QueryCategory.WRITING, "Proofread and improve this technical specification"),
            new DailyQuery(9,  QueryCategory.DATA,    "Forecast next month's server costs based on usage trends"),
            new DailyQuery(10, QueryCategory.CODE,    "Explain the CAP theorem in simple terms")
        ));
    }

    private static DailyQueryData thuQueries() {
        return new DailyQueryData("Thu", 9874, Arrays.asList(
            new DailyQuery(1,  QueryCategory.CODE,    "Design a microservices architecture for an e-commerce platform"),
            new DailyQuery(2,  QueryCategory.DATA,    "Identify which features have the highest correlation with churn"),
            new DailyQuery(3,  QueryCategory.CODE,    "Debug this memory leak in my Go service"),
            new DailyQuery(4,  QueryCategory.WRITING, "Write a proposal for migrating to a cloud-native stack"),
            new DailyQuery(5,  QueryCategory.CODE,    "Implement a JWT refresh token strategy"),
            new DailyQuery(6,  QueryCategory.DATA,    "Build a dashboard showing real-time query volume"),
            new DailyQuery(7,  QueryCategory.OTHER,   "What are the best practices for remote team management?"),
            new DailyQuery(8,  QueryCategory.CODE,    "Optimise this slow PostgreSQL query"),
            new DailyQuery(9,  QueryCategory.DATA,    "Generate a heatmap of user activity by hour of day"),
            new DailyQuery(10, QueryCategory.WRITING, "Draft an incident post-mortem for the Tuesday outage"),
            new DailyQuery(11, QueryCategory.CODE,    "Add OpenAPI documentation to my Flask endpoints"),
            new DailyQuery(12, QueryCategory.DATA,    "What is the 90th percentile response time this week?"),
            new DailyQuery(13, QueryCategory.OTHER,   "Explain transformer architecture for a non-technical audience")
        ));
    }

    private static DailyQueryData friQueries() {
        return new DailyQueryData("Fri", 9132, Arrays.asList(
            new DailyQuery(1,  QueryCategory.WRITING, "Write the weekly engineering update email"),
            new DailyQuery(2,  QueryCategory.CODE,    "Create a Python script to automate the report generation"),
            new DailyQuery(3,  QueryCategory.DATA,    "Compare this week's KPIs against last week"),
            new DailyQuery(4,  QueryCategory.CODE,    "Fix the race condition in the task queue worker"),
            new DailyQuery(5,  QueryCategory.OTHER,   "What are some team-building activities for remote teams?"),
            new DailyQuery(6,  QueryCategory.DATA,    "Predict weekend traffic based on historical patterns"),
            new DailyQuery(7,  QueryCategory.CODE,    "Write unit tests for the authentication module"),
            new DailyQuery(8,  QueryCategory.WRITING, "Summarize this research paper on LLM fine-tuning"),
            new DailyQuery(9,  QueryCategory.DATA,    "Show error rate breakdown by endpoint for today"),
            new DailyQuery(10, QueryCategory.CODE,    "How do I implement retry logic with exponential backoff?"),
            new DailyQuery(11, QueryCategory.OTHER,   "Generate ideas for the Q4 product roadmap")
        ));
    }

    private static DailyQueryData satQueries() {
        return new DailyQueryData("Sat", 4939, Arrays.asList(
            new DailyQuery(1, QueryCategory.OTHER,   "What are some weekend side project ideas in AI?"),
            new DailyQuery(2, QueryCategory.CODE,    "Build a simple chatbot with Python and Ollama"),
            new DailyQuery(3, QueryCategory.DATA,    "Explore the open NYC taxi dataset and find patterns"),
            new DailyQuery(4, QueryCategory.WRITING, "Write a blog post about building AI-powered tools"),
            new DailyQuery(5, QueryCategory.CODE,    "How do I deploy a model to Hugging Face Spaces?"),
            new DailyQuery(6, QueryCategory.OTHER,   "Recommend books on systems design"),
            new DailyQuery(7, QueryCategory.DATA,    "What machine learning model is best for time-series forecasting?")
        ));
    }

    private static DailyQueryData sunQueries() {
        return new DailyQueryData("Sun", 4314, Arrays.asList(
            new DailyQuery(1, QueryCategory.OTHER,   "Plan my week using the Eisenhower matrix"),
            new DailyQuery(2, QueryCategory.WRITING, "Write a personal OKR template for Q4"),
            new DailyQuery(3, QueryCategory.CODE,    "Explain how large language models are trained"),
            new DailyQuery(4, QueryCategory.DATA,    "Show a summary of my activity from this week"),
            new DailyQuery(5, QueryCategory.OTHER,   "What productivity tools do top engineers recommend?"),
            new DailyQuery(6, QueryCategory.CODE,    "Create a Markdown cheat sheet")
        ));
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
    // ESG DATA MODELS & FACTORY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Definition of one ESG metric that can be pushed to the Trends dashboard.
     * Mirrors the {@code ESG_METRICS} array in esg-checker.html.
     */
    public static final class EsgMetric {
        public final String key;
        public final String label;
        public final String description;
        public final String unit;
        public final String color;
        /** Seed used for the deterministic history generator. */
        public final int    seed;
        /** Starting value for the history series. */
        public final double baseVal;
        /** Weekly trend increment (can be negative). */
        public final double trend;

        public EsgMetric(String key, String label, String description,
                         String unit, String color, int seed,
                         double baseVal, double trend) {
            this.key         = key;
            this.label       = label;
            this.description = description;
            this.unit        = unit;
            this.color       = color;
            this.seed        = seed;
            this.baseVal     = baseVal;
            this.trend       = trend;
        }

        /**
         * Builds a deterministic 12-week history using the same seeded algorithm
         * as {@code buildHistory()} in esg-checker.html, so Java and browser
         * produce identical values.
         *
         * <p>Formula: {@code baseVal + trend*w + noise}, where
         * {@code noise = (sin(seed + w*7.3)*43758.5453 - floor(…) - 0.5) * baseVal * 0.12}
         */
        public List<Double> buildHistory() {
            List<Double> h = new ArrayList<>(12);
            for (int w = 0; w < 12; w++) {
                double x     = Math.sin(seed + w * 7.3) * 43758.5453;
                double frac  = x - Math.floor(x);          // equivalent to JS (x - Math.floor(x))
                double noise = (frac - 0.5) * baseVal * 0.12;
                double raw   = baseVal + trend * w + noise;
                h.add(Math.round(raw * 100.0) / 100.0);    // 2 dp, matches JS toFixed(2)
            }
            return Collections.unmodifiableList(h);
        }

        /** Builds a 4-period linear forecast from the history, with confidence bands. */
        public EsgForecast buildForecast() {
            List<Double> history = buildHistory();
            int n = history.size();
            double slope = (history.get(n - 1) - history.get(0)) / (n - 1);
            double last  = history.get(n - 1);
            double margin = Math.abs(last) * 0.025;

            List<Double> fc   = new ArrayList<>(4);
            List<Double> low  = new ArrayList<>(4);
            List<Double> high = new ArrayList<>(4);
            for (int i = 1; i <= 4; i++) {
                double v = Math.round((last + slope * i) * 100.0) / 100.0;
                fc.add(v);
                low.add(Math.round((v - margin * i) * 100.0) / 100.0);
                high.add(Math.round((v + margin * i) * 100.0) / 100.0);
            }
            return new EsgForecast(slope, fc, low, high);
        }

        @Override
        public String toString() {
            List<Double> h = buildHistory();
            return String.format("%-30s  latest=%.2f  unit=%s", label, h.get(h.size()-1), unit);
        }
    }

    /** Forecast result produced by {@link EsgMetric#buildForecast()}. */
    public static final class EsgForecast {
        public final double       slope;
        public final List<Double> forecast;
        public final List<Double> confLow;
        public final List<Double> confHigh;

        public EsgForecast(double slope, List<Double> forecast,
                           List<Double> confLow, List<Double> confHigh) {
            this.slope    = slope;
            this.forecast = Collections.unmodifiableList(forecast);
            this.confLow  = Collections.unmodifiableList(confLow);
            this.confHigh = Collections.unmodifiableList(confHigh);
        }

        /** "upward", "downward", or "flat" based on slope magnitude. */
        public String trendWord() {
            if (slope >  0.01) return "upward";
            if (slope < -0.01) return "downward";
            return "flat";
        }

        /** Arrow label for display, e.g. "Improving ↑". */
        public String trendLabel() {
            if (slope >  0.01) return "Improving \u2191";
            if (slope < -0.01) return "Declining \u2193";
            return "Stable \u2192";
        }
    }

    /**
     * A complete ESG dataset ready to be pushed to the Trends dashboard —
     * built from a selected {@link EsgMetric} and its computed history/forecast.
     */
    public static final class EsgDataset {
        public final EsgMetric   metric;
        public final List<Double> history;
        public final List<String> weeks;
        public final EsgForecast  forecast;

        public EsgDataset(EsgMetric metric) {
            this.metric   = metric;
            this.history  = metric.buildHistory();
            this.weeks    = weeks12();
            this.forecast = metric.buildForecast();
        }

        /** Returns the latest (most recent) history value. */
        public double latestValue() {
            return history.get(history.size() - 1);
        }

        /** Returns the total absolute change across the 12-week history. */
        public double totalChange() {
            return history.get(history.size() - 1) - history.get(0);
        }

        @Override
        public String toString() {
            return String.format("%s  [W1=%.2f → W12=%.2f, F4=%.2f, trend=%s]",
                    metric.label, history.get(0), latestValue(),
                    forecast.forecast.get(3), forecast.trendWord());
        }
    }

    /**
     * Returns all six ESG metrics available for push to Trends,
     * matching the {@code ESG_METRICS} array in esg-checker.html exactly.
     */
    public static List<EsgMetric> allEsgMetrics() {
        return Arrays.asList(
            new EsgMetric("coverage",     "Section Coverage Score",
                "Overall % of ESG standard sections with adequate disclosure",
                "% coverage",       "#02fdfb", 101,  52.0,  2.1),
            new EsgMetric("completeness", "Report Completeness",
                "Composite completeness score (0–100) adjusted for gap severity",
                "score /100",       "#22c55e", 202,  48.0,  1.8),
            new EsgMetric("critical",     "Critical Gaps",
                "Number of critical disclosure gaps detected per analysis run",
                "critical gaps",    "#ef4444", 303,   6.0, -0.35),
            new EsgMetric("major",        "Major Gaps",
                "Number of major disclosure gaps detected per analysis run",
                "major gaps",       "#eab308", 404,   5.0, -0.28),
            new EsgMetric("emissions",    "GHG Emissions Intensity",
                "Reported Scope 1+2 emissions intensity (tCO\u2082e per $M revenue)",
                "tCO\u2082e / $M rev", "#8A00C4", 505, 38.4, -1.2),
            new EsgMetric("social",       "Social Coverage Score",
                "Percentage of Social pillar disclosure requirements met",
                "% social coverage","#60a5fa", 606,  44.0,  2.4)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REPORT-BASED PREDICTION FACTORY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the TrendDataset produced when "Analyze &amp; Predict Reports" is
     * clicked in dashboard.html — linear extrapolation over the 5 model run
     * accuracy values, oldest → newest.
     *
     * <p>Mirrors the computation in the {@code analyzeBtn} click handler exactly.
     */
    public static TrendDataset reportsForecastDataset() {
        // Accuracy values from oldest (#2044) to newest (#2048)
        List<Double> history = Arrays.asList(89.1, 93.9, 94.2, 96.8, 97.1);
        List<String> labels  = Arrays.asList("#2044", "#2045", "#2046", "#2047", "#2048");

        int n = history.size();
        double slope = (history.get(n-1) - history.get(0)) / (n - 1);   // 2.0 pp per run

        // 4-run forecast
        double last = history.get(n-1);
        List<Double> forecast = new ArrayList<>(4);
        for (int i = 1; i <= 4; i++) {
            forecast.add(Math.round((last + slope * i) * 100.0) / 100.0);
        }

        // Confidence margins match the JS [0.5, 0.8, 1.1, 1.5]
        double[] margins = {0.5, 0.8, 1.1, 1.5};
        List<Double> confLow  = new ArrayList<>(4);
        List<Double> confHigh = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            confLow.add( Math.round((forecast.get(i) - margins[i]) * 100.0) / 100.0);
            confHigh.add(Math.round((forecast.get(i) + margins[i]) * 100.0) / 100.0);
        }

        double avg        = history.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double totalGain  = history.get(n-1) - history.get(0);
        String trendLabel = totalGain >= 0 ? "Improving \u2191" : "Declining \u2193";

        List<TrendStat> stats = Arrays.asList(
            new TrendStat("Latest Run",      String.format("%.1f%%", history.get(n-1)), labels.get(n-1), "neutral"),
            new TrendStat("4-Run Forecast",  String.format("%.2f%%", forecast.get(3)),  "projected",     "pred"),
            new TrendStat("Total Change",    String.format("%+.1fpp", totalGain),        "across " + n + " runs",
                          totalGain >= 0 ? "up" : "down"),
            new TrendStat("Trend Signal",    trendLabel, "upward trend", "up")
        );

        List<TrendInsight> insights = Arrays.asList(
            new TrendInsight("Overall Accuracy Improving",
                String.format("Across %d report runs, accuracy moved from %.1f%% to %.1f%% " +
                              "— a %+.1f pp change. %.2f pp per run on average.",
                              n, history.get(0), history.get(n-1), totalGain, slope),
                "up"),
            new TrendInsight(String.format("Average Run Accuracy: %.1f%%", avg),
                String.format("The mean accuracy across all %d report runs is %.2f%%. " +
                              "Runs on JARVIS-4 lead the pack; JARVIS-2 remains below the 90%% baseline.",
                              n, avg),
                "info"),
            new TrendInsight("Model Transition Gap",
                "There is a visible accuracy drop at the JARVIS-2 \u2192 JARVIS-3 boundary " +
                "(89.1% \u2192 93.9%). Monitoring accuracy across model rollouts is recommended.",
                "anomaly"),
            new TrendInsight(String.format("4-Run Forecast: %.2f%%", forecast.get(3)),
                String.format("Based on the current trajectory, accuracy is projected to reach " +
                              "%.2f%% in 4 runs. Forecast confidence decreases at the 3\u20134 run horizon.",
                              forecast.get(3)),
                "ok")
        );

        return new TrendDataset(
            "reports", "Report Accuracy", "% accuracy",
            "Model Report Accuracy \u2014 Run History + 4-Run Forecast",
            labels, history, forecast, confLow, confHigh, stats, insights
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

    /** 12-week label list used by ESG datasets ("W1" … "W12"). */
    public static List<String> weeks12() {
        return weeks();
    }
}
