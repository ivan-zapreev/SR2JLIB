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

/**
 * The configuration class for the grammar
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class GrammarConfig {

    //The grammar string
    /**
     *
     */
    public final String m_grammar;
    //The maximum allowed tree size of the expression

    /**
     *
     */
    public final int m_max_ts;
    //The change versus replace ratio within [0,1] for mutations

    /**
     *
     */
    public final double m_ch_vs_rep;
    //The number of state-space variables

    /**
     *
     */
    public final int m_num_vars;
    //The minimum nore growth fator [0,inf)

    /**
     *
     */
    public final double m_min_node_grow;
    //The maximum nore growth fator [min_node_grow,inf)

    /**
     *
     */
    public final double m_max_node_grow;
    //The flag indicating if the placement nodes are to be propagated

    /**
     *
     */
    public final boolean m_is_prop_pnodes;
    //The maximum grammar depth, is needed to bound the fixed point 
    //iterations when computing grammar's min/max expression sizes

    /**
     *
     */
    public final int m_max_gd;
    //The terminals versus non-terminals mutation ration within [0,1]

    /**
     *
     */
    public final double m_tm_vs_ntm;

    /**
     * The basic constructor
     *
     * @param grammar the grammar's textual description
     * @param max_ts the maximum allowed generated expression tree size
     * @param ch_vs_rep the change versus replace ratio from the range [0,1]
     * @param num_vars the number of variables to be used in this grammar
     * @param min_node_grow the minimum node grow coefficient, a positive double
     * @param max_node_grow the maximum node grow coefficient, a positive double
     * @param is_prop_pnodes true if placement nodes are to be propagated
     * @param max_gd the maximum grammar depth for fixed point iteration
     * @param tm_vs_ntm terminal versus non terminal mutation ratio from the
     * range [0,1]
     */
    public GrammarConfig(final String grammar,
            final int max_ts, final double ch_vs_rep,
            final int num_vars, final double min_node_grow,
            final double max_node_grow, final boolean is_prop_pnodes,
            final int max_gd, final double tm_vs_ntm) {
        m_grammar = grammar;
        m_max_ts = max_ts;
        m_ch_vs_rep = ch_vs_rep;
        m_num_vars = num_vars;
        m_min_node_grow = min_node_grow;
        m_max_node_grow = max_node_grow;
        m_is_prop_pnodes = is_prop_pnodes;
        m_max_gd = max_gd;
        m_tm_vs_ntm = tm_vs_ntm;
    }

}
