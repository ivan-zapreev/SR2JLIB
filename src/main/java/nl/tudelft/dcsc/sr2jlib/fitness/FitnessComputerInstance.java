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
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tudelft.dcsc.sr2jlib.err.ErrorManager;
import nl.tudelft.dcsc.sr2jlib.instance.Creator;
import nl.tudelft.dcsc.sr2jlib.instance.Loader;

/**
 * The fitness computer class to be derived from when on needs to compute
 * fitness based on the individual implementing method.
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public abstract class FitnessComputerInstance extends FitnessComputerClass {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(FitnessComputerInstance.class.getName());

    //Stores the class loader
    private final Loader m_loader = new Loader();

    @Override
    public final Fitness compute_fitness(
            final int mgr_id, final String class_name)
            throws IllegalStateException, IllegalArgumentException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException {
        LOGGER.log(Level.FINE, "About to compute fitness for class {0}", class_name);
        try {
            Class<?> ind_class = m_loader.loadClassNC(class_name.replaceAll("/", "."));
            Method gnd_method = ind_class.getMethod(Creator.GET_NUM_DOFS);
            final int num_dofs = (Integer) gnd_method.invoke(null);
            Method[] vf = new Method[num_dofs];
            for (int idx = 0; idx < num_dofs; ++idx) {
                vf[idx] = ind_class.getMethod(Creator.EVALUATE + idx, double[].class);
            }
            return compute_fitness(mgr_id, vf);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
            final String msg = "Failed when loading and instantiating of " + class_name;
            LOGGER.log(Level.SEVERE, msg, ex);
            ErrorManager.error(msg, ex);
        }

        return null;
    }

    /**
     *
     * Allows to compute fitness for the given individual vector function
     * methods and manager id
     *
     * @param mgr_id the population manager id from which the individual is
     * originated
     * @param vf an array of individual class methods representing the vector
     * function
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
            final int mgr_id, final Method[] vf)
            throws IllegalStateException, IllegalArgumentException,
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException;
}
