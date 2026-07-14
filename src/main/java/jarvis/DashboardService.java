package jarvis;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jarvis.JarvisData.ActivityItem;
import jarvis.JarvisData.DailyBar;
import jarvis.JarvisData.DonutSlice;
import jarvis.JarvisData.ModelRun;
import jarvis.JarvisData.RunStatus;
import jarvis.JarvisData.StatCard;

/**
 * DashboardService — operations on the dashboard data.
 *
 * Each method works with the static data from {@link JarvisData} and mirrors
 * the information shown in dashboard.html: stat cards, bar chart, donut chart,
 * recent model runs, and the system activity feed.
 */
public final class DashboardService {

    private final List<StatCard>    stats;
    private final List<DailyBar>    dailyBars;
    private final List<DonutSlice>  categories;
    private final List<ModelRun>    runs;
    private final List<ActivityItem> activity;

    /** Constructs a service backed by the canonical UI data. */
    public DashboardService() {
        this.stats      = JarvisData.dashboardStats();
        this.dailyBars  = JarvisData.dailyQueryBars();
        this.categories = JarvisData.queryCategories();
        this.runs       = JarvisData.recentModelRuns();
        this.activity   = JarvisData.systemActivity();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAT CARDS
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all dashboard stat cards. */
    public List<StatCard> getAllStats() {
        return stats;
    }

    /**
     * Looks up a stat card by label (case-insensitive, partial match allowed).
     *
     * @param label partial label to search for, e.g. "accuracy"
     * @return the first matching stat card, or empty if none found
     */
    public Optional<StatCard> findStat(String label) {
        String lc = label.toLowerCase();
        return stats.stream()
                .filter(s -> s.label.toLowerCase().contains(lc))
                .findFirst();
    }

    /** Returns all stat cards where the direction is "up" (positive trend). */
    public List<StatCard> positiveStats() {
        return stats.stream()
                .filter(s -> "up".equals(s.direction))
                .collect(Collectors.toList());
    }

    /** Returns all stat cards where the direction is "down" (negative trend). */
    public List<StatCard> negativeStats() {
        return stats.stream()
                .filter(s -> "down".equals(s.direction))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BAR CHART — Daily Query Volume
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all 7 daily bars. */
    public List<DailyBar> getDailyBars() {
        return dailyBars;
    }

    /** Returns the busiest day of the week by chart height. */
    public DailyBar peakDay() {
        return dailyBars.stream()
                .max(Comparator.comparingInt(b -> b.heightPercent))
                .orElseThrow();
    }

    /** Returns the quietest day of the week by chart height. */
    public DailyBar quietestDay() {
        return dailyBars.stream()
                .min(Comparator.comparingInt(b -> b.heightPercent))
                .orElseThrow();
    }

    /** Computes the average daily chart height across the week (0–100). */
    public double averageDailyLoad() {
        return dailyBars.stream()
                .mapToInt(b -> b.heightPercent)
                .average()
                .orElse(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DONUT CHART — Query Categories
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all donut slices. */
    public List<DonutSlice> getCategories() {
        return categories;
    }

    /** Returns the largest category by percentage. */
    public DonutSlice dominantCategory() {
        return categories.stream()
                .max(Comparator.comparingInt(s -> s.percent))
                .orElseThrow();
    }

    /** Returns slices whose share is above the given percentage threshold. */
    public List<DonutSlice> categoriesAbove(int thresholdPercent) {
        return categories.stream()
                .filter(s -> s.percent > thresholdPercent)
                .sorted(Comparator.comparingInt((DonutSlice s) -> s.percent).reversed())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODEL RUNS TABLE
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns all recent model runs. */
    public List<ModelRun> getModelRuns() {
        return runs;
    }

    /**
     * Returns runs filtered by status.
     *
     * @param status e.g. {@link RunStatus#PASSED}
     */
    public List<ModelRun> runsByStatus(RunStatus status) {
        return runs.stream()
                .filter(r -> r.status == status)
                .collect(Collectors.toList());
    }

    /** Returns the run with the highest accuracy. */
    public ModelRun bestRun() {
        return runs.stream()
                .max(Comparator.comparingDouble(r -> r.accuracyPercent))
                .orElseThrow();
    }

    /** Returns the run with the lowest accuracy. */
    public ModelRun worstRun() {
        return runs.stream()
                .min(Comparator.comparingDouble(r -> r.accuracyPercent))
                .orElseThrow();
    }

    /** Computes average accuracy across all runs. */
    public double averageAccuracy() {
        return runs.stream()
                .mapToDouble(r -> r.accuracyPercent)
                .average()
                .orElse(0);
    }

    /**
     * Returns runs for a specific model name (case-insensitive, prefix match).
     *
     * @param modelName e.g. "JARVIS-4"
     */
    public List<ModelRun> runsByModel(String modelName) {
        String lc = modelName.toLowerCase();
        return runs.stream()
                .filter(r -> r.model.toLowerCase().startsWith(lc))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACTIVITY FEED
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns the full activity feed, most recent first. */
    public List<ActivityItem> getActivity() {
        return activity;
    }

    /**
     * Returns activity items of a given type ("ok", "warn", "info", "event").
     *
     * @param type activity type to filter by
     */
    public List<ActivityItem> activityByType(String type) {
        return activity.stream()
                .filter(a -> a.type.equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /** Returns warning-level activity items. */
    public List<ActivityItem> warnings() {
        return activityByType("warn");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUMMARY REPORT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Prints a formatted summary of all dashboard sections to stdout —
     * useful for quick inspection in a terminal or notebook.
     */
    public void printSummary() {
        println("═══════════════════════════════════════════════════════════");
        println("  J.A.R.V.I.S  —  Dashboard Summary");
        println("═══════════════════════════════════════════════════════════");

        println("\n── Stat Cards ──────────────────────────────────────────────");
        stats.forEach(s -> println("  " + s));

        println("\n── Daily Query Volume (last 7 days) ────────────────────────");
        dailyBars.forEach(b -> {
            String bar = "█".repeat(b.heightPercent / 5);
            println(String.format("  %-4s %s %d%%", b.day, bar, b.heightPercent));
        });
        println(String.format("  Peak: %s  |  Avg load: %.0f%%", peakDay().day, averageDailyLoad()));

        println("\n── Query Categories ────────────────────────────────────────");
        categories.forEach(c -> println("  " + c));
        println("  Dominant: " + dominantCategory().category);

        println("\n── Recent Model Runs ───────────────────────────────────────");
        runs.forEach(r -> println("  " + r));
        println(String.format("  Best: #%d (%.1f%%)  |  Avg accuracy: %.2f%%",
                bestRun().runId, bestRun().accuracyPercent, averageAccuracy()));

        println("\n── System Activity ─────────────────────────────────────────");
        activity.forEach(a -> println("  " + a));

        println("\n═══════════════════════════════════════════════════════════");
    }

    private static void println(String s) {
        System.out.println(s);
    }
}
