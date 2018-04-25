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

import java.util.List;

/**
 * The standard interface to observe the population manager
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public interface GridObserver {

    /**
     * Is called when the observation is to be started
     */
    public void start_observing();

    /**
     * Will be called once a new individual becomes a population part. Note
     * that, there could have been another individual in the position of this
     * one, then that one is to be considered as deleted.
     *
     * @param ind the individual to be set into the grid.
     */
    public void set(final Individual ind);

    /**
     * Will be called if an individual is to be removed from the population.
     *
     * @param ind an individual to be removed
     */
    public void remove(final Individual ind);

    /**
     * Allows to get the best fit individuals.
     *
     * @return the best fit individuals
     */
    public List<Individual> get_best_fit_ind();

    /**
     * Is called when the observation is to be stopped
     */
    public void stop_observing();
}
