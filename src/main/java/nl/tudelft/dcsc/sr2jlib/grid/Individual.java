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
package nl.tudelft.dcsc.sr2jlib.grid;

import java.util.ArrayList;
import nl.tudelft.dcsc.sr2jlib.fitness.Fitness;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import nl.tudelft.dcsc.sr2jlib.fitness.FitnessManager;
import nl.tudelft.dcsc.sr2jlib.grammar.Grammar;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.Expression;

/**
 * Represents the individual
 *
 * @author Dr. Ivan S. Zapreev
 */
public class Individual {

    //The undefined position value
    private static final int UNDEF_POSITION = -1;
    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(Individual.class.getName());
    //Stores the minimum and maximum children conunts
    private static int MIN_CHILDREN_CNT = 0;
    private static int MAX_CHILDREN_CNT = 0;

    /**
     * Set the minimum and maximum child counts for the case individuals die
     * after a limited amount of children.
     *
     * @param min_chld_cnt the minimum children count
     * @param max_chld_cnt the maximum children count
     */
    public static void set_min_max_child_cnt(final int min_chld_cnt,
            final int max_chld_cnt) {
        MIN_CHILDREN_CNT = min_chld_cnt;
        MAX_CHILDREN_CNT = max_chld_cnt;
    }

    //Stores the individual's fitness
    private Fitness m_fitness;
    //Stores the total size of expressions
    private int m_size;
    //Stores the individual's expression
    private final Expression[] m_exps;
    //Stores the input dof id for which this individual is prodiced
    private final int m_mgr_id;
    //Stores the x position
    private int m_pos_x;
    //Stores the y position
    private int m_pos_y;
    //Stores the reproduction count, the number
    //of times to reproduce before death;
    private int m_max_child_cnt;

    /**
     * Constructor for an individual
     *
     * @param exprs an array of dof expressions
     * @param pos_x its x coordinate
     * @param pos_y its y coordinate
     * @param mgr_id the id of the population manager
     */
    private Individual(final Expression[] exps, final int pos_x,
            final int pos_y, final int mgr_id) {
        this.m_fitness = null;
        this.m_exps = exps;
        this.m_size = 0;
        this.m_pos_x = pos_x;
        this.m_pos_y = pos_y;
        this.m_mgr_id = mgr_id;
        this.m_max_child_cnt = -1;
        //Computethe individual's fitness
        compute_fitness();
    }

    /**
     * Constructor for an individual
     *
     * @param exprs an array of dof expressions
     * @param input_dof_id the corresponding dof id
     */
    private Individual(final Expression[] exprs, final int input_dof_id) {
        this(exprs, UNDEF_POSITION, UNDEF_POSITION, input_dof_id);
    }

    /**
     * Constructor for an individual
     *
     * @param pos_x its x coordinate
     * @param pos_y its y coordinate
     * @param mgr_id the id of the population manager
     * @param num_dofs the number of dimensions for the vector function
     */
    public Individual(final int pos_x, final int pos_y,
            final int mgr_id, final int num_dofs) {
        this(generate_exprs(mgr_id, num_dofs), pos_x, pos_y, mgr_id);
    }

    /**
     * Allows to generate a random vector function
     *
     * @param mgr_id the id of the process manager
     * @param num_dofs the number of dimensions
     * @return the array storing the vector function components
     */
    private static Expression[] generate_exprs(final int mgr_id, final int num_dofs) {
        Expression[] exprs = new Expression[num_dofs];
        for (int idx = 0; idx < num_dofs; ++idx) {
            exprs[idx] = Grammar.inst(mgr_id, idx).generate_numeric();
        }
        return exprs;
    }

    /**
     * Returns the x position
     *
     * @return the x position
     */
    public int get_pos_x() {
        return m_pos_x;
    }

    /**
     * Returns the y position
     *
     * @return the y position
     */
    public int get_pos_y() {
        return m_pos_y;
    }

    /**
     * Sets the x position
     *
     * @param pos the x position
     */
    public void set_pos_x(final int pos) {
        m_pos_x = pos;
    }

    /**
     * Sets the y position
     *
     * @param pos the y position
     */
    public void set_pos_y(final int pos) {
        m_pos_y = pos;
    }

    /**
     * Allows to check if the cell had reached its reproduction limit and has to
     * die.
     *
     * @return true if the limit has been reached
     */
    public boolean is_has_to_die() {
        return (m_max_child_cnt <= 0);
    }

    /**
     * Produces individual's children based on the individual's fitness The
     * resulting list if ordered by fitness values
     *
     * @param area_size the area size around
     *
     * @param list the container for children
     */
    public void reproduce(final int area_size, final List<Individual> list) {
        //Generate children
        list.clear();
        IntStream.range(0, area_size).forEachOrdered(idx -> {
            if (m_max_child_cnt > 0) {
                //Create a new individual and add it to the result list
                list.add(new Individual(mutate_expressions(m_exps), m_mgr_id));
                //Decrement the number of children left
                m_max_child_cnt--;
            }
        });
    }

    /**
     * Mutates the vector function
     *
     * @param exps the old vector function expressions
     * @return the new vector function expressions
     */
    private Expression[] mutate_expressions(Expression[] exps) {
        Expression[] new_exps = new Expression[exps.length];
        for (int idx = 0; idx < exps.length; ++idx) {
            new_exps[idx] = Grammar.inst(m_mgr_id, idx).mutate(exps[idx]);
        }
        return new_exps;
    }

    /**
     * Allows to get expressions representing this individual
     *
     * @return copies of expressions representing this individual
     */
    public List<Expression> get_expr() {
        List<Expression> result = new ArrayList();
        for (int idx = 0; idx < m_exps.length; ++idx) {
            result.add(m_exps[idx].duplicate());
        }
        return result;
    }

    /**
     * Get the individual's size
     *
     * @return the size of the individual
     */
    public int get_size() {
        if (m_size == 0) {
            for (int idx = 0; idx < m_exps.length; ++idx) {
                m_size += m_exps[idx].get_size();
            }
        }
        return m_size;
    }

    /**
     * Computes the individual's fitness value from the range [0,1]
     */
    private void compute_fitness() {
        //Try compiling and computing fitness
        m_fitness = FitnessManager.inst().compute_fitness(m_mgr_id, m_exps);

        if (m_fitness == null) {
            LOGGER.log(Level.SEVERE, "Failed computing fitness!");
            m_fitness = new Fitness(0.0);
        }

        //Define the reproduction count based on the fitness
        this.m_max_child_cnt = Math.max(MIN_CHILDREN_CNT,
                (int) (m_fitness.get_fitness() * MAX_CHILDREN_CNT));
    }

    /**
     * Gets/computes the individual's fitness value from the range [0,1]
     *
     * @return the individual's fitness value
     */
    public Fitness get_fitness() {
        return m_fitness;
    }

    @Override
    public String toString() {
        return "[" + m_fitness + ", (" + m_pos_x + ", " + m_pos_y + ")]";
    }

    /**
     *
     * Compares two individuals based on their fitness
     *
     * @param other the other individual to compare with
     * @return true if the fitness of both individuals are equal
     */
    public boolean equals(final Individual other) {
        return this.m_fitness.equals(other.m_fitness);
    }

    /**
     *
     * Compares two individuals based on their fitness
     *
     * @param other the other individual to compare with
     * @return true if the fitness of this individual is smaller than the
     * fitness of the other one
     */
    public boolean is_less(final Individual other) {
        return this.m_fitness.is_less(other.m_fitness);
    }
}
