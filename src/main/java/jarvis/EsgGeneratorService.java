package jarvis;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EsgGeneratorService — drafts structured ESG disclosure narratives for any
 * combination of reporting standard (GRI, SASB, TCFD, CSRD, SDG) and sections.
 *
 * <p>Mirrors the "Generate it" feature in esg-checker.html exactly:
 * the same section IDs, headings, narrative text (parameterised by company name
 * and industry), and compliance reference callouts.
 *
 * <p>Usage:
 * <pre>{@code
 * EsgGeneratorService gen = new EsgGeneratorService();
 * EsgGeneratorService.GeneratedReport report = gen.generate(
 *     "gri",                           // standard key
 *     List.of("gri-2", "gri-300"),     // section IDs to include
 *     "Acme Corporation",              // company name
 *     "Technology"                     // industry label
 * );
 * System.out.println(report.toPlainText());
 * }</pre>
 */
public final class EsgGeneratorService {

    // ─────────────────────────────────────────────────────────────────────────
    // DATA MODEL
    // ─────────────────────────────────────────────────────────────────────────

    /** A single compliance reference callout attached to a subsection. */
    public static final class Callout {
        public final String label;
        public final String text;
        public Callout(String label, String text) {
            this.label = label;
            this.text  = text;
        }
        @Override public String toString() {
            return "[" + label + "] " + text;
        }
    }

    /** One subsection within a generated section (heading + body paragraphs + callout). */
    public static final class Subsection {
        public final String  heading;
        public final String  body;       // may contain \n\n paragraph breaks
        public final Callout callout;    // may be null
        public Subsection(String heading, String body, Callout callout) {
            this.heading = heading;
            this.body    = body;
            this.callout = callout;
        }
    }

    /** One top-level section of the generated ESG report. */
    public static final class Section {
        public final String          heading;
        public final List<Subsection> subsections;
        public Section(String heading, List<Subsection> subsections) {
            this.heading     = heading;
            this.subsections = Collections.unmodifiableList(subsections);
        }
    }

    /** The full generated ESG report — a list of drafted sections. */
    public static final class GeneratedReport {
        public final String        company;
        public final String        industry;
        public final String        standard;
        public final String        generatedDate;
        public final List<Section> sections;

        public GeneratedReport(String company, String industry, String standard,
                               String generatedDate, List<Section> sections) {
            this.company       = company;
            this.industry      = industry;
            this.standard      = standard;
            this.generatedDate = generatedDate;
            this.sections      = Collections.unmodifiableList(sections);
        }

        /**
         * Returns the report as plain text, matching the .txt download in the browser.
         */
        public String toPlainText() {
            StringBuilder sb = new StringBuilder();
            sb.append(company).append(" — ESG Sustainability Disclosure\n");
            sb.append("Standard: ").append(standard)
              .append("  |  Industry: ").append(industry)
              .append("  |  Generated: ").append(generatedDate).append("\n");
            sb.append("═".repeat(60)).append("\n\n");
            for (Section sec : sections) {
                String bar = "─".repeat(sec.heading.length());
                sb.append(sec.heading.toUpperCase()).append("\n").append(bar).append("\n\n");
                for (Subsection ss : sec.subsections) {
                    sb.append(ss.heading).append("\n\n");
                    sb.append(ss.body).append("\n\n");
                    if (ss.callout != null) {
                        sb.append(ss.callout).append("\n\n");
                    }
                }
            }
            return sb.toString().stripTrailing();
        }
    }

