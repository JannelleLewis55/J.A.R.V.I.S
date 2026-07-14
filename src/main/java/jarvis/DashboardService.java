package jarvis;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jarvis.JarvisData.ActivityItem;
import jarvis.JarvisData.DailyBar;
import jarvis.JarvisData.DailyQuery;
import jarvis.JarvisData.DailyQueryData;
import jarvis.JarvisData.DonutSlice;
import jarvis.JarvisData.ModelRun;
import jarvis.JarvisData.QueryCategory;
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

    private final List<StatCard>             stats;
    private final List<DailyBar>             dailyBars;
    private final Map<String, DailyQueryData> dailyQueryData;
    private final List<DonutSlice>           categories;
    private final List<ModelRun>             runs;
    private final List<ActivityItem>         activity;

    /** Constructs a service backed by the canonical UI data. */
    public DashboardService() {
        this.stats          = JarvisData.dashboardStats();
        this.dailyBars      = JarvisData.dailyQueryBars();
        this.dailyQueryData = JarvisData.allDailyQueryData();
        this.categories     = JarvisData.queryCategories();
        this.runs           = JarvisData.recentModelRuns();
        this.activity       = JarvisData.systemActivity();
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
    // DAILY QUERY DRILL-DOWN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all 7 days of drill-down query data, keyed by day name
     * ("Mon"–"Sun"). Mirrors the {@code DAILY_QUERIES} constant in dashboard.html.
     */
    public Map<String, DailyQueryData> getDailyQueryData() {
        return dailyQueryData;
    }

    /**
     * Returns the drill-down data for a single day.
     *
     * @param day day abbreviation, e.g. "Mon", "Thu"
     * @return the data for that day, or empty if the day is unknown
     */
    public Optional<DailyQueryData> queriesForDay(String day) {
        return Optional.ofNullable(dailyQueryData.get(day));
    }

    /**
     * Returns the day with the highest absolute query count.
     * (Mirrors clicking the tallest bar in dashboard.html.)
     */
    public DailyQueryData peakQueryDay() {
        return dailyQueryData.values().stream()
                .max(Comparator.comparingInt(d -> d.count))
                .orElseThrow();
    }

    /**
     * Returns queries for a specific day filtered by category.
     *
     * @param day      day abbreviation
     * @param category query category to filter by
     */
    public List<DailyQuery> queriesByCategory(String day, QueryCategory category) {
        return queriesForDay(day)
                .map(d -> d.queries.stream()
                        .filter(q -> q.category == category)
                        .collect(Collectors.toList()))
                .orElseGet(List::of);
    }

    /**
     * Returns a breakdown of query counts per category for the given day.
     *
     * @param day day abbreviation
     * @return map of category → count
     */
    public Map<QueryCategory, Long> categorySplitForDay(String day) {
        return queriesForDay(day)
                .map(d -> d.queries.stream()
                        .collect(Collectors.groupingBy(q -> q.category,
                                () -> new EnumMap<>(QueryCategory.class),
                                Collectors.counting())))
                .orElseGet(() -> new EnumMap<>(QueryCategory.class));
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
            println(String.format("  %-4s %s %d%%  (%,d queries)", b.day, bar, b.heightPercent, b.queryCount));
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

    /**
     * Prints the full drill-down query list for every day.
     * Mirrors opening every bar in the dashboard bar chart.
     */
    public void printDailyQueryBreakdown() {
        println("═══════════════════════════════════════════════════════════");
        println("  J.A.R.V.I.S  —  Daily Query Breakdown (all 7 days)");
        println("═══════════════════════════════════════════════════════════");
        for (DailyBar bar : dailyBars) {
            DailyQueryData d = dailyQueryData.get(bar.day);
            println(String.format("\n── %s — %,d queries ─────────────────────────────────────", d.day, d.count));
            d.queries.forEach(q -> println("  " + q));
            Map<QueryCategory, Long> split = categorySplitForDay(bar.day);
            println(String.format("  Categories: CODE=%d  DATA=%d  WRITING=%d  OTHER=%d",
                    split.getOrDefault(QueryCategory.CODE,    0L),
                    split.getOrDefault(QueryCategory.DATA,    0L),
                    split.getOrDefault(QueryCategory.WRITING, 0L),
                    split.getOrDefault(QueryCategory.OTHER,   0L)));
        }
        println("\n═══════════════════════════════════════════════════════════");
    }

    private static void println(String s) {
        System.out.println(s);
    }
}
