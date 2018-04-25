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

import nl.tudelft.dcsc.sr2jlib.grid.GridObserver;

/**
 * The configuration class for the @see GeneticManager
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class ProcessManagerConfig extends BreedingManagerConfig {

    /**
     * The callback object to be used once the process manager has stopped
     */
    public final FinishedCallback m_done_cb;

    /**
     * The grid observer object
     */
    public final GridObserver m_observer;

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
     * @param mgr_id the id of the population manager
     * @param init_pop_mult the initial population coefficient relative to the
     * number of grid cells, from (0.0,1.0]
     * @param num_workers the number of worker threads for this manager, each
     * thread works on reproducing individuals
     * @param max_num_reps the maximum number of reproductions, defined the
     * run-time of the symbolic regression on the grid
     * @param num_dofs the number of dimensions for the individual's vector
     * function
     * @param size_x the number of the population grid cells in x
     * @param size_y the number of the population grid cells in y
     * @param ch_sp_x the number of positions from the parent in x the children
     * will be spread
     * @param ch_sp_y the number of positions from the parent in y the children
     * will be spread
     * @param sel_type the individual's selection type
     * @param is_allow_dying if true then individuals are dying after they had a
     * certain number of children
     * @param is_avoid_equal if true then the children what are equally fit to
     * their parent will be attempted to be settled into their parent's
     * position. This shall prevent spreading of the equally fit children with
     * meaningless mutations.
     * @param min_chld_cnt the minimum number of children before dying
     * @param max_chld_cnt the maximum number of children before dying
     * @param observer the fitness observer instance to monitor the population
     * @param done_cb the call back to be called once this manager has finished
     */
    public ProcessManagerConfig(
            final int mgr_id,
            final double init_pop_mult,
            final int num_workers,
            final long max_num_reps,
            final int num_dofs,
            final int size_x, final int size_y,
            final int ch_sp_x, final int ch_sp_y,
            final SelectionType sel_type,
            final boolean is_allow_dying,
            final boolean is_avoid_equal,
            final int min_chld_cnt,
            final int max_chld_cnt,
            final GridObserver observer,
            final FinishedCallback done_cb
    ) {
        super(mgr_id, num_dofs, size_x, size_y,
                ch_sp_x, ch_sp_y, sel_type,
                is_allow_dying, is_avoid_equal,
                min_chld_cnt, max_chld_cnt);
        this.m_done_cb = done_cb;
        this.m_observer = observer;
        this.m_init_pop_mult = init_pop_mult;
        this.m_num_workers = num_workers;
        this.m_max_num_reps = max_num_reps;
    }
}
