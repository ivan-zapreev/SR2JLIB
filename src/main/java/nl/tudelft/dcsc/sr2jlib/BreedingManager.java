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

import nl.tudelft.dcsc.sr2jlib.grid.AreaLocker;
import nl.tudelft.dcsc.sr2jlib.grid.GridManager;
import nl.tudelft.dcsc.sr2jlib.grid.GridObserver;
import nl.tudelft.dcsc.sr2jlib.grid.Individual;
import nl.tudelft.dcsc.sr2jlib.fitness.Fitness;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import nl.tudelft.dcsc.sr2jlib.err.ErrorManager;

/**
 * The population grid manager class that allows to perform genetic breeding of
 * the population of the grid.
 *
 * @author Dr. Ivan S. Zapreev
 */
public class BreedingManager {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(BreedingManager.class.getName());

    private final GridManager m_grid_mgr;
    private final int m_num_dofs;
    private final int m_mgr_id;
    private final LinkedList<Individual> m_pop_list;
    private final SelectionType m_sel_type;
    private final AreaLocker m_locker;
    private final boolean m_is_allow_dying;
    private final boolean m_is_avoid_equal;

    /**
     * The basic constructor
     *
     * @param observer the grid observer class
     * @param conf the configuration object
     */
    public BreedingManager(final GridObserver observer, final BreedingManagerConfig conf) {
        this.m_num_dofs = conf.m_num_dofs;
        this.m_grid_mgr = new GridManager(observer, conf.m_size_x, conf.m_size_y);
        this.m_mgr_id = conf.m_mgr_id;
        this.m_sel_type = conf.m_sel_type;
        this.m_locker = new AreaLocker(conf);
        this.m_pop_list = new LinkedList();
        this.m_is_allow_dying = conf.m_is_allow_dying;
        this.m_is_avoid_equal = conf.m_is_avoid_equal;
        //Set the min max children count 
        Individual.set_min_max_child_cnt(conf.m_min_chld_cnt, conf.m_max_chld_cnt);
    }

    /**
     * Allows to check if the new individual winds the old one
     *
     * @param old_ind the old individual
     * @param new_ind the new individual
     * @return true if the new wins the old one
     */
    private boolean does_new_win(final Individual old_ind,
            final Individual new_ind) {
        final Fitness old_fitness = old_ind.get_fitness();
        final Fitness new_fitness = new_ind.get_fitness();
        if (m_sel_type == SelectionType.VALUE) {
            return old_fitness.is_less(new_fitness)
                    || (old_fitness.is_equal(new_fitness)
                    && (new_ind.get_size() < old_ind.get_size()));
        } else {
            if (m_sel_type == SelectionType.PROB) {
                final double total = old_fitness.get_fitness() + new_fitness.get_fitness();
                double outcome = ThreadLocalRandom.current().nextDouble(total);
                return outcome >= old_fitness.get_fitness();
            } else {
                final String msg = "Unsupported selection type: " + m_sel_type;
                LOGGER.log(Level.SEVERE, msg);
                ErrorManager.error(msg);
            }
        }
        return false;
    }

    /**
     * Allows to 'kill' the individual
     *
     * @param old_ind the individual to die
     */
    private void kill_individual(final Individual old_ind) {
        synchronized (m_pop_list) {
            m_pop_list.remove(old_ind);
            m_grid_mgr.remove(old_ind);
        }
    }

    /**
     * Allows to filter out the individuals using the individual evaluator
     *
     * @param eval the individual evaluator
     */
    public void filter_individuals(final IndividualFilter eval) {
        //Request the area locker to be paused
        m_locker.pause();
        if (m_locker.wait_paused(1000)) {
            synchronized (m_pop_list) {
                //Find all the individuals to be removed
                final LinkedList<Individual> to_remove = new LinkedList();
                m_pop_list.forEach((ind) -> {
                    if (eval.evaluate(ind)) {
                        to_remove.add(ind);
                    }
                });
                //Remove those individuals
                to_remove.forEach((ind) -> {
                    kill_individual(ind);
                });
            }
        } else {
            LOGGER.severe("The are locker could not be paused, filtering is skipped!");
        }
        m_locker.resume();
    }

    /**
     * Attempts to set a new individual in place of an old one
     *
     * @param pos_x x position of the individual
     * @param pos_y y position of the individual
     * @param old_ind the old individual
     * @param new_ind the new individual
     * @return true if the new individual is set, otherwise false
     */
    public boolean settle_individual(final int pos_x, final int pos_y,
            final Individual old_ind, final Individual new_ind) {
        boolean is_settle = (old_ind == null)
                || does_new_win(old_ind, new_ind);
        if (is_settle) {
            //Found a free spot or a weaker individual
            new_ind.set_pos_x(pos_x);
            new_ind.set_pos_y(pos_y);
            synchronized (m_pop_list) {
                if (old_ind != null) {
                    m_pop_list.remove(old_ind);
                }
                m_pop_list.add(new_ind);
                m_grid_mgr.set(new_ind);
            }
            LOGGER.log(Level.FINE, "{0} -> Settled individual {1}",
                    new Object[]{Thread.currentThread().getName(), new_ind});
        }
        return is_settle;
    }

