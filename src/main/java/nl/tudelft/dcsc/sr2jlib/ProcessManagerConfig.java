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
 * The configuration class for the @see GeneticManager
 *
 * @author Dr. Ivan S. Zapreev
 */
public class ProcessManagerConfig extends BreedingManagerConfig {

    /**
     * The multiplier (coefficient) the defines the number of initial population
     * individuals relative to the grid size.
     */
    public final double m_init_pop_mult;

    /**
     * The number of worker threads to work in parallel.
     */
    public final int m_num_workers;

    /**
     * The maximum allowed number of reproductions.
     */
    public final long m_max_num_reps;

    /**
     * The basic constructor
     *
     * @param init_pop_mult the multiple for the initial population
     * @param num_workers the number of worker threads for this manager
     * @param max_num_reps the maximum number of reproductions
     * @param num_dofs the number of dimensions for the individual
     * @param size_x the number of cells in x
     * @param size_y the number of cells in y
     * @param ch_sp_x the child spread in x, relative to the parent
     * @param ch_sp_y the child spread in y, relative to the parent
     * @param mgr_id the id of the population manager
     * @param sel_type the individual selection type,
     * @param is_allow_dying if true then individuals are dying after they had
     * some number of children
     * @param min_chld_cnt the minimum number of children before dying
     * @param max_chld_cnt the maximum number of children before dying
     */
    public ProcessManagerConfig(
            final double init_pop_mult,
            final int num_workers,
            final long max_num_reps,
            final int num_dofs,
            final int size_x, final int size_y,
            final int ch_sp_x, final int ch_sp_y,
            final int mgr_id,
            final SelectionType sel_type,
            final boolean is_allow_dying,
            final int min_chld_cnt,
            final int max_chld_cnt) {
        super(num_dofs, size_x, size_y, ch_sp_x, ch_sp_y, mgr_id,
                sel_type, is_allow_dying, min_chld_cnt, max_chld_cnt);
        this.m_init_pop_mult = init_pop_mult;
        this.m_num_workers = num_workers;
        this.m_max_num_reps = max_num_reps;
    }
}
