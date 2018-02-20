/* 
 * Copyright (C) 2018 Dr. Ivan S. Zapreev <ivan.zapreev@gmail.com>
 *
 *  Visit my Linked-in profile:
 *     https://nl.linkedin.com/in/zapreevis
 *  Visit my GitHub:
 *     https://github.com/ivan-zapreev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tudelft.dcsc.sr2jlib.grammar;

import nl.tudelft.dcsc.sr2jlib.grammar.expr.Expression;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.FunctExpr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the grammar entry class storing all the required info of the given
 * entry
 *
 * @author Dr. Ivan S. Zapreev
 */
class FunctGrammarEntry implements GrammarEntry {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(FunctGrammarEntry.class.getName());

    //The entry name value delimiter
    private static final String IS_DELIM_REG = ":=";
    //The entry values delimiter
    private static final String VALUES_DELIM_REG = ";";
    //The value and vewight delimiter
    private static final String WEIGHT_DELIM_REG = "@";

    //Stores the recursion index
    private int m_rec_idx;
    private boolean m_is_max_comp;
    //Stores the expression type
    private final String m_entry_type;
    //Stores the list of functions
    private final List<Expression> m_funct;
    //Stores the list of function weights
    private final List<Double> m_weight;
    //Store the min max size values;
    private final int[] m_min_max_size;
    //Stores the mapping from the minimum size to randomizers
    private final Map<Integer, Randomizer> m_size2rnds;
    //Stores the mapping from the signature to the randomisers
    private final Map<String, Randomizer> m_sig2rnds;
    //Stores the minimum minimum size value
    private int m_min_min_size;
    //Stores the maximum finite size bound value
    private int m_max_fin_size;

    /**
     * Basic constructor
     *
     * @param provider the grammar provider
     * @param entry the grammar entry to be parsed
     */
    FunctGrammarEntry(final GrammarProvider provider, final String entry) {
        String[] name_val = entry.split(IS_DELIM_REG);
        if (name_val.length != 2) {
            throw new IllegalArgumentException("Improper entry format (name:=value) for: " + entry);
        }
        LOGGER.log(Level.FINE, "Considering grammar entry: {0}", entry);
        this.m_rec_idx = 0;
        this.m_min_min_size = Integer.MAX_VALUE;
        this.m_max_fin_size = Integer.MIN_VALUE;
        this.m_is_max_comp = false;
        this.m_entry_type = name_val[0].trim();
        this.m_min_max_size = new int[]{0, Integer.MIN_VALUE};
        this.m_funct = new ArrayList();
        this.m_weight = new ArrayList();
        this.m_size2rnds = new HashMap();
        this.m_sig2rnds = new HashMap();

        //Parse the entry values
        parse_entry_values(provider, name_val[1].trim());
    }

    /**
     * Allows to parse the entry values
     *
     * @param provider the grammar provider
     * @param values_str the entry values
     */
    private void parse_entry_values(final GrammarProvider provider, final String values_str) {
        String[] values = values_str.split(VALUES_DELIM_REG);
        if ((values.length == 1) && values[0].trim().isEmpty()) {
            throw new IllegalArgumentException("Empty entry '"
                    + m_entry_type + "' is not allowed!");
        } else {
            for (String value : values) {
                value = value.trim();
                if (!value.isEmpty()) {
                    //Get the weight
                    final String[] elems = value.split(WEIGHT_DELIM_REG);
                    final String funct = elems[0].trim();
                    final double weight = (elems.length > 1)
                            ? Double.parseDouble(elems[1].trim()) : 1.0;
                    final FunctExpr expr = new FunctExpr(provider, m_entry_type, funct);
                    //Store the weight in the list
                    m_funct.add(expr);
                    m_weight.add(weight);
                }
            }
        }
    }

    /**
     * The entry name getter
     *
     * @return the entry name
     */
    @Override
    public String get_expr_type() {
        return m_entry_type;
    }

    @Override
    public synchronized int compute_max_size() {
        LOGGER.log(Level.FINE, "Computing maximum size for {0} ({1}/{2}/{3}):",
                new Object[]{m_entry_type, m_is_max_comp, m_rec_idx, m_funct.size()});
        //Check if the min max size is already computed
        if (!m_is_max_comp) {
            //If we are in recursion then the maximum valiue is "infinity"
            if (m_rec_idx > 0) {
                m_min_max_size[1] = Integer.MAX_VALUE;
            }
            //Keep computing the max value from where left it
            while (m_rec_idx < m_funct.size()) {
                //Move to the next element in recursion
                ++m_rec_idx;
                //Call the min max size computations for the given element
                int max_size = m_funct.get(m_rec_idx - 1).compute_max_size();
                //Once the results are mach update
                m_min_max_size[1] = Math.max(m_min_max_size[1], max_size);
            }
            m_is_max_comp = true;
        }
        LOGGER.log(Level.FINE, "The maximum size for {0} is {1}",
                new Object[]{m_entry_type, m_min_max_size[1]});

        //Return the array value
        return m_min_max_size[1];
    }

