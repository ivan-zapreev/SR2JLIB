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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows to lock the area around individuals while the reproduction
 * happens
 *
 * @author Dr. Ivan S. Zapreev
 */
public class AreaLocker {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(AreaLocker.class.getName());

    /**
     * This class represents an area on the gird that can be locked
     */
    public class Area {

        private final int m_min_x;
        private final int m_elems_x;
        private final int m_min_y;
        private final int m_elems_y;

        /**
         * The basic constructor
         *
         * @param ind the individual holding a grid cell being a center point of
         * the area
         */
        Area(final Individual ind) {
            m_min_x = Math.max(0, ind.get_pos_x() - m_ch_sp_x);
            final int max_x = Math.min(ind.get_pos_x() + m_ch_sp_x, m_max_x);
            m_elems_x = (max_x - m_min_x) + 1;
            m_min_y = Math.max(0, ind.get_pos_y() - m_ch_sp_y);
            final int max_y = Math.min(ind.get_pos_y() + m_ch_sp_y, m_max_y);
            m_elems_y = (max_y - m_min_y) + 1;
        }

        /**
         * Returns the maximum area size
         *
         * @return the maximum area size
         */
        public int get_area_size() {
            return m_elems_x * m_elems_y;
        }

        /**
         * Gets a random position in the area around the individual
         *
         * @return a random x position
         */
        public int get_rnd_x_pos() {
            return m_min_x + ThreadLocalRandom.current().nextInt(m_elems_x);
        }

        /**
         * Gets a random position in the area around the individual
         *
         * @return a random y position
         */
        public int get_rnd_y_pos() {
            return m_min_y + ThreadLocalRandom.current().nextInt(m_elems_y);
        }
    };

    private final Set<Individual> m_mut_set;
    private final int m_max_x;
    private final int m_max_y;
    private final int m_ch_sp_x;
    private final int m_ch_sp_y;
    private boolean m_is_paused;

    /**
     * The area locker constructor
     *
     * @param conf the configuration object
     */
    public AreaLocker(final AreaLockerConfig conf) {
        m_mut_set = new HashSet();
        m_max_x = conf.m_size_x - 1;
        m_max_y = conf.m_size_y - 1;
        m_ch_sp_x = conf.m_ch_sp_x;
        m_ch_sp_y = conf.m_ch_sp_y;
        m_is_paused = false;
    }

    /**
     * The individual to lock on
     *
     * @param ind the individual to attempt a lock on
     * @return the locked individual or null if the lock has failed
     */
    public Individual lock_area(final Individual ind) {
        if (ind != null) {
            synchronized (m_mut_set) {
                if (!m_is_paused) {
                    //Count the number of overlaps in the stream
                    final Optional<Individual> opt = m_mut_set.stream().filter(elem -> ((Math.abs(elem.get_pos_x() - ind.get_pos_x()) <= 2 * m_ch_sp_x)
                            && (Math.abs(elem.get_pos_y() - ind.get_pos_y()) <= 2 * m_ch_sp_y))).findFirst();
                    //If the number of overlaps is zero then allow to lock, otherwise not
                    if (opt.isPresent()) {
                        LOGGER.log(Level.FINE, "{0} -> Failed to lock individual {1}",
                                new Object[]{Thread.currentThread().getName(), ind});
                        return null;
                    } else {
                        m_mut_set.add(ind);
                        LOGGER.log(Level.FINE, "Locked Individual {0} size: {1}",
                                new Object[]{ind, m_mut_set.size()});
                        return ind;
                    }
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Allows to request pausing of the are locker.
     */
    public void pause() {
        synchronized (m_mut_set) {
            m_is_paused = true;
        }
    }

    /**
     * Waits until all of the locked areas are released, must only be invoked
     * after the pause is requested.
     *
     * @param timeout the timeout in milliseconds to wake up during waiting
     * @return true if the area locker is paused, otherwise false
     */
    public boolean wait_paused(final long timeout) {
        while (m_is_paused) {
            try {
                synchronized (m_mut_set) {
                    if (m_mut_set.isEmpty()) {
                        return true;
                    } else {
                        m_mut_set.wait(timeout);
                    }
                }
            } catch (InterruptedException ex) {
            }
        }
        return false;
    }

    /**
     * Allows to resume the paused action locker
     */
    public void resume() {
        synchronized (m_mut_set) {
            m_is_paused = false;
            m_mut_set.notifyAll();
        }
    }

    /**
     * Allows to release the lock assigned to the individual
     *
     * @param ind the individual which was locked
     */
    public void unlock_area(final Individual ind) {
        synchronized (m_mut_set) {
            m_mut_set.remove(ind);
            LOGGER.log(Level.FINE, "Unlocked Individual {0} size: {1}",
                    new Object[]{ind, m_mut_set.size()});
            if (m_mut_set.isEmpty()) {
                m_mut_set.notifyAll();
            }
        }
    }

    /**
     * Allows to get an area object for the given individual
     *
     * @param ind the individual for which the area is taken
     * @return the corresponding area object
     */
    public Area get_area(final Individual ind) {
        return new Area(ind);
    }
}
