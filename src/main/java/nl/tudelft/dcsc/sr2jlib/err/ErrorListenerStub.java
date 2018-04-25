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
package nl.tudelft.dcsc.sr2jlib.err;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A stub implementation of the error listener
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class ErrorListenerStub implements ErrorListener {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(ErrorListenerStub.class.getName());

    @Override
    public void error(String msg, final Exception ex) {
        msg = ((msg == null) ? "" : msg);
        if (ex == null) {
            LOGGER.log(Level.SEVERE, msg);
        } else {
            LOGGER.log(Level.SEVERE, msg, ex);
        }
    }

}