    @Override
    public int[] get_min_max_size() {
        return m_min_max_size;
    }

    @Override
    public boolean compute_min_size() {
        int new_min = Integer.MAX_VALUE;
        for (Expression expr : m_funct) {
            new_min = Math.min(new_min, expr.compute_min_size());
        }
        //Check for the fixedpoint
        final boolean is_fixed_point = (m_min_max_size[0] == new_min);
        if (is_fixed_point) {
            LOGGER.log(Level.FINE, "The entry: {0} converged to minimum size: {1}",
                    new Object[]{m_entry_type, new_min,});
        }
        //Store the new minimum value
        m_min_max_size[0] = new_min;
        return is_fixed_point;
    }

    @Override
    public void pre_process_arg_sizes() {
        for (Expression expr : m_funct) {
            expr.pre_process_arg_sizes();
        }
    }

    @Override
    public int get_min_size() {
        return m_min_max_size[0];
    }

    /**
     * Allows to add a new expression with a weight into a randomizer
     *
     * @param rands the map for min size to randomizers
     * @param exp the new expression
     * @param weight the expression's weight
     */
    private static void register_exp_rand(final Map<Integer, Randomizer> rands,
            final Expression exp, final double weight) {
        Randomizer rnd = rands.get(exp.get_min_size());
        if (rnd == null) {
            rnd = new Randomizer();
            rands.put(exp.get_min_size(), rnd);
        }
        rnd.register(exp, weight);
    }

    /**
     * Allows to add a new expression with a weight into a randomizer
     *
     * @param rands the map for signature to randomizers
     * @param exp the new expression
     * @param weight the expression's weight
     */
    private static void register_exp_srand(final Map<String, Randomizer> rands,
            final Expression exp, final double weight) {
        //Ony functional expressions have signatures
        if (exp instanceof FunctExpr) {
            final FunctExpr fexp = (FunctExpr) exp;
            Randomizer rnd = rands.get(fexp.get_signature());
            if (rnd == null) {
                rnd = new Randomizer();
                rands.put(fexp.get_signature(), rnd);
            }
            rnd.register(exp, weight);
        }
    }

    @Override
    public void prepare_randomizers() {
        boolean is_max_inf = false;
        for (int idx = 0; idx < m_funct.size(); ++idx) {
            final Expression expr = m_funct.get(idx);
            final double weight = m_weight.get(idx);
            register_exp_rand(m_size2rnds, expr, weight);
            register_exp_srand(m_sig2rnds, expr, weight);
            m_min_min_size = Math.min(m_min_min_size, expr.get_min_size());
            m_max_fin_size = Math.max(m_max_fin_size, expr.get_min_size());
            //For the maximum size we want to know if there is 
            //infinity and the maximum finite size to consider
            final int max_size = expr.get_max_size();
            if (max_size == Integer.MAX_VALUE) {
                is_max_inf = true;
            } else {
                m_max_fin_size = Math.max(m_max_fin_size, max_size);
            }
        }
        //If the maximum is infinity then add an extra randomizer for those
        if (is_max_inf) {
            m_max_fin_size += 1;
        }
        LOGGER.log(Level.FINE, "Grammar entry min min/max fin. sizes are: {0}/{1}",
                new Object[]{m_min_min_size, m_max_fin_size});
        //Propagate expressions based on their minimum and maximum sizes!

        //We shall start from minimum minimum size and go to maximum finite size
        //and propagate all expressions which have the size index in the min/max range
        //The very last randomizer will be for the remaining sizes between the found
        //finite maximum and infinity, if any
        for (int idx = m_min_min_size; idx < m_max_fin_size; ++idx) {
            LOGGER.log(Level.FINE, "Propagating randomizer {0}", idx);
            final Randomizer curr = m_size2rnds.get(idx);
            //The current randomizer may be null if the previous one
            //did not propagate anything because of fixed sizes
            if (curr != null) {
                final int next_idx = idx + 1;
                Randomizer next = m_size2rnds.get(next_idx);
                if (next == null) {
                    next = new Randomizer();
                }
                for (RandomInterval ivl : curr.get_ivls()) {
                    Expression exp = ivl.get_exp();
                    LOGGER.log(Level.FINE, "{0} has max size {1}, copying to level {2}",
                            new Object[]{exp, exp.get_max_size(), next_idx});
                    //If we would propagate all the elements then we would not
                    //be able to fulfil the requested size and would often get 
                    //the tree size smaller than requested
                    if (exp.get_max_size() >= next_idx) {
                        next.register(exp, ivl.get_weight());
                    }
                }
                //Put the randomizer into the map inly if it is not empty
                if (!next.is_empty()) {
                    m_size2rnds.put(next_idx, next);
                }
            }
        }
        //Make distributions now when the expressons have been filled in
        m_size2rnds.forEach((key, value) -> {
            LOGGER.log(Level.FINE, "Preparing distributions for level {0}", key);
            value.prepare_distrib();
        });
        m_sig2rnds.forEach((key, value) -> {
            LOGGER.log(Level.FINE, "Preparing distributions for signature {0}", key);
            value.prepare_distrib();
        });
    }

