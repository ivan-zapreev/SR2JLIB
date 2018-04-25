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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the randomizer class that can choose randomly from its elements
 * based on a discrete probability distribution defined by the element
 * expression weights.
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
class Randomizer {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(Randomizer.class.getName());

    //Store the mapping of an expression to its weight
    private final List<RandomInterval> m_rand_ivls;
    //Stores the upper bound for the random value
    private double m_upper_bound;

    /**
     * The basic constructor
     */
    Randomizer() {
        this.m_upper_bound = 0;
        this.m_rand_ivls = new ArrayList();
    }

    /**
     * Allows to check if this randomizer is empty
     *
     * @return true if the randomizer has no intervals, otherwise false
     */
    public boolean is_empty() {
        return m_rand_ivls.isEmpty();
    }

    /**
     * Get the list of stored intervals
     *
     * @return the list of stored intervals
     */
    public List<RandomInterval> get_ivls() {
        return m_rand_ivls;
    }

    /**
     * Returns the number of random intervals stored
     *
     * @return the number of random intervals stored
     */
    public int get_num_ivls() {
        return m_rand_ivls.size();
    }

    /**
     * Randomly chooses and expression based on the distribution
     *
     * @return a randomly chosen expression
     */
    public Expression choose_expression() {
        LOGGER.log(Level.FINE, "Choosing one of {0} expressions, upper bound {1}",
                new Object[]{m_rand_ivls.size(), m_upper_bound});
        if (m_rand_ivls.size() == 1) {
            RandomInterval elem = m_rand_ivls.get(0);
            LOGGER.log(Level.FINE, "The found, expression is: {0}!", elem.get_exp());
            return elem.get_exp().duplicate();
        } else {
            final double value = ThreadLocalRandom.current().nextDouble(m_upper_bound);
            LOGGER.log(Level.FINE, "Choosing a value from [0..{0}) gave: {1}",
                    new Object[]{m_upper_bound, value});
            for (int idx = 0; idx < m_rand_ivls.size(); ++idx) {
                RandomInterval elem = m_rand_ivls.get(idx);
                LOGGER.log(Level.FINE, "Considering intervala {0}", elem);
                if (elem.is_inside(value)) {
                    LOGGER.log(Level.FINE, "The value {0} interval is found, expression {1}!",
                            new Object[]{value, elem.get_exp()});
                    return elem.get_exp().duplicate();
                }
            }
            LOGGER.log(Level.FINE, "The value {0} interval is NOT found!", value);
            throw new IllegalArgumentException("Unable to match value " + value + " to a random interval!");
        }
    }

    /**
     * Allows to register a new expression with its weight
     *
     * @param exp the expression
     * @param weight its weight
     */
    public void register(final Expression exp, final double weight) {
        m_rand_ivls.add(new RandomInterval(exp, weight));
    }

    /**
     * Is to be called when the randomiser is filled in with expressions and the
     * distribution can be cooked up
     */
    public void prepare_distrib() {
        final int num_ints = m_rand_ivls.size();
        LOGGER.log(Level.FINE, "Number of intervals is {0}", num_ints);
        RandomInterval prev_ivl = m_rand_ivls.get(0);
        m_upper_bound = prev_ivl.get_right_bnd();
        for (int idx = 1; idx < num_ints; ++idx) {
            //Get the current interval and shift it
            RandomInterval curr_ivl = m_rand_ivls.get(idx);
            curr_ivl.shift(prev_ivl.get_right_bnd());
            //Remember the right bound of the last interval
            if (idx == (num_ints - 1)) {
                m_upper_bound = curr_ivl.get_right_bnd();
            } else {
                prev_ivl = curr_ivl;
            }
        }
        LOGGER.log(Level.FINE, "The intervals upper bound is {0}", m_upper_bound);
    }
}