    /**
     * Tries to lock an individual at a random position on the grid.
     *
     * @return a locked individual or null if lock has failed
     */
    public Individual aquire_individual() {
        LOGGER.log(Level.FINE, "{0} -> Start achquiring individual",
                Thread.currentThread().getName());
        Individual cand_ind;
        //Find for an individual to mutate
        synchronized (m_pop_list) {
            LOGGER.log(Level.FINE, "{0} -> Current population size is: {1}",
                    new Object[]{Thread.currentThread().getName(),
                        m_pop_list.size()});
            //Get the next individual
            cand_ind = m_pop_list.poll();
        }

        LOGGER.log(Level.FINE, "{0} -> Got individual {1}, trying to lock on",
                new Object[]{Thread.currentThread().getName(), cand_ind});

        //Try to lock on the individual
        final Individual locked_ind = m_locker.lock_area(cand_ind);

        //If locking failed release the individual
        if (locked_ind != cand_ind) {
            release_individual(cand_ind);
        }

        LOGGER.log(Level.FINE, "{0} -> Achquired individual {1}",
                new Object[]{Thread.currentThread().getName(), locked_ind});

        //Return the locked individual or null if we shall stop mutations
        return locked_ind;
    }

    /**
     *
     * Allows to release the previously locked individual
     *
     * @param ind the individual to be released or null if none
     */
    public void release_individual(final Individual ind) {
        if (ind != null) {
            //Return the individual to the list if it has not been deleted from the grid
            synchronized (m_pop_list) {
                LOGGER.log(Level.FINE, "{0} -> Current population size is: {1}",
                        new Object[]{Thread.currentThread().getName(),
                            m_pop_list.size()});
                if (m_grid_mgr.has(ind)) {
                    LOGGER.log(Level.FINE, "{0} -> Released individual {1}",
                            new Object[]{Thread.currentThread().getName(), ind});
                    m_pop_list.add(ind);
                }
                LOGGER.log(Level.FINE, "{0} -> New population size is: {1}",
                        new Object[]{Thread.currentThread().getName(),
                            m_pop_list.size()});
            }
            //unlock the individual
            m_locker.unlock_area(ind);
        }
    }

    /**
     * Allows to generate the initial population
     *
     * @param init_pop_mult the initial population size multiple
     */
    public void generate_initial(final double init_pop_mult) {
        //Generate initial population
        final int size_x = m_grid_mgr.get_size_x();
        final int size_y = m_grid_mgr.get_size_y();
        final int init_pop_size = Math.max(1,
                (int) (size_x * size_y * init_pop_mult));
        LOGGER.log(Level.FINE, "Started creating initial {0} individuals", init_pop_size);
        IntStream.range(0, init_pop_size).forEachOrdered(idx -> {
            int pos_x = 0, pos_y = 0;
            //Create a new individual with some default, always existing position
            Individual new_ind = new Individual(pos_x, pos_y, m_mgr_id, m_num_dofs);

            //Try to lock on the individual to some area, this should eventually succeed
            Individual locked_ind = null;
            while (locked_ind == null) {
                pos_x = ThreadLocalRandom.current().nextInt(0, size_x);
                pos_y = ThreadLocalRandom.current().nextInt(0, size_y);
                new_ind.set_pos_x(pos_x);
                new_ind.set_pos_y(pos_y);
                locked_ind = m_locker.lock_area(new_ind);
            }

            //Get the old individual at position
            final Individual old_ind = m_grid_mgr.get(pos_x, pos_y);

            //Settle the new individual in place of the old one
            settle_individual(pos_x, pos_y, old_ind, new_ind);

            //Unlock the area
            m_locker.unlock_area(new_ind);
        });
        LOGGER.log(Level.FINE, "Finished creating initial {0} individuals", init_pop_size);
    }

    /**
     * Allows to reproduce the given individual
     *
     * @param parent_ind the locked individual to reproduce
     * @param new_inds the container for children
     */
    public void reproduce_individual(final Individual parent_ind, final List<Individual> new_inds) {
        final AreaLocker.Area area = m_locker.get_area(parent_ind);
        final int area_size = area.get_area_size();

        if (m_is_allow_dying && parent_ind.is_has_to_die()) {
            kill_individual(parent_ind);
        } else {
            parent_ind.reproduce(area_size, new_inds);

            LOGGER.log(Level.FINE, "{0} -> Parent {1} got {2} children",
                    new Object[]{Thread.currentThread().getName(),
                        parent_ind, new_inds.size()});

            //Try to insert a new individual at a random location around the predecessor
            for (Individual child_ind : new_inds) {
                LOGGER.log(Level.FINE, "{0} -> Settling child {1} of {2}",
                        new Object[]{Thread.currentThread().getName(), child_ind, parent_ind});
                //If the child is as fit as its parent then try settling it into the parent
                //This shall avoid spreding of the individuals with the same fitness
                if (m_is_avoid_equal && child_ind.is_equal(parent_ind)) {
                    settle_individual(parent_ind.get_pos_x(),
                            parent_ind.get_pos_y(), parent_ind, child_ind);
                } else {
                    //Iterate over the area and see where the new individual can be placed
                    int attempts = area_size;
                    while (attempts != 0) {
                        final int pos_x = area.get_rnd_x_pos();
                        final int pos_y = area.get_rnd_y_pos();
                        final Individual old_ind = m_grid_mgr.get(pos_x, pos_y);
                        if (settle_individual(pos_x, pos_y, old_ind, child_ind)) {
                            break;
                        }
                        attempts--;
                    }
                    LOGGER.log(Level.FINE, "{0} -> Settling child {1} of {2} is DONE",
                            new Object[]{Thread.currentThread().getName(), child_ind, parent_ind});
                }
            }
        }
    }
}