    @Override
    public Expression choose_expr(final String signature) {
        return m_sig2rnds.get(signature).choose_expression();
    }

    @Override
    public Expression choose_expr(final int max_size) {
        int rand_size = Math.max(m_min_min_size, Math.min(m_max_fin_size, max_size));
        LOGGER.log(Level.FINE, "Size change from {0} to {1}, to stay within [{2}, {3}]",
                new Object[]{max_size, rand_size, m_min_min_size, m_max_fin_size});
        Randomizer rnd = m_size2rnds.get(rand_size);
        while (rnd == null) {
            rand_size--;
            rnd = m_size2rnds.get(rand_size);
        }
        LOGGER.log(Level.FINE, "The actual available size is {0}", rand_size);
        return rnd.choose_expression();
    }

    @Override
    public boolean propagate_placement_nodes(Map<String, GrammarEntry> entries) {
        final List<Integer> plac_idx = new ArrayList();
        final List<Expression> new_expr = new ArrayList();
        final List<Double> new_weights = new ArrayList();
        LOGGER.log(Level.FINE, "Propagating placements for {0} expressions listsize:"
                + " {1}, weight list size: {2}", new Object[]{
                    m_entry_type, m_funct.size(), m_weight.size()});

        //Iterate over the functions searching for placements
        for (int idx = 0; idx < m_funct.size(); ++idx) {
            final FunctExpr func = (FunctExpr) m_funct.get(idx);
            final Double weight = m_weight.get(idx);
            LOGGER.log(Level.FINE, "Got funciton: {0} of weight: {1}",
                    new Object[]{func, weight});
            if (func.is_placement()) {
                final String plc_type = func.get_signature();
                final GrammarEntry entry = entries.get(plc_type);
                LOGGER.log(Level.FINE, "The placement signature type is: {0}",
                        new Object[]{plc_type});
                //Do not propagate for the same type
                if (!plc_type.equals(m_entry_type)) {
                    final List<Expression> exprs = entry.get_expressions();
                    final List<Double> weights = entry.get_weights();
                    for (int o_idx = 0; o_idx < exprs.size(); ++o_idx) {
                        Expression exp = exprs.get(o_idx).duplicate();
                        exp.change_expr_type(m_entry_type);
                        new_expr.add(exp);
                        new_weights.add(weight * weights.get(o_idx));
                    }
                    LOGGER.log(Level.FINE, "Remembering the placement index: {0}",
                            new Object[]{idx});
                    //Remember the placement index
                    plac_idx.add(idx);
                }
            }
        }

        //If there we some placements found then remove them from
        //the list and add their expressions instead
        if (!plac_idx.isEmpty()) {
            //Remove the placement expressions and weights,
            //go backwards to keep indexes preserved
            for (int idx = plac_idx.size() - 1; idx >= 0; idx--) {
                final int index = plac_idx.get(idx);
                Expression expr = m_funct.remove(index);
                Double weight = m_weight.remove(index);
                LOGGER.log(Level.FINE, "Removing placement: {0} with weight: {1}",
                        new Object[]{expr, weight});
            }
            //Add the new expressions
            m_funct.addAll(new_expr);
            //Add the new weights
            m_weight.addAll(new_weights);
        }

        LOGGER.log(Level.FINE, "Propagated {0} placements for {1}",
                new Object[]{plac_idx.size(), m_entry_type});

        return !plac_idx.isEmpty();
    }

    @Override
    public List<Expression> get_expressions() {
        return m_funct;
    }

    @Override
    public List<Double> get_weights() {
        return m_weight;
    }
}
