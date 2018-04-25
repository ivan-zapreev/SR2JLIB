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
 * Defines the fitness type
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public enum FitnessType {

    /**
     * The undefined fitness
     */
    UNDEF(0, "Undefined", 1.0),
    /**
     * The exact fitness
     */
    EXACT(1, "Exact", 1.0),
    /**
     * The fitness based on the arc tangent
     */
    ATANG(2, "Atangent", 100.0),
    /**
     * The fitness based on inverse function
     */
    INVER(3, "Inverse", 150.0);

    //Stores the fitness type identifier value
    private final int m_uid;
    //Stores the fitness type name
    private final String m_name;
    //Stores the fitness type default scaling factor
    private double m_def_scaling;

    FitnessType(final int uid, final String name, final double def_scaling) {
        this.m_uid = uid;
        this.m_name = name;
        this.m_def_scaling = def_scaling;
    }

    /**
     * The fitness unique identifier
     *
     * @return the unique identifier
     */
    public int get_uid() {
        return m_uid;
    }

    /**
     * Allows to check if the fitness type allows for scaling factor
     *
     * @return true if the fitness type supports scaling
     */
    public boolean has_scaling() {
        return (m_def_scaling != 1.0);
    }

    /**
     * Allows to get the fitness scaling factor
     *
     * @return the fitness scaling factor
     */
    public double get_scaling() {
        return m_def_scaling;
    }

    /**
     * Allows to set fitness scaling factor, only if the previous scaling factor
     * is not 0.0 an the new scaling factor is also not 0.0.
     *
     * @param def_scaling sets new scaling factor
     */
    public void set_scaling(final double def_scaling) {
        if (def_scaling != 0.0) {
            if (m_def_scaling != 0.0) {
                m_def_scaling = def_scaling;
            }
        }
    }

    @Override
    public String toString() {
        return m_name;
    }
}
