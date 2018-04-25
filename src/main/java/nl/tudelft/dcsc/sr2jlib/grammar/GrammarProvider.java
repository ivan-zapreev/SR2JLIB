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

/**
 * Stores the methods required for the
 *
 * @see nl.tudelft.dcsc.sr2jlib.grammar.expr.FunctExpr
 *
 * class to provide Grammar related methods
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public interface GrammarProvider {

    /**
     * Allows to compute the minimum size for the argument type node
     *
     * @param arg_type the argument type node
     * @return the maximum size of the argument type node
     */
    public int compute_max_size(final String arg_type);

    /**
     * Allows to get the minimum size for the argument type node
     *
     * @param arg_type the argument type node
     * @return the minimum size of the argument type node
     */
    public int get_min_size(final String arg_type);

    /**
     * Allows to get pre-computed minimum and maximum sizes for the grammar
     * entry
     *
     * @param arg_type the argument type node
     * @return the minimum and maximum size of the argument type node
     */
    public int[] get_min_max_size(final String arg_type);

    /**
     * Generates an expression of the given maximum size and type
     *
     * @param exp_type the expression type
     * @param max_size the maximum expression size
     * @return the generated expression
     */
    public Expression choose_expr(final String exp_type, final int max_size);
}
