package jarvis;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jarvis.JarvisData.ForecastPoint;
import jarvis.JarvisData.TrendDataset;
import jarvis.JarvisData.TrendInsight;
import jarvis.JarvisData.TrendStat;

/**
 * TrendsService — operations on the four trend datasets shown in trends.html.
 *
 * Datasets available: {@code "queries"}, {@code "accuracy"},
 * {@code "latency"}, {@code "users"}.
 */
public final class TrendsService {

    private final Map<String, TrendDataset> datasets;

    /** Constructs a service backed by the canonical UI data. */
    public TrendsService() {
        this.datasets = JarvisData.allDatasets();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATASET ACCESS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all dataset keys available for analysis.
     *
     * @return ordered list: ["queries", "accuracy", "latency", "users"]
     */
    public List<String> availableDatasets() {
        return List.copyOf(datasets.keySet());
    }

    /**
     * Retrieves a dataset by its key.
     *
     * @param key one of "queries", "accuracy", "latency", "users"
     * @return the matching dataset, or empty if the key is unknown
     */
    public Optional<TrendDataset> getDataset(String key) {
        return Optional.ofNullable(datasets.get(key));
    }

    /**
     * Retrieves a dataset by key, throwing if not found.
     *
     * @throws IllegalArgumentException for unknown keys
     */
    public TrendDataset requireDataset(String key) {
        TrendDataset ds = datasets.get(key);
        if (ds == null) {
            throw new IllegalArgumentException(
                    "Unknown dataset key '" + key + "'. Valid keys: " + datasets.keySet());
        }
        return ds;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HISTORICAL SERIES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the historical weekly values for a dataset.
     *
     * @param key dataset key
     */
    public List<Double> history(String key) {
        return requireDataset(key).history;
    }

    /**
     * Returns the most recent (latest) historical value.
     *
     * @param key dataset key
     */
    public double latestValue(String key) {
        List<Double> h = history(key);
        return h.get(h.size() - 1);
    }

    /**
     * Computes the percentage change from the first historical value to the last.
     *
     * @param key dataset key
     * @return percentage change (can be negative for improving latency, etc.)
     */
    public double overallChangePercent(String key) {
        List<Double> h = history(key);
        double first = h.get(0);
        double last  = h.get(h.size() - 1);
        return ((last - first) / Math.abs(first)) * 100.0;
    }

    /**
     * Returns the minimum value in the historical series.
     *
     * @param key dataset key
     */
    public double historyMin(String key) {
        return history(key).stream().mapToDouble(Double::doubleValue).min().orElseThrow();
    }

    /**
     * Returns the maximum value in the historical series.
     *
     * @param key dataset key
     */
    public double historyMax(String key) {
        return history(key).stream().mapToDouble(Double::doubleValue).max().orElseThrow();
    }

    /**
     * Computes the mean of the historical series.
     *
     * @param key dataset key
     */
    public double historyMean(String key) {
        return history(key).stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FORECAST
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the 4 forecast data points with confidence intervals and
     * confidence percentage — mirrors the Predicted Values table in the UI.
     *
     * @param key dataset key
     */
    public List<ForecastPoint> forecast(String key) {
        return requireDataset(key).forecastPoints();
    }

    /**
     * Returns the peak (max) forecast value across the 4-week horizon.
     *
     * @param key dataset key
     */
    public double peakForecast(String key) {
        return requireDataset(key).forecast.stream()
                .mapToDouble(Double::doubleValue).max().orElseThrow();
    }

    /**
     * Returns the forecast point with the highest confidence score.
     *
     * @param key dataset key
     */
    public ForecastPoint highestConfidenceForecast(String key) {
        return forecast(key).stream()
                .max(Comparator.comparingInt(fp -> fp.confidencePercent))
                .orElseThrow();
    }

    /**
     * Returns the forecast point with the lowest confidence score
     * (widest uncertainty, furthest into the future).
     *
     * @param key dataset key
     */
    public ForecastPoint lowestConfidenceForecast(String key) {
        return forecast(key).stream()
                .min(Comparator.comparingInt(fp -> fp.confidencePercent))
                .orElseThrow();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INSIGHTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all AI insights for the given dataset.
     *
     * @param key dataset key
     */
    public List<TrendInsight> insights(String key) {
        return requireDataset(key).insights;
    }

    /**
     * Returns insights of a given type ("up", "down", "anomaly", "info", "ok").
     *
     * @param key  dataset key
     * @param type insight type to filter by
     */
    public List<TrendInsight> insightsByType(String key, String type) {
        return insights(key).stream()
                .filter(i -> i.type.equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * Returns all anomaly-flagged insights across all datasets.
     * Useful for an alert-style view of everything that needs attention.
     */
    public List<TrendInsight> allAnomalies() {
        return datasets.values().stream()
                .flatMap(ds -> ds.insights.stream())
                .filter(i -> "anomaly".equalsIgnoreCase(i.type))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAT CARDS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the four mini stat cards for the given dataset.
     *
     * @param key dataset key
     */
    public List<TrendStat> stats(String key) {
        return requireDataset(key).stats;
    }

    /**
     * Returns the "pred" (yellow forecast) stat card for a dataset, if present.
     *
     * @param key dataset key
     */
    public Optional<TrendStat> forecastStat(String key) {
        return stats(key).stream()
                .filter(s -> "pred".equalsIgnoreCase(s.cls))
                .findFirst();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CROSS-DATASET ANALYSIS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the dataset key with the highest week-over-week growth rate
     * computed from the last two historical data points.
     */
    public String fastestGrowingDataset() {
        return datasets.values().stream()
                .max(Comparator.comparingDouble(ds -> {
                    List<Double> h = ds.history;
                    double prev = h.get(h.size() - 2);
                    double last = h.get(h.size() - 1);
                    return (last - prev) / Math.abs(prev);
                }))
                .map(ds -> ds.key)
                .orElseThrow();
    }

    /**
     * Returns the dataset key whose latest history value is furthest below
     * the lower bound of its F1 confidence interval —
     * i.e. the metric most likely to beat its own forecast.
     */
    public String mostOptimisticDataset() {
        return datasets.values().stream()
                .max(Comparator.comparingDouble(ds -> {
                    double latest = ds.history.get(ds.history.size() - 1);
                    double f1Low  = ds.confLow.get(0);
                    return f1Low - latest; // positive = latest already above lower CI
                }))
                .map(ds -> ds.key)
                .orElseThrow();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUMMARY REPORT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Prints a formatted trends analysis report for a single dataset to stdout.
     *
     * @param key dataset key
     */
    public void printDatasetReport(String key) {
        TrendDataset ds = requireDataset(key);
        println("═══════════════════════════════════════════════════════════");
        println("  J.A.R.V.I.S  —  Trend Report: " + ds.label + " (" + ds.unit + ")");
        println("═══════════════════════════════════════════════════════════");

        println("\n── Historical Series (" + ds.weeks.size() + " weeks) ──────────────────────────");
        for (int i = 0; i < ds.weeks.size(); i++) {
            println(String.format("  %-4s  %.2f", ds.weeks.get(i), ds.history.get(i)));
        }
        println(String.format("  Overall change: %+.1f%%  |  Min: %.2f  |  Max: %.2f  |  Mean: %.2f",
                overallChangePercent(key), historyMin(key), historyMax(key), historyMean(key)));

        println("\n── 4-Week Forecast ─────────────────────────────────────────");
        forecast(key).forEach(fp -> println("  " + fp));

        println("\n── Stat Cards ──────────────────────────────────────────────");
        stats(key).forEach(s -> println("  " + s));

        println("\n── AI Insights ─────────────────────────────────────────────");
        insights(key).forEach(i -> println("  " + i));

        println("\n═══════════════════════════════════════════════════════════");
    }

    /**
     * Prints a cross-dataset comparison summary to stdout.
     */
    public void printCrossDatasetSummary() {
        println("═══════════════════════════════════════════════════════════");
        println("  J.A.R.V.I.S  —  Cross-Dataset Trends Summary");
        println("═══════════════════════════════════════════════════════════");
        println("\n  Dataset              Latest       12-wk Change   Peak Forecast");
        println("  ─────────────────────────────────────────────────────────────");
        for (TrendDataset ds : datasets.values()) {
            println(String.format("  %-20s %-12.2f %+.1f%%          %.2f",
                    ds.label,
                    latestValue(ds.key),
                    overallChangePercent(ds.key),
                    peakForecast(ds.key)));
        }
        println("\n  Fastest growing dataset : " + fastestGrowingDataset());
        println("  Most anomalies flagged  : " + allAnomalies().size() + " total across all datasets");
        println("\n═══════════════════════════════════════════════════════════");
    }

    private static void println(String s) {
        System.out.println(s);
    }
}
