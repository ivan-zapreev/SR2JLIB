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
import java.util.List;
import java.util.Map;

/**
 *
 * Represents a grammar entry interface
 *
 * @author Dr. Ivan S. Zapreev
 */
interface GrammarEntry {

    /**
     * Gets the expression type name - the grammar entry name
     *
     * @return the grammar entry name
     */
    public String get_expr_type();

    /**
     * Computes the max size for the grammar entry
     *
     * @return the max for the grammar entry
     */
    public int compute_max_size();

    /**
     * Computes the minimum node sizes and checks if the fixed point is reached.
     *
     * @return true if the fixed point is reached
     */
    public boolean compute_min_size();

    /**
     * Pre-process the entry's argument sizes
     */
    public void pre_process_arg_sizes();

    /**
     * Allows to get the min size for the grammar entry
     *
     * @return the min for the grammar entry
     */
    public int get_min_size();

    /**
     * Allows to get pre-computed minimum and maximum entry sizes
     *
     * @return the minimum and maximum entry node sizes
     */
    public int[] get_min_max_size();

    /**
     * Prepare the randomizers after the min and max node and entry sizes are
     * computed
     */
    public void prepare_randomizers();

    /**
     * Chooses the expression based on signature
     *
     * @param signature the signature for the expression
     * @return the chosen expression
     */
    public Expression choose_expr(final String signature);

    /**
     * Chooses the expression based on maximum size
     *
     * @param max_size the maximum size for the expression
     * @return the chosen expression
     */
    public Expression choose_expr(final int max_size);

    /**
     * Propagates the placement entries to improve mutation choices
     *
     * @param entries all the known entry types map
     * @return true if there we some placements propagated
     */
    public boolean propagate_placement_nodes(Map<String, GrammarEntry> entries);

    /**
     * Allows to get a list of all expressions of the given entry
     *
     * @return a list of all expressions
     */
    public List<Expression> get_expressions();

    /**
     * Allows to get a list of all expression weights of the given entry
     *
     * @return a list of all expression weights
     */
    public List<Double> get_weights();

    /**
     * Allows to check if the entry has multiple expressions with the given
     * signature
     *
     * @param signature the signature to be checked
     * @return true if there is more than one entry with the given signature
     */
    public boolean has_many(final String signature);
}
