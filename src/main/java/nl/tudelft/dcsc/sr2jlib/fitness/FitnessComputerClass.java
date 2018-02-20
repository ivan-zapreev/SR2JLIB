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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tudelft.dcsc.sr2jlib.err.ErrorManager;
import nl.tudelft.dcsc.sr2jlib.instance.Creator;
import nl.tudelft.dcsc.sr2jlib.instance.Loader;

/**
 * The fitness computer interface
 *
 * @author Dr. Ivan S. Zapreev
 */
public abstract class FitnessComputerClass extends FitnessComputerString {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(FitnessComputerInstance.class.getName());

    //Stores the uid issuing counter
    private static long uid_cnt = 0;
    private static final Object UID_SYNCH = new Object();

    /**
     * Issues an individual id.
     *
     * @return an individual id.
     */
    private static long acquire_uid() {
        synchronized (UID_SYNCH) {
            return uid_cnt++;
        }
    }

    @Override
    public Fitness compute_fitness(
            final int mgr_id, final String[] exp_strs) {
        Fitness ftn = null;
        //Acquire the uid for a unique individual class name
        final long uid = acquire_uid();
        final String class_name = Creator.get_class_name(uid);
        try {
            //Attempt compilation
            Creator.prepare(uid, exp_strs);
            //Comute fitness
            ftn = compute_fitness(mgr_id, class_name);
            //Remove the old class from the loader
            Loader.remove_old(class_name);
        } catch (IllegalArgumentException | IllegalStateException
                | ClassNotFoundException | IllegalAccessException
                | InvocationTargetException ex) {
            final String msg = "Failed to compute the individual"
                    + " fitness for: " + class_name;
            LOGGER.log(Level.SEVERE, msg, ex);
            ErrorManager.error(msg, ex);
        }
        LOGGER.log(Level.INFO, "Generated {0}: {1}, fitness: {2}",
                new Object[]{class_name, Arrays.toString(exp_strs), ftn});
        return ftn;
    }

    /**
     * Allows to compute fitness for the given class and manager id
     *
     * @param mgr_id the population manager id from which the individual is
     * originated
     * @param class_name the name of the class storing individual
     * @return individual's fitness
     * @throws IllegalStateException some illegal state
     * @throws IllegalArgumentException an illegal argument value
     * @throws ClassNotFoundException the individual class is not found, e.g.
     * could not be compiled
     * @throws IllegalAccessException illegal access to individual class methods
     * (should not be happening)
     * @throws InvocationTargetException failed calling individual class methods
     * (should not be happening)
     */
    public abstract Fitness compute_fitness(
            final int mgr_id, final String class_name)
            throws IllegalStateException, IllegalArgumentException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException;

}