    /** Section definition (id, label, default-on flag) for one standard. */
    public static final class SectionDef {
        public final String  id;
        public final String  label;
        public final boolean defaultOn;
        public SectionDef(String id, String label, boolean defaultOn) {
            this.id        = id;
            this.label     = label;
            this.defaultOn = defaultOn;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION CATALOGUE
    // ─────────────────────────────────────────────────────────────────────────

    private static final Map<String, List<SectionDef>> SECTION_DEFS;
    static {
        Map<String, List<SectionDef>> m = new LinkedHashMap<>();
        m.put("gri", Arrays.asList(
            new SectionDef("gri-2",   "GRI 2 — General Disclosures",  true),
            new SectionDef("gri-200", "GRI 200 — Economic",           false),
            new SectionDef("gri-300", "GRI 300 — Environmental",      true),
            new SectionDef("gri-400", "GRI 400 — Social",             true)
        ));
        m.put("sasb", Arrays.asList(
            new SectionDef("sasb-env",    "Environment",                 true),
            new SectionDef("sasb-social", "Social Capital",              true),
            new SectionDef("sasb-human",  "Human Capital",               true),
            new SectionDef("sasb-bus",    "Business Model & Innovation", false),
            new SectionDef("sasb-gov",    "Leadership & Governance",     false)
        ));
        m.put("tcfd", Arrays.asList(
            new SectionDef("tcfd-gov",      "Governance",         true),
            new SectionDef("tcfd-strategy", "Strategy",           true),
            new SectionDef("tcfd-risk",     "Risk Management",    true),
            new SectionDef("tcfd-metrics",  "Metrics & Targets",  true)
        ));
        m.put("csrd", Arrays.asList(
            new SectionDef("esrs-2",  "ESRS 2 — General Disclosures",  true),
            new SectionDef("esrs-e1", "ESRS E1 — Climate Change",      true),
            new SectionDef("esrs-e2", "ESRS E2-5 — Other Environment", false),
            new SectionDef("esrs-s",  "ESRS S1-4 — Social",            true),
            new SectionDef("esrs-g",  "ESRS G1 — Governance",          false)
        ));
        m.put("sdg", Arrays.asList(
            new SectionDef("sdg-env",    "SDG 6,7,13,14,15 — Environment",  true),
            new SectionDef("sdg-social", "SDG 1,3,4,5,8,10 — People",       true),
            new SectionDef("sdg-econ",   "SDG 9,11,12,17 — Prosperity",     false),
            new SectionDef("sdg-gov",    "SDG 16 — Peace & Justice",        false)
        ));
        SECTION_DEFS = Collections.unmodifiableMap(m);
    }

    /** Returns all section definitions for a given standard key. */
    public List<SectionDef> sectionsFor(String standardKey) {
        return SECTION_DEFS.getOrDefault(standardKey, List.of());
    }

    /** Returns only the default-on sections for a standard. */
    public List<String> defaultSectionIds(String standardKey) {
        return sectionsFor(standardKey).stream()
                .filter(s -> s.defaultOn)
                .map(s -> s.id)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GENERATION ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates an ESG disclosure report for the given standard, sections,
     * company name, and industry — mirrors the "Generate Report" button in
     * esg-checker.html exactly.
     *
     * @param standardKey one of "gri", "sasb", "tcfd", "csrd", "sdg"
     * @param sectionIds  list of section IDs to include (in catalogue order)
     * @param company     organisation name (e.g. "Acme Corporation")
     * @param industry    industry label (e.g. "Technology")
     * @return a fully drafted {@link GeneratedReport}
     */
    public GeneratedReport generate(String standardKey, List<String> sectionIds,
                                    String company, String industry) {
        String org     = (company  == null || company.isBlank())  ? "the Organisation" : company.trim();
        String ind     = (industry == null || industry.isBlank()) ? "its sector"       : industry.trim();
        int    year    = Year.now().getValue();
        String date    = java.time.LocalDate.now()
                             .format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy"));
        String stdLabel = STD_LABELS.getOrDefault(standardKey, standardKey.toUpperCase());

        // Honour catalogue order
        List<SectionDef> defs   = sectionsFor(standardKey);
        List<String>      ordered = defs.stream()
                .map(d -> d.id)
                .filter(sectionIds::contains)
                .collect(Collectors.toList());

        List<Section> sections = new ArrayList<>();
        for (String id : ordered) {
            Section sec = draftSection(id, org, ind, year);
            if (sec != null) sections.add(sec);
        }
        return new GeneratedReport(org, ind, stdLabel, date, sections);
    }

    /** Convenience overload that uses the default section selection. */
    public GeneratedReport generateDefault(String standardKey, String company, String industry) {
        return generate(standardKey, defaultSectionIds(standardKey), company, industry);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION DRAFTERS
    // ─────────────────────────────────────────────────────────────────────────

    private static final Map<String, String> STD_LABELS = Map.of(
        "gri",  "GRI Standards",
        "sasb", "SASB Standards",
        "tcfd", "TCFD Framework",
        "csrd", "CSRD / ESRS",
        "sdg",  "UN SDG Mapping"
    );

    private Section draftSection(String id, String org, String ind, int year) {
        return switch (id) {
            case "gri-2"       -> griGeneral(org, ind, year);
            case "gri-200"     -> griEconomic(org, ind, year);
            case "gri-300"     -> griEnvironment(org, ind, year);
            case "gri-400"     -> griSocial(org, ind, year);
            case "sasb-env"    -> sasbEnvironment(org, ind, year);
            case "sasb-social" -> sasbSocial(org, ind, year);
            case "sasb-human"  -> sasbHuman(org, ind, year);
            case "sasb-bus"    -> sasbBusiness(org, ind, year);
            case "sasb-gov"    -> sasbGovernance(org, ind, year);
            case "tcfd-gov"    -> tcfdGovernance(org, ind, year);
            case "tcfd-strategy" -> tcfdStrategy(org, ind, year);
            case "tcfd-risk"   -> tcfdRisk(org, ind, year);
            case "tcfd-metrics"-> tcfdMetrics(org, ind, year);
            case "esrs-2"      -> esrs2(org, ind, year);
            case "esrs-e1"     -> esrsE1(org, ind, year);
            case "esrs-e2"     -> esrsE2(org, ind, year);
            case "esrs-s"      -> esrsS(org, ind, year);
            case "esrs-g"      -> esrsG(org, ind, year);
            case "sdg-env"     -> sdgEnvironment(org, ind, year);
            case "sdg-social"  -> sdgSocial(org, ind, year);
            case "sdg-econ"    -> sdgEconomy(org, ind, year);
            case "sdg-gov"     -> sdgGovernance(org, ind, year);
            default            -> null;
        };
    }

    // ── GRI ──────────────────────────────────────────────────────────────────

    private Section griGeneral(String org, String ind, int year) {
        return new Section("GRI 2 — General Disclosures", List.of(
            new Subsection("Organisational Profile",
                org + " is a " + ind + "-sector organisation committed to transparent governance and sustainable operations. " +
                "This report covers the financial year ending 31 December " + year + " and encompasses all wholly owned entities and operations under direct management control.\n\n" +
                "The organisation's principal activities, ownership structure, and operational footprint are described in the Annual Report (incorporated by reference). " +
                "This ESG disclosure has been prepared in accordance with the GRI Standards (Core option) and reviewed by senior leadership prior to publication.",
                new Callout("GRI 2-1", "Legal name, nature of ownership, headquarters location, and countries of operation are disclosed in Section 1 of the Annual Report.")),
            new Subsection("Strategy, Policies and Practices",
                org + "'s sustainability strategy is aligned with our material topics identified through a structured stakeholder engagement process conducted in " + (year - 1) + ". " +
                "Our Board of Directors retains ultimate oversight of ESG performance, supported by the Sustainability Committee which meets quarterly.\n\n" +
                "We have adopted organisation-wide policies covering environmental management, human rights, labour practices, anti-corruption, and data privacy. " +
                "Policy compliance is monitored via internal audit with results reported to the Audit & Risk Committee.",
                new Callout("GRI 2-9", "Governance structure, including board committees with ESG oversight, is detailed in the Corporate Governance section of the Annual Report."))
        ));
    }

    private Section griEconomic(String org, String ind, int year) {
        return new Section("GRI 200 — Economic Performance", List.of(
            new Subsection("Economic Value Generated and Distributed",
                "In " + year + ", " + org + " generated total revenues of [INSERT VALUE] and distributed economic value to employees, suppliers, capital providers, governments, and communities. " +
                "The retained economic value reinvested into the business was [INSERT VALUE], representing [X]% of revenues.\n\n" +
                "The organisation's procurement practices prioritise local suppliers where economically viable, contributing to regional economic development in markets where we operate.",
                new Callout("GRI 201-1", "Complete economic value table including generated and distributed values is provided in Appendix A — Economic Performance Data."))
        ));
    }

    private Section griEnvironment(String org, String ind, int year) {
        return new Section("GRI 300 — Environmental", List.of(
            new Subsection("Energy Consumption (GRI 302-1)",
                org + " consumed a total of [X] GJ of energy in " + year + ", comprising [X]% renewable and [X]% non-renewable sources. " +
                "Total energy intensity was [X] GJ per [unit of revenue/output], representing a [X]% change from the prior year baseline.\n\n" +
                "Key initiatives driving energy reduction in " + year + " included server virtualisation, LED lighting retrofits across all owned facilities, and HVAC optimisation programmes. " +
                "These measures delivered an estimated [X] GJ reduction versus business-as-usual.",
                new Callout("GRI 302-1", "Full energy data table by source and by facility is provided in the Environmental Data Appendix.")),
            new Subsection("GHG Emissions (GRI 305-1, 305-2, 305-3)",
                "Total Scope 1 direct emissions for " + year + " were [X] metric tonnes CO\u2082e. Scope 2 location-based emissions were [X] metric tonnes CO\u2082e; " +
                "market-based Scope 2 was [X] metric tonnes CO\u2082e, reflecting [X]% renewable electricity procurement.\n\n" +
                "Scope 3 emissions have been estimated across [X] material categories totalling [X] metric tonnes CO\u2082e, " +
                "with purchased goods and services and use of sold products representing the largest contributors. " +
                org + " is committed to setting a Science Based Target within 24 months.",
                new Callout("GRI 305-1/2/3", "GHG inventory prepared in accordance with the GHG Protocol Corporate Standard. Methodology and base-year data available on request.")),
            new Subsection("Water Withdrawal (GRI 303-3)",
                "Total water withdrawal in " + year + " was [X] megalitres, drawn primarily from [municipal supply / groundwater / surface water]. " +
                "Of this, [X]% was withdrawn from areas designated as water-stressed according to the WRI Aqueduct tool.\n\n" +
                "The organisation has set a target to reduce water intensity by [X]% by [year] against the " + (year - 1) + " baseline, " +
                "with progress tracked via our Environmental Management System.",
                new Callout("GRI 303-3", "Water withdrawal data disaggregated by source and by water-stressed region is included in the Environmental Data Appendix."))
        ));
    }

    private Section griSocial(String org, String ind, int year) {
        return new Section("GRI 400 — Social", List.of(
            new Subsection("Employee Health & Safety (GRI 403-9)",
                org + " recorded a Total Recordable Incident Rate (TRIR) of [X] per 200,000 hours worked in " + year + ", compared to [X] in the prior year. " +
                "There were [X] lost-time injuries and zero work-related fatalities. High-hazard operations are covered by ISO 45001-aligned Occupational Health & Safety management systems.\n\n" +
                "Wellbeing initiatives in " + year + " included the launch of a 24/7 Employee Assistance Programme, mandatory mental health awareness training for all line managers, " +
                "and a flexible working framework rolled out across all office locations.",
                new Callout("GRI 403-9", "TRIR, LTIFR, and fatality data disaggregated by employee and contractor are in the Social Data Appendix.")),
            new Subsection("Diversity, Equity & Inclusion (GRI 405-1, 405-2)",
                "Women represent [X]% of the total workforce and [X]% of senior leadership roles at " + org + " as of 31 December " + year + ". " +
                "The organisation targets [X]% female representation at senior levels by [year].\n\n" +
                "The gender pay gap (median) across the organisation is [X]%, with equal pay for equal work confirmed through our annual pay equity audit. " +
                "Action plans to close identified gaps are monitored by the People & Culture Committee on a semi-annual basis.",
                new Callout("GRI 405-2", "Full pay-gap data by employee category and gender is disclosed in the Social Data Appendix."))
        ));
    }

    // ── SASB ─────────────────────────────────────────────────────────────────

    private Section sasbEnvironment(String org, String ind, int year) {
        return new Section("SASB — Environment", List.of(
            new Subsection("Environmental Footprint of Hardware Infrastructure",
                org + " tracks and discloses environmental metrics aligned with SASB sector-specific standards for the " + ind + " industry. " +
                "Total energy consumed by operated facilities was [X] GJ in " + year + ", with a GHG emissions intensity of [X] metric tonnes CO\u2082e per [relevant operational unit].\n\n" +
                "The organisation has implemented an Environmental Management System certified to ISO 14001, covering all major operational sites. " +
                "Material environmental risks, including physical climate impacts and regulatory transition risks, are assessed annually and incorporated into enterprise risk management processes.",
                new Callout("SASB IF-RE-130a.1", "Industry-specific quantitative metrics are provided in the SASB Standards Index, Appendix B."))
        ));
    }

    private Section sasbSocial(String org, String ind, int year) {
        return new Section("SASB — Social Capital", List.of(
            new Subsection("Data Privacy & Customer Protection",
                org + " is subject to data protection regulations including [GDPR / applicable regional laws]. " +
                "In " + year + ", the organisation experienced [X] confirmed data breaches affecting [X] customers, representing [X]% of the active customer base. " +
                "Each incident was investigated, contained, and reported to the relevant supervisory authorities within statutory timeframes.\n\n" +
                "We maintain a comprehensive Data Protection programme overseen by a dedicated Data Protection Officer. " +
                "Privacy-by-design principles are embedded in all new product development and technology procurement processes.",
                new Callout("SASB TC-SI-230a.1", "Data breach details, regulatory correspondence, and remediation actions are described in the Risk Management section."))
        ));
    }

    private Section sasbHuman(String org, String ind, int year) {
        return new Section("SASB — Human Capital", List.of(
            new Subsection("Employee Engagement & Retention",
                org + " reported an employee voluntary turnover rate of [X]% in " + year + ", compared to [X]% in the prior year. " +
                "Involuntary turnover was [X]%. These figures are reported on a consolidated global basis; regional breakdowns are available in the Social Data Appendix.\n\n" +
                "Employee engagement is measured annually via an independent survey platform. The most recent survey achieved a [X]% response rate with an engagement score of [X] out of 100, " +
                "placing the organisation in the [X]th percentile for the " + ind + " industry.",
                new Callout("SASB SV-PS-330a.1", "Full turnover data disaggregated by region and employee category is included in Appendix B — SASB Standards Index."))
        ));
    }

    private Section sasbBusiness(String org, String ind, int year) {
        return new Section("SASB — Business Model & Innovation", List.of(
            new Subsection("Product Innovation & Lifecycle Management",
                org + " invested [X]% of revenues ([X] million) in research and development in " + year + ", " +
                "with [X]% of that investment directed towards sustainability-oriented product and service innovation. " +
                "New products launched in " + year + " with enhanced ESG attributes include [INSERT PRODUCT NAMES].\n\n" +
                "The organisation applies a product lifecycle assessment (LCA) methodology to key product lines to identify material environmental hotspots and inform eco-design decisions. " +
                "Results of LCAs conducted in " + year + " are summarised in the Product Sustainability Appendix.",
                new Callout("SASB multiple", "R&D investment data and product sustainability metrics are reported in the Business Model section of the Annual Report."))
        ));
    }

    private Section sasbGovernance(String org, String ind, int year) {
        return new Section("SASB — Leadership & Governance", List.of(
            new Subsection("Executive Sustainability-Linked Remuneration",
                "ESG performance metrics are incorporated into the variable remuneration framework for Executive Directors and Senior Leadership at " + org + ". " +
                "In " + year + ", [X]% of short-term incentive awards were subject to ESG performance conditions, " +
                "including [emission reduction targets / diversity targets / safety metrics].\n\n" +
                "The Remuneration Committee reviews ESG metric definitions and targets annually to ensure alignment with material sustainability priorities and stakeholder expectations.",
                new Callout("SASB multiple", "Full remuneration policy, including ESG metric weightings and outturn, is disclosed in the Directors' Remuneration Report."))
        ));
    }

    // ── TCFD ─────────────────────────────────────────────────────────────────

    private Section tcfdGovernance(String org, String ind, int year) {
        return new Section("TCFD — Governance", List.of(
            new Subsection("Board Oversight of Climate-Related Risks",
                "The Board of Directors of " + org + " retains ultimate oversight of climate-related risks and opportunities. " +
                "The Board receives updates on climate strategy and performance at least twice per year, " +
                "supplemented by escalation of material climate risks via the enterprise risk management process.\n\n" +
                "A dedicated Sustainability Committee of the Board — comprising [X] independent non-executive directors — " +
                "holds specific accountability for reviewing climate targets, transition plans, and scenario analysis outcomes.",
                new Callout("TCFD — Governance a)", "Board committee terms of reference, including climate oversight mandate, are published on the corporate governance section of the company website.")),
            new Subsection("Management's Role in Climate Assessment",
                "Day-to-day management of climate-related risks and opportunities is led by the Chief Sustainability Officer (CSO), " +
                "who reports directly to the CEO and presents to the Sustainability Committee quarterly. " +
                "The CSO is supported by a cross-functional Climate Working Group comprising representatives from Operations, Finance, Risk, Legal, and Strategy.\n\n" +
                "Climate risk is formally integrated into the organisation's enterprise risk register, " +
                "with risk owners assigned at the business-unit level and quarterly attestation to the Group Risk function.",
                new Callout("TCFD — Governance b)", "The CSO's remit, including climate-related accountabilities, is described in the Leadership section of the Annual Report."))
        ));
    }

    private Section tcfdStrategy(String org, String ind, int year) {
        return new Section("TCFD — Strategy", List.of(
            new Subsection("Climate Scenario Analysis",
                org + " conducted climate scenario analysis in " + year + " using two representative pathways: " +
                "a 1.5\u00b0C orderly transition scenario aligned with IEA NZE 2050, and a 3\u00b0C delayed-transition scenario reflecting current stated policies. " +
                "The analysis covered a short (to 2030), medium (to 2040), and long (to 2050) time horizon.\n\n" +
                "Key findings indicate that transition risks — primarily carbon pricing, policy change, and shifts in customer demand — represent the most material near-term exposure, " +
                "while physical risks become more significant post-2035 under the high-warming pathway.",
                new Callout("TCFD — Strategy b)", "Detailed scenario assumptions, methodology, and sensitivity analysis are presented in the Climate Risk Report.")),
            new Subsection("Climate Risks and Opportunities",
                "Transition risks identified as material include: rising carbon prices affecting operational costs, regulatory requirements for enhanced disclosure, " +
                "and potential stranding of carbon-intensive assets. The aggregate financial exposure under the 1.5\u00b0C scenario is estimated at [X] million over a 10-year horizon.\n\n" +
                "Material climate-related opportunities include growing demand for low-carbon products and services in the " + ind + " sector, " +
                "cost savings from energy efficiency improvements, and access to green finance instruments.",
                new Callout("TCFD — Strategy a)", "Financial quantification of risks and opportunities follows the methodology described in the TCFD Technical Supplement."))
        ));
    }

    private Section tcfdRisk(String org, String ind, int year) {
        return new Section("TCFD — Risk Management", List.of(
            new Subsection("Identification and Assessment of Climate Risks",
                org + " uses a structured climate risk identification process that combines top-down scenario analysis with bottom-up operational risk assessments. " +
                "Physical risk screening is performed for all owned and leased sites using geospatial climate hazard data, covering flood, heat stress, water scarcity, and windstorm perils.\n\n" +
                "Each identified risk is assessed on a likelihood-impact matrix, with risks above a defined materiality threshold entered into the Group Risk Register " +
                "and assigned a named risk owner. The methodology is reviewed annually by the Chief Risk Officer.",
                new Callout("TCFD — Risk Mgmt a)", "Physical risk screening results by site location are available to institutional investors on request."))
        ));
    }

    private Section tcfdMetrics(String org, String ind, int year) {
        return new Section("TCFD — Metrics & Targets", List.of(
            new Subsection("GHG Emissions and Reduction Targets",
                org + " discloses Scope 1, 2, and 3 GHG emissions in accordance with the GHG Protocol Corporate Standard. " +
                "In " + year + ", total Scope 1 + 2 (market-based) emissions were [X] metric tonnes CO\u2082e. " +
                "The organisation has set the following science-aligned reduction targets:\n\n" +
                "\u2022 Short-term (" + (year + 5) + "): Reduce absolute Scope 1 + 2 emissions by [X]% vs. " + (year - 1) + " baseline\n" +
                "\u2022 Long-term (2050): Achieve net-zero emissions across all scopes\n\n" +
                "Progress against targets is reported quarterly to the Sustainability Committee and annually to the Board.",
                new Callout("TCFD — Metrics c)", "Emissions data has been independently limited assured by [Assurance Provider] in accordance with ISAE 3000."))
        ));
    }

    // ── CSRD / ESRS ──────────────────────────────────────────────────────────

    private Section esrs2(String org, String ind, int year) {
        return new Section("ESRS 2 — General Disclosures", List.of(
            new Subsection("Double Materiality Assessment",
                org + " conducted a formal double materiality assessment in " + (year - 1) + " in accordance with ESRS 2 IRO-1 requirements. " +
                "The assessment followed a three-phase process: (1) universe identification of all potential impact, risk, and opportunity (IRO) topics; " +
                "(2) stakeholder consultation with [X] internal and [X] external stakeholder groups; " +
                "and (3) materiality scoring across impact materiality and financial materiality dimensions.\n\n" +
                "The assessment identified [X] material topics covering environmental, social, and governance matters. " +
                "Material topics are listed in the Materiality Matrix available in Annex I of this report.",
                new Callout("ESRS 2 — IRO-1", "The full materiality assessment methodology, stakeholder mapping, and scoring criteria are disclosed in Annex I.")),
            new Subsection("Governance and Sustainability Strategy",
                "The management of sustainability matters at " + org + " is governed by a two-tier structure: " +
                "the Board-level Sustainability Committee responsible for strategic oversight, and the Management Sustainability Council responsible for implementation and monitoring.\n\n" +
                "Our sustainability strategy is anchored to three pillars — [Pillar 1 / Pillar 2 / Pillar 3] — " +
                "each with quantified targets, KPIs, and assigned management ownership.",
                new Callout("ESRS 2 — GOV-1", "Governance structure chart and committee mandates are included in the Corporate Governance section of the Annual Report."))
        ));
    }

    private Section esrsE1(String org, String ind, int year) {
        return new Section("ESRS E1 — Climate Change", List.of(
            new Subsection("Climate Transition Plan (ESRS E1-1)",
                org + " has developed a Climate Transition Plan setting out our pathway to net zero by 2050. " +
                "The plan identifies key decarbonisation levers across Scope 1, 2, and material Scope 3 categories, " +
                "supported by capital expenditure commitments and accountability mechanisms.\n\n" +
                "Key near-term actions include: transitioning the company vehicle fleet to [X]% zero-emission vehicles by [year], " +
                "procuring [X]% of electricity from renewable sources by [year], and engaging our top [X] suppliers on setting their own science-based targets.",
                new Callout("ESRS E1-1", "The full Climate Transition Plan, including locked-in emissions, capex commitments, and decarbonisation milestones, is published on the company website.")),
            new Subsection("GHG Targets and Performance (ESRS E1-4)",
                org + "'s GHG reduction targets are aligned with a 1.5\u00b0C pathway and validated by the Science Based Targets initiative (SBTi).\n\n" +
                "\u2022 Near-term (" + (year + 5) + "): Reduce absolute Scope 1 + 2 GHG emissions by [X]% from a " + (year - 2) + " base year\n" +
                "\u2022 Near-term (" + (year + 5) + "): Reduce absolute Scope 3 GHG emissions by [X]% from a " + (year - 2) + " base year\n" +
                "\u2022 Long-term (2050): Achieve net-zero GHG emissions across the value chain\n\n" +
                "In " + year + ", Scope 1 emissions were [X] tCO\u2082e and Scope 2 (market-based) emissions were [X] tCO\u2082e.",
                new Callout("ESRS E1-4", "SBTi commitment letter and target validation are available on the SBTi website. Full emissions dataset is in the Environmental Data Appendix."))
        ));
    }

    private Section esrsE2(String org, String ind, int year) {
        return new Section("ESRS E2-5 — Pollution, Water, Biodiversity & Circular Economy", List.of(
            new Subsection("Pollution Prevention (ESRS E2)",
                org + " manages pollution risks through site-level Environmental Management Plans, which are reviewed annually. " +
                "Significant pollution incidents in " + year + ": [X]. Regulatory exceedances: [X]. " +
                "All incidents were reported to the relevant environmental authorities and remediation actions are tracked to closure.\n\n" +
                "Air emissions of regulated substances, including NO\u2093, SO\u2093, and persistent organic pollutants, " +
                "are monitored at all operated facilities with continuous emissions monitoring equipment where required by permit.",
                new Callout("ESRS E2", "Pollution incident register and regulatory correspondence are maintained by the HSE function and available to regulators on request."))
        ));
    }

    private Section esrsS(String org, String ind, int year) {
        return new Section("ESRS S1-4 — Social", List.of(
            new Subsection("Own Workforce (ESRS S1)",
                "As of 31 December " + year + ", " + org + " employed [X] people globally, " +
                "comprising [X]% permanent employees and [X]% non-guaranteed hours or temporary workers.\n\n" +
                "The gender pay gap (mean) across the organisation was [X]%, and the ratio of the highest paid individual's total compensation " +
                "to the median employee's total compensation was [X]:1.",
                new Callout("ESRS S1", "Workforce metrics including headcount, turnover, pay gap, and safety data are in the Social Data Appendix.")),
            new Subsection("Value Chain Workers (ESRS S2)",
                org + " has conducted a value chain human rights due diligence assessment covering [X]% of tier-1 suppliers by spend. " +
                "The assessment identified [X] suppliers with potential exposure to forced labour risks, [X] with child labour risks, " +
                "and [X] with health and safety concerns. Remediation plans are in place for [X]% of identified cases.\n\n" +
                "The organisation's Supplier Code of Conduct — aligned with the ILO Core Conventions and UN Guiding Principles on Business and Human Rights — " +
                "is a mandatory contractual requirement for all new and renewed supplier agreements from " + year + ".",
                new Callout("ESRS S2", "Human rights due diligence methodology and supplier assessment results are described in the Supply Chain section."))
        ));
    }

    private Section esrsG(String org, String ind, int year) {
        return new Section("ESRS G1 — Governance, Risk & Internal Control", List.of(
            new Subsection("Business Conduct Policies (ESRS G1-1)",
                org + " maintains a comprehensive suite of business conduct policies including the Code of Business Conduct, " +
                "Anti-Bribery and Anti-Corruption Policy, Whistleblowing Policy, and Conflicts of Interest Policy. " +
                "All policies are reviewed annually by the Board and require annual attestation by all employees above [grade level].\n\n" +
                "In " + year + ", [X] whistleblowing reports were received via the confidential reporting hotline, " +
                "of which [X] were investigated. [X] led to disciplinary action and [X] are ongoing.",
                new Callout("ESRS G1-1", "Policy documents are publicly available on the corporate website. Whistleblowing report statistics are disclosed in the Governance section."))
        ));
    }

    // ── SDG ──────────────────────────────────────────────────────────────────

    private Section sdgEnvironment(String org, String ind, int year) {
        return new Section("SDG Alignment — Environment (SDG 6, 7, 13, 14, 15)", List.of(
            new Subsection("SDG 13 — Climate Action",
                org + "'s contribution to SDG 13 is anchored by our net-zero commitment and Climate Transition Plan. " +
                "In " + year + ", we reduced absolute Scope 1 + 2 emissions by [X]% year-on-year, " +
                "contributing to our " + (year + 10) + " science-based target.\n\n" +
                "We also contribute to SDG 13 through capacity-building initiatives: [X] employees completed climate literacy training in " + year + ".",
                new Callout("SDG 13.2.1", "Alignment of our climate targets with national and international climate commitments is described in the Climate Strategy section.")),
            new Subsection("SDG 7 — Affordable and Clean Energy",
                "In " + year + ", " + org + " sourced [X]% of its electricity from renewable sources, up from [X]% in the prior year. " +
                "Energy intensity improved by [X]% versus the baseline year, reflecting investments in building efficiency and process optimisation.",
                new Callout("SDG 7.2.1", "Renewable energy share and energy intensity data are in the Environmental Data Appendix."))
        ));
    }

    private Section sdgSocial(String org, String ind, int year) {
        return new Section("SDG Alignment — People (SDG 1, 3, 4, 5, 8, 10)", List.of(
            new Subsection("SDG 5 — Gender Equality",
                org + " is committed to advancing gender equality across our workforce and value chain. " +
                "In " + year + ", women represented [X]% of our total workforce and [X]% of our Board of Directors.\n\n" +
                "Our targeted leadership development programme for women supported [X] participants in " + year + ". " +
                "We have set a target of [X]% female representation at Director level and above by [target year].",
                new Callout("SDG 5.5.2", "Proportions of women in leadership positions are in the Social Data Appendix.")),
            new Subsection("SDG 8 — Decent Work and Economic Growth",
                org + " paid a living wage to [X]% of directly employed workers in " + year + ". " +
                "No operations in " + year + " were assessed as carrying a significant risk of child labour or forced labour.\n\n" +
                "The organisation contributed to economic growth through [X] new jobs created, [X] million in local supplier spend, " +
                "and [X] apprenticeship starts in " + year + ".",
                new Callout("SDG 8.5.1", "Fair wages data and supply chain decent-work screening results are in the Social Data Appendix."))
        ));
    }

    private Section sdgEconomy(String org, String ind, int year) {
        return new Section("SDG Alignment — Prosperity (SDG 9, 11, 12, 17)", List.of(
            new Subsection("SDG 12 — Responsible Consumption and Production",
                org + " generated [X] metric tonnes of total waste in " + year + ", of which [X]% was diverted from landfill through reuse, recycling, or recovery.\n\n" +
                "Our circular economy strategy sets a target of [X]% waste diverted from landfill by [target year]. " +
                "In " + year + ", we introduced [X] new circular product initiatives.",
                new Callout("SDG 12.5.1", "Waste generation and diversion data are in the Environmental Data Appendix."))
        ));
    }

    private Section sdgGovernance(String org, String ind, int year) {
        return new Section("SDG Alignment — Peace & Justice (SDG 16)", List.of(
            new Subsection("SDG 16 — Peace, Justice and Strong Institutions",
                org + " supports SDG 16 through transparent governance, zero-tolerance anti-corruption policies, " +
                "and support for the rule of law in all jurisdictions where we operate.\n\n" +
                "No regulatory fines or sanctions related to corruption, bribery, or anti-competitive behaviour were incurred in " + year + ". " +
                "The organisation's political donations policy prohibits direct contributions to political parties or candidates.",
                new Callout("SDG 16.5.2", "Anti-corruption KPIs and training completion rates are in the Governance section."))
        ));
    }
}
