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

import java.util.stream.IntStream;

/**
 *
 * Is the population grid class that stores the current population
 *
 * @author Dr. Ivan S. Zapreev
 */
public class GridManager {

    private final int m_size_x;
    private final int m_size_y;
    private final Individual[][] m_pop_grid;

    /**
     * The basic constructor
     *
     * @param size_x the grid size in x
     * @param size_y the grid size in y
     */
    public GridManager(final int size_x, final int size_y) {
        this.m_size_x = size_x;
        this.m_size_y = size_y;
        this.m_pop_grid = new Individual[size_x][];
        IntStream.range(0, size_x).forEachOrdered(idx -> {
            this.m_pop_grid[idx] = new Individual[size_y];
        });
    }

    /**
     * Get the grid size in x
     *
     * @return the grid size in x
     */
    public int get_size_x() {
        return m_size_x;
    }

    /**
     * Get the grid size in y
     *
     * @return the grid size in y
     */
    public int get_size_y() {
        return m_size_y;
    }

    /**
     * Allows to check if the individual is present on the grid
     *
     * @param ind the individual with properly set x,y coordinates
     * @return true if the individual is present by the given coordinates
     */
    public synchronized boolean has_individual(final Individual ind) {
        return (m_pop_grid[ind.get_pos_x()][ind.get_pos_y()] == ind);
    }

    /**
     * Allows to retrieve the individual by the given coordinates
     *
     * @param pos_x the x coordinate
     * @param pos_y the y coordinate
     * @return the individual
     */
    public synchronized Individual get_individual(final int pos_x, final int pos_y) {
        return m_pop_grid[pos_x][pos_y];
    }

    /**
     * Allows to remove an old individual from the grid
     *
     * @param old_ind an old individual to be removed
     */
    public synchronized void remove_individual(final Individual old_ind) {
        //Remove an old individual from the grid
        m_pop_grid[old_ind.get_pos_x()][old_ind.get_pos_y()] = null;
    }

    /**
     * Allows to schedule the population chart update
     *
     * @param new_ind the new individual to be put in place of the old one
     */
    public synchronized void set_individual(final Individual new_ind) {
        //Add new individual to the grid
        m_pop_grid[new_ind.get_pos_x()][new_ind.get_pos_y()] = new_ind;
    }
}
