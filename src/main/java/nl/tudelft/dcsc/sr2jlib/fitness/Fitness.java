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
package nl.tudelft.dcsc.sr2jlib.fitness;

/**
 * The class that will be used for storing the fitness values
 *
 * @author Dr. Ivan S. Zapreev
 */
public class Fitness {

    /**
     * Stores the fitness value
     */
    protected final double m_ftn;

    /**
     * The constructor.
     *
     * @param ftn the fitness value
     */
    public Fitness(final double ftn) {
        m_ftn = ftn;
    }

    /**
     * Get the requested fitness
     *
     * @return the requested fitness
     */
    public double get_fitness() {
        return m_ftn;
    }

    /**
     * Checks if the requested fitness is zero
     *
     * @return true if the requested fitness is zero
     */
    public boolean is_zerro() {
        return (m_ftn == 0.0);
    }

    /**
     * Checks if the exact fitness is one
     *
     * @return true if the exact fitness is one
     */
    public boolean is_one() {
        return (m_ftn == 1.0);
    }

    @Override
    public String toString() {
        return "[fitness: " + m_ftn + "]";
    }

    /**
     * Checks if this fitness is equal to the other one
     *
     * @param other the other fitness
     * @return true if the two fitness values are equal, otherwise false
     */
    public boolean equals(final Fitness other) {
        if (other != null) {
            return (this.m_ftn == other.m_ftn);
        } else {
            throw new IllegalArgumentException("Attempting to compare withness with null!");
        }
    }

    /**
     * Allow to check whether one fitness is less than another
     *
     * @param other the other fitness
     * @return true if this fitness is less than the other one
     */
    public boolean is_less(final Fitness other) {
        return (this.m_ftn < other.m_ftn);
    }
};
