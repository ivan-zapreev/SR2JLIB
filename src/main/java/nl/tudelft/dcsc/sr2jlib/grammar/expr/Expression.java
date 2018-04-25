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

/**
 * Represents and expression class, both boolean and numeric
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public abstract class Expression {

    //Stores the expression type
    private String m_expr_type;

    /**
     * The basic constructor
     *
     * @param expr_type the expressino type
     */
    public Expression(final String expr_type) {
        this.m_expr_type = expr_type;
    }

    /**
     * The copy constructor
     *
     * @param other the expression to copy
     */
    protected Expression(final Expression other) {
        this.m_expr_type = other.m_expr_type;
    }

    /**
     * Allows to get the expression type
     *
     * @return the expression type
     */
    public String get_expr_type() {
        return m_expr_type;
    }

    /**
     * Gets all the expression nodes including this one
     *
     * @param nterm the container for non-terminal nodes
     * @param term the container for terminal nodes
     */
    public abstract void get_nodes(final List<Expression> nterm, final List<Expression> term);

    /**
     * Allows to replace a sub-tree node from with node to
     *
     * @param from the node to replace
     * @param to the node to replace with
     * @return true if the node is found, otherwise false
     * @throws UnsupportedOperationException in case the method is nod supported
     */
    public abstract boolean replace_node(final Expression from, final Expression to);

    /**
     * Allows to emplace a sub-tree node from with node to
     *
     * @param from the node to change
     * @param to the node to emplace with
     * @return true if the node is found, otherwise false
     * @throws UnsupportedOperationException in case the method is nod supported
     */
    public abstract boolean emplace_funct(final Expression from, final Expression to);

    /**
     * Gets the number of nodes in the tree, excluding the open ones.
     *
     * @param is_re_compute is true then the size will be re-computed if false
     * then the previously computed will be used if non then it will be computed
     *
     * @return the number of nodes in the tree, excluding the open ones
     */
    public abstract int get_size(final boolean is_re_compute);

    /**
     * Gets the number of nodes in the tree, excluding the open ones. Always
     * re-computes the size.
     *
     * @return the number of nodes in the tree, excluding the open ones
     */
    public abstract int get_size();

    /**
     * Computes the maximum size of the given node
     *
     * @return the maximum size value
     */
    public abstract int compute_max_size();

    /**
     * Computes the minimum size of the given node
     *
     * @return the minimum size value
     */
    public abstract int compute_min_size();

    /**
     * Pre-compute the argument size values
     */
    public abstract void pre_process_arg_sizes();

    /**
     * Gets the minimum tree size
     *
     * @return the minimum tree size for this node type
     */
    public abstract int get_min_size();

    /**
     * Gets the maximum tree size
     *
     * @return the maximum tree size for this node type
     */
    public abstract int get_max_size();

    /**
     * Allows to check if the expression can be of an infinite size
     *
     * @return true if the expression can be of infinite size
     */
    public abstract boolean is_max_size_inf();

    /**
     * Allows to get a signature of the expression
     *
     * @return the expression's signature
     * @throws UnsupportedOperationException in case the method is nod supported
     */
    public abstract String get_signature();

    /**
     * Creates a clone of the expression
     *
     * @return a copy of the expression
     */
    public abstract Expression duplicate();

    /**
     * Materialize tree starting in this node
     *
     * @param max_size the maximum size of the materialized tree
     */
    public abstract void materialize(int max_size);

    /**
     * Allows to serialize the expression to a mathematical string using the
     * java.lang.Math functions
     *
     * @return a string of the
     */
    public abstract String serialize();

    /**
     * Compares node functions
     *
     * @param expr the other node
     * @return true if the nodes are of the same functions, otherwise false
     */
    public abstract boolean is_equal_funct(Expression expr);

    /**
     * Allows to check if the node is a terminal one.
     *
     * The result is true if one of the next holds: (i) a terminal node; (ii) a
     * placement node whose only child is a terminal node
     *
     * NOTE: Is only valid after node materialization.
     *
     * @return true if the node is terminal
     */
    protected abstract boolean is_terminal();

    /**
     * Allows to indicate whether the expression is brackets safe. I.e. can be
     * used without brackets without causing any execution problems.
     *
     * @return true if the expression is brackets safe
     */
    protected abstract boolean is_bsafe_expr();

    /**
     * Allows to check if the node is a placement one. I.e. it puts one type
     * into another one via a functional.
     *
     * @return true if the node is a placement node
     */
    public abstract boolean is_placement();

    /**
     * Allows to check if the node is a basic placement one. I.e. it puts a
     * basic terminal type into another one via a functional.
     *
     * @return true if the node is a basic placement node
     */
    public abstract boolean is_b_placement();

    /**
     * Allows to change the expression type to the given one, is needed for
     * propagation
     *
     * @param expr_type the new expression type
     */
    public void change_expr_type(final String expr_type) {
        m_expr_type = expr_type;
    }

    /**
     * Allows to get a textual representation of the given expression
     *
     * @return a textual representation of the given expression
     */
    public abstract String to_text();

    /**
     * Allows to compute an optimized copy of the given expression.
     *
     * @return a new expression if the current node can be optimized or the same
     * object with optimized sub-nodes
     */
    public abstract Expression optimize();
}
