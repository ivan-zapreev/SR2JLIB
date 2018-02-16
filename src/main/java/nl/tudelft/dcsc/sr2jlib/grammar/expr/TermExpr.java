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
package nl.tudelft.dcsc.sr2jlib.grammar.expr;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the terminal expression
 *
 * @author Dr. Ivan S. Zapreev
 * @param <ValueType> the value type Boolean, Double ...
 */
public abstract class TermExpr<ValueType> extends Expression {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(TermExpr.class.getName());

    //Stores the random constant value

    /**
     * The value storing object for the materialized terminal expression
     */
    protected ValueType m_value;
    private static final int[] MIN_MAX_VALUES = new int[]{1, 1};

    /**
     * The basic constructor
     *
     * @param expr_type the expression type
     */
    public TermExpr(final String expr_type) {
        super(expr_type);
    }

    /**
     * The copy constructor
     *
     * @param other the terminal expression to copy from
     */
    protected TermExpr(final TermExpr<ValueType> other) {
        super(other);
        this.m_value = other.m_value;
    }

    @Override
    public int compute_max_size() {
        return MIN_MAX_VALUES[1];
    }

    @Override
    public int compute_min_size() {
        return MIN_MAX_VALUES[0];
    }

    @Override
    public void pre_process_arg_sizes() {
        //Nothing to be done
    }

    @Override
    public void get_nodes(final List<Expression> nterm, final List<Expression> term) {
        term.add(this);
    }

    @Override
    public int get_size(final boolean is_re_compute) {
        return MIN_MAX_VALUES[0];
    }

    @Override
    public int get_size() {
        return get_size(true);
    }

    @Override
    public int get_min_size() {
        return MIN_MAX_VALUES[0];
    }

    @Override
    public int get_max_size() {
        return MIN_MAX_VALUES[1];
    }

    @Override
    public String get_signature() {
        LOGGER.log(Level.SEVERE, "Can not get signature of {0}", this.getClass().getName());
        throw new UnsupportedOperationException("Can not get signature of "
                + this.getClass().getName());
    }

    @Override
    public boolean replace_node(final Expression from, final Expression to)
            throws UnsupportedOperationException {
        LOGGER.log(Level.SEVERE, "Calling replace node {0}->{1} on a {2} node!",
                new Object[]{from, to, this.getClass().getName()});
        throw new UnsupportedOperationException("Calling replace node "
                + from + "->" + to + " on a " + this.getClass().getName() + " node!");
    }

    @Override
    public boolean emplace_funct(final Expression from, final Expression to)
            throws UnsupportedOperationException {
        LOGGER.log(Level.SEVERE, "Calling emplace node {0}->{1} on a {2} node!",
                new Object[]{from, to, this.getClass().getName()});
        throw new UnsupportedOperationException("Calling emplace node "
                + from + "->" + to + " on a " + this.getClass().getName() + " node!");
    }

    @Override
    public boolean is_terminal() {
        return true;
    }

    @Override
    public boolean is_placement() {
        return false;
    }
}
