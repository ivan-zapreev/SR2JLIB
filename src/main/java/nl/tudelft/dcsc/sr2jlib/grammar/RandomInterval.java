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

import java.util.logging.Logger;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.Expression;

/**
 * This class represents an interval for the discrete distribution
 *
 * @author Dr. Ivan S. Zapreev
 */
class RandomInterval {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(RandomInterval.class.getName());

    private double m_left;
    private double m_right;
    private final double m_weight;
    private final Expression m_exp;

    /**
     * The basic constructor
     *
     * @param exp the expression related to this interval
     * @param weight the expression's weight
     */
    RandomInterval(final Expression exp, final double weight) {
        this.m_left = 0.0;
        this.m_right = weight;
        this.m_weight = weight;
        this.m_exp = exp;
    }

    /**
     *
     * Shift the interval by the given value
     *
     * @param value the value to be shifted by
     */
    public void shift(final double value) {
        this.m_left += value;
        this.m_right += value;
    }

    /**
     * Provides the left value of the interval
     *
     * @return the left value of the interval
     */
    public double get_left_bnd() {
        return m_left;
    }

    /**
     * Provides the right value of the interval
     *
     * @return the right value of the interval
     */
    public double get_right_bnd() {
        return m_right;
    }

    /**
     * Provides the encapsulated expression
     *
     * @return the expression
     */
    public Expression get_exp() {
        return m_exp;
    }

    /**
     * Provides the expression weight
     *
     * @return the expression weight
     */
    public double get_weight() {
        return m_weight;
    }

    /**
     * Allows to check if the given value is inside the half-open interval:
     * [m_left, m_right)
     *
     * @param value the value
     * @return true if the value is within the interval
     */
    public boolean is_inside(final double value) {
        return (m_left <= value) && (value < m_right);
    }

    @Override
    public String toString() {
        return "[" + m_left + "," + m_right + ") -> " + m_exp;
    }
}
