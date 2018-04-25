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

/**
 * The @see AreaLocker configuration object
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class AreaLockerConfig {

    /**
     * The number of gird elements in x
     */
    public final int m_size_x;

    /**
     * The number of grid elements in y
     */
    public final int m_size_y;

    /**
     * The child spread in x, relative to the parent
     */
    public final int m_ch_sp_x;

    /**
     * The child spread in y, relative to the parent
     */
    public final int m_ch_sp_y;

    /**
     * The basic constructor
     *
     * @param size_x the number of cells in x
     * @param size_y the number of cells in y
     * @param ch_sp_x the child spread in x, relative to the parent
     * @param ch_sp_y the child spread in y, relative to the parent
     */
    public AreaLockerConfig(final int size_x, final int size_y,
            final int ch_sp_x, final int ch_sp_y) {
        this.m_size_x = size_x;
        this.m_size_y = size_y;
        this.m_ch_sp_x = ch_sp_x;
        this.m_ch_sp_y = ch_sp_y;
    }

}
