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
import nl.tudelft.dcsc.sr2jlib.grid.Individual;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * This is a population manager class responsible for managing the GP process
 *
 * @author Dr. Ivan S. Zapreev
 */
public class ProcessManager {

    private static final Logger LOGGER = Logger.getLogger(ProcessManager.class.getName());

    private final FinishedCallback m_done_cb;
    private final GridObserver m_observer;
    private int m_num_workers;
    private long m_num_reps;
    private final long m_max_num_reps;
    private final ExecutorService m_executor;
    private final BreedingManager m_breeder;
    private final double m_init_pop_mult;

    /**
     * The basic constructor
     *
     * @param conf the manger configuration object
     */
    public ProcessManager(final ProcessManagerConfig conf) {
        this.m_done_cb = conf.m_done_cb;
        this.m_observer = conf.m_observer;
        this.m_init_pop_mult = conf.m_init_pop_mult;
        this.m_num_workers = conf.m_num_workers;
        this.m_max_num_reps = conf.m_max_num_reps;
        this.m_breeder = new BreedingManager(m_observer, conf);

        this.m_num_reps = 0;

        final UncaughtExceptionHandler ueh = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread th, Throwable ex) {
                LOGGER.log(Level.SEVERE, "The worker thread " + th.getName() + " has failed!", ex);
            }
        };
        this.m_executor = Executors.newFixedThreadPool(m_num_workers, new ThreadFactory() {
            private int idx = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread th = new Thread(r);
                th.setName("SR-Worker-" + conf.m_mgr_id + "-" + idx);
                th.setDaemon(true);
                th.setUncaughtExceptionHandler(ueh);
                ++idx;
                return th;
            }
        });
    }

    /**
     * Is the Genetic procedure worker class responsible for running GP
     * iterations
     */
    private class GpWorkerTask implements Runnable {

        GpWorkerTask(final int idx) {
            super();
            LOGGER.log(Level.FINE, "Worker {0} created!", idx);
        }

        @Override
        public void run() {
            LOGGER.log(Level.FINE, "{0} -> Thread started!", Thread.currentThread().getName());

            final List<Individual> inds = new ArrayList();
            try {
                //Generate initial population
                m_breeder.generate_initial(m_init_pop_mult / m_num_workers);
                //Go on with reproduction
                Individual locked_ind = null;
                while (is_can_reproduce()) {
                    try {
                        //First get a new individual
                        locked_ind = m_breeder.aquire_individual();
                        //Check if we can reproduce
                        if ((locked_ind != null) && is_reproduction_allowed()) {
                            //Reproduce individual
                            m_breeder.reproduce_individual(locked_ind, inds);
                        }
                    } catch (Throwable ex) {
                        LOGGER.log(Level.SEVERE, "Exception in a GP worker!", ex);
                    } finally {
                        //Unlock the individual
                        m_breeder.release_individual(locked_ind);
                    }
                }
            } catch (Throwable ex) {
                LOGGER.log(Level.SEVERE, "Exception in a GP worker, premature finish!", ex);
            }

            LOGGER.log(Level.FINE, "{0} -> Thread finished!", Thread.currentThread().getName());

            //Notify that the thread is stopped
            notify_worker_finished();
        }
    }

    /**
     * Request reproduction, if the maximum allowed number of reproductions is
     * not yet reached returns true, otherwise false.
     *
     * @return true if a reproduction is granted
     */
    private synchronized boolean is_reproduction_allowed() {
        if (m_num_reps < m_max_num_reps) {
            m_num_reps++;
            LOGGER.log(Level.FINE, "Mutation {0}/{1}",
                    new Object[]{m_num_reps, m_max_num_reps});
            return true;
        } else {
            return false;
        }
    }

    /**
     * Allows to check if we can still reproduce
     *
     * @return true if the reproduction is still allowed
     */
    private synchronized boolean is_can_reproduce() {
        return (m_num_reps < m_max_num_reps);
    }

    /**
     * Force mutations to stop
     *
     * @return true if a mutation is granted
     */
    private synchronized void request_stop() {
        m_num_reps = m_max_num_reps;
    }

    /**
     * Allows to get the best fit individuals.
     *
     * @return the best fit individuals
     */
    public List<Individual> get_best_fit_ind() {
        return m_observer.get_best_fit_ind();
    }

    /**
     * Starts the GP procedure
     */
    public void start() {
        //Start the observer
        m_observer.start_observing();

        //Start the workers
        IntStream.range(0, m_num_workers).forEachOrdered(
                idx -> {
                    m_executor.submit(new GpWorkerTask(idx));
                });
    }

    /**
     * Allows to stop the population manager
     *
     * @param is_soft if soft then the thread executors are stopped softly
     */
    public void stop(final boolean is_soft) {
        //Stop the GP process if it is running
        request_stop();

        //Stop the executors
        if (is_soft) {
            m_executor.shutdown();
        } else {
            m_executor.shutdownNow();
        }

        //Stop the observer
        m_observer.stop_observing();

        //Notify that process is stopped
        m_done_cb.finished(this);
    }

    /**
     * Shall be called from the worker when its execution is finished
     */
    private synchronized void notify_worker_finished() {
        //Decrement the number of active workers
        --m_num_workers;
        //Check if all workers have been stoped
        if (m_num_workers == 0) {
            this.stop(true);
        }
    }

    /**
     * Allows to filter out the individuals using the individual evaluator. In
     * order to do so a separate thread is used so this method is non-blocking.
     *
     * @param eval the individual evaluator
     */
    public void filter_individuals(final IndividualFilter eval) {
        m_breeder.filter_individuals(eval);
    }
}
