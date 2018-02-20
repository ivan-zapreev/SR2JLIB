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

import nl.tudelft.dcsc.sr2jlib.grid.AreaLockerConfig;

/**
 * The configuration class for the @see BreedingManager
 *
 * @author Dr. Ivan S. Zapreev
 */
public class BreedingManagerConfig extends AreaLockerConfig {

    /**
     * The number of dofs of the individual's vector function
     */
    public final int m_num_dofs;

    /**
     * The process manager id
     */
    public final int m_mgr_id;

    /**
     * The individual selection type
     */
    public final SelectionType m_sel_type;

    /**
     * Stores the flag indicating if individuals can die by themselves
     */
    public final boolean m_is_allow_dying;

    /**
     * The minimum number of children before dying
     */
    public final int m_min_chld_cnt;

    /**
     * The maximum number of children before dying
     */
    public final int m_max_chld_cnt;

    /**
     * The basic constructor
     *
     * @param mgr_id the id of the population manager
     * @param num_dofs the number of dimensions for the individual
     * @param size_x the number of cells in x
     * @param size_y the number of cells in y
     * @param ch_sp_x the child spread in x, relative to the parent
     * @param ch_sp_y the child spread in y, relative to the parent
     * @param sel_type the individual selection type,
     * @param is_allow_dying if true then individuals are dying after they had
     * some number of children
     * @param min_chld_cnt the minimum number of children before dying
     * @param max_chld_cnt the maximum number of children before dying
     */
    public BreedingManagerConfig(
            final int mgr_id,
            final int num_dofs,
            final int size_x, final int size_y,
            final int ch_sp_x, final int ch_sp_y,
            final SelectionType sel_type,
            final boolean is_allow_dying,
            final int min_chld_cnt,
            final int max_chld_cnt) {
        super(size_x, size_y, ch_sp_x, ch_sp_y);
        this.m_num_dofs = num_dofs;
        this.m_mgr_id = mgr_id;
        this.m_sel_type = sel_type;
        this.m_is_allow_dying = is_allow_dying;
        this.m_min_chld_cnt = min_chld_cnt;
        this.m_max_chld_cnt = max_chld_cnt;
    }

}
