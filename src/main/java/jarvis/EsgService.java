package jarvis;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jarvis.JarvisData.EsgDataset;
import jarvis.JarvisData.EsgForecast;
import jarvis.JarvisData.EsgMetric;
import jarvis.JarvisData.TrendDataset;
import jarvis.JarvisData.TrendInsight;
import jarvis.JarvisData.TrendStat;

/**
 * EsgService — operations on the six ESG metrics available in esg-checker.html.
 *
 * <p>Mirrors the "Insert into Trends" flow: select metrics, preview their
 * 12-week history, build forecast data, and produce a {@link TrendDataset}
 * payload that can be pushed to the Trends dashboard.
 *
 * <p>Available metric keys:
 * {@code "coverage"}, {@code "completeness"}, {@code "critical"},
 * {@code "major"}, {@code "emissions"}, {@code "social"}.
 */
public final class EsgService {

    private final List<EsgMetric> allMetrics;
    /** Keys of the currently selected metrics — order is insertion order. */
    private final Set<String> selected;

    /**
     * Constructs a service with the default two metrics pre-selected
     * (coverage + completeness), matching the browser UI default.
     */
    public EsgService() {
        this.allMetrics = JarvisData.allEsgMetrics();
        this.selected   = new LinkedHashSet<>();
        this.selected.add("coverage");
        this.selected.add("completeness");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // METRIC SELECTION
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all available ESG metrics. */
    public List<EsgMetric> getAllMetrics() {
        return allMetrics;
    }

    /**
     * Returns the metric for the given key, or empty if unknown.
     *
     * @param key metric key, e.g. {@code "coverage"}
     */
    public Optional<EsgMetric> findMetric(String key) {
        return allMetrics.stream().filter(m -> m.key.equals(key)).findFirst();
    }

    /** Selects a metric by key. No-op if already selected or key unknown. */
    public void select(String key) {
        if (findMetric(key).isPresent()) selected.add(key);
    }

    /** Deselects a metric by key. No-op if not selected. */
    public void deselect(String key) {
        selected.remove(key);
    }

    /** Replaces the entire selection with the given keys (order preserved). */
    public void setSelection(List<String> keys) {
        selected.clear();
        keys.forEach(this::select);
    }

    /** Returns the currently selected metric keys, in insertion order. */
    public List<String> getSelectedKeys() {
        return List.copyOf(selected);
    }

    /** Returns the currently selected {@link EsgMetric} objects, in insertion order. */
    public List<EsgMetric> getSelectedMetrics() {
        return selected.stream()
                .map(k -> findMetric(k).orElseThrow())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HISTORY & FORECAST
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds and returns the {@link EsgDataset} for a given metric key.
     *
     * @param key metric key
     * @throws IllegalArgumentException if the key is unknown
     */
    public EsgDataset datasetFor(String key) {
        EsgMetric m = findMetric(key)
                .orElseThrow(() -> new IllegalArgumentException("Unknown ESG metric key: " + key));
        return new EsgDataset(m);
    }

    /**
     * Returns ESG datasets for all currently selected metrics.
     */
    public List<EsgDataset> selectedDatasets() {
        return getSelectedMetrics().stream()
                .map(EsgDataset::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns the selected metric whose latest history value represents
     * the greatest improvement (highest positive change) over 12 weeks.
     */
    public Optional<EsgDataset> mostImprovedMetric() {
        return selectedDatasets().stream()
                .max(Comparator.comparingDouble(EsgDataset::totalChange));
    }

    /**
     * Returns the selected metric with the steepest decline over 12 weeks.
     * Useful for flagging which ESG area needs the most attention.
     */
    public Optional<EsgDataset> mostDecliningMetric() {
        return selectedDatasets().stream()
                .min(Comparator.comparingDouble(EsgDataset::totalChange));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUSH TO TRENDS — PAYLOAD BUILDER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a {@link TrendDataset} from the primary (first selected) metric,
     * ready to be pushed to the Trends dashboard — mirrors the
     * {@code pushBtn} click handler in esg-checker.html exactly.
     *
     * <p>If no metrics are selected, throws {@link IllegalStateException}.
     *
     * @return a TrendDataset with key {@code "reports"} sourced from ESG data
     */
    public TrendDataset buildTrendPayload() {
        List<EsgMetric> active = getSelectedMetrics();
        if (active.isEmpty()) {
            throw new IllegalStateException("No ESG metrics selected — call select() first.");
        }
        EsgMetric primary  = active.get(0);
        EsgDataset ds      = new EsgDataset(primary);
        EsgForecast fc     = ds.forecast;
        int n              = ds.history.size();
        double totalChange = ds.totalChange();

        List<TrendStat> stats = List.of(
            new TrendStat("Current Value",
                String.format("%.2f", ds.latestValue()), "Week 12", "neutral"),
            new TrendStat("4-Week Forecast",
                String.format("%.2f", fc.forecast.get(3)), "projected", "pred"),
            new TrendStat("Total Change",
                String.format("%+.2f", totalChange), "over 12 weeks",
                totalChange >= 0 ? "up" : "down"),
            new TrendStat("Trend Signal",
                fc.trendLabel(), fc.trendWord() + " trend",
                totalChange >= 0 ? "up" : "down")
        );

        List<TrendInsight> insights = List.of(
            new TrendInsight(primary.label + ": " + fc.trendWord() + " trend",
                String.format("Over the last 12 weeks, %s moved from %.2f to %.2f. "
                        + "The %s trajectory is expected to continue into the forecast period.",
                        primary.label.toLowerCase(),
                        ds.history.get(0), ds.latestValue(), fc.trendWord()),
                fc.slope > 0.01 ? "up" : fc.slope < -0.01 ? "down" : "info"),
            new TrendInsight(String.format("4-Week Forecast: %.2f", fc.forecast.get(3)),
                String.format("J.A.R.V.I.S projects %s to reach %.2f within 4 periods. "
                        + "Confidence narrows at the 3–4 week horizon.",
                        primary.label.toLowerCase(), fc.forecast.get(3)),
                "info"),
            new TrendInsight(active.size() > 1
                    ? active.size() + " ESG metrics selected"
                    : "1 ESG metric selected",
                "Metrics: " + active.stream().map(m -> m.label).collect(Collectors.joining(", "))
                        + ". Re-run the ESG Checker and push again to update the trend history.",
                "anomaly"),
            new TrendInsight("Data sourced from ESG Report Analysis",
                "This dataset reflects ESG metric performance derived from uploaded report files.",
                "ok")
        );

        return new TrendDataset(
            "reports", primary.label, primary.unit,
            primary.label + " \u2014 12-Week ESG History + 4-Week Forecast",
            ds.weeks, ds.history, fc.forecast, fc.confLow, fc.confHigh,
            stats, insights
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUMMARY REPORT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Prints a formatted summary of all ESG metrics and their 12-week trend
     * to stdout — mirrors the metric grid in esg-checker.html.
     */
    public void printMetricSummary() {
        println("═══════════════════════════════════════════════════════════");
        println("  J.A.R.V.I.S  —  ESG Metrics Summary");
        println("═══════════════════════════════════════════════════════════");
        println(String.format("  %-30s  %-10s  %-8s  %-8s  %s",
                "Metric", "Latest", "W1", "Change", "Trend"));
        println("  ─────────────────────────────────────────────────────────────");
        for (EsgMetric m : allMetrics) {
            EsgDataset ds = new EsgDataset(m);
            boolean isSelected = selected.contains(m.key);
            println(String.format("  %s%-30s  %-10.2f  %-8.2f  %+8.2f  %s",
                    isSelected ? "* " : "  ",
                    m.label,
                    ds.latestValue(),
                    ds.history.get(0),
                    ds.totalChange(),
                    ds.forecast.trendLabel()));
        }
        println("\n  (* = currently selected)");
        println("\n═══════════════════════════════════════════════════════════");
    }

    private static void println(String s) {
        System.out.println(s);
    }
}
