package com.backbase.oss.blimp.lint;

import java.util.ArrayList;
import java.util.List;
import liquibase.servicelocator.ServiceLocator;
import lombok.Getter;

/**
 * Collects all implementations of a {@code LintRule} interface.
 */
public class LintRuleFinder {

    private static LintRuleFinder instance;

    /**
     * RuleFinder instance.
     */
    public static LintRuleFinder getInstance() {
        if (instance == null) {
            synchronized (LintRuleFinder.class) {
                if (instance == null) {
                    instance = new LintRuleFinder();
                }
            }
        }

        return instance;
    }

    private final ServiceLocator sl = ServiceLocator.getInstance();

    @Getter(lazy = true)
    private final Class<? extends LintRule>[] types = types();
    @Getter(lazy = true)
    private final List<LintRule> rules = rules();


    private LintRuleFinder() {
        this.sl.addPackageToScan("com.backbase");
    }

    public void setTypes(Class<? extends LintRule>[] rules) {
        this.types.set(rules);
        this.rules.set(null);
    }

    public void addPackageToScan(String packageName) {
        this.sl.addPackageToScan(packageName);

        this.types.set(null);
        this.rules.set(null);
    }

    private Class<? extends LintRule>[] types() {
        return this.sl.findClasses(LintRule.class);
    }

    private List<LintRule> rules() {
        final List<LintRule> rules = new ArrayList<>();

        for (final Class<? extends LintRule> cl : getTypes()) {
            final LintRule rule = (LintRule) this.sl.newInstance(cl);

            rules.add(rule);
        }

        return rules;
    }
}
