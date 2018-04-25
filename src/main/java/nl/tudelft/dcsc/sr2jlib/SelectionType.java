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
package nl.tudelft.dcsc.sr2jlib;

/**
 * Defines the selection type
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public enum SelectionType {

    /**
     * The value based selection type when the higher fitness wins.
     */
    VALUE(0, "Value based"),
    /**
     * The probabilistic selection type where fitness as taken as a probability
     * to win and a randomized selection is oned. Not very efficient for when
     * both individuals have close probability values.
     */
    PROB(1, "Probabilistic");

    private final int m_idx;
    private final String m_name;

    SelectionType(final int idx, final String name) {
        this.m_idx = idx;
        this.m_name = name;
    }

    /**
     * Allows to get the selection type unique index
     *
     * @return the selection type unique index
     */
    public int get_idx() {
        return m_idx;
    }

    @Override
    public String toString() {
        return m_name;
    }
}
