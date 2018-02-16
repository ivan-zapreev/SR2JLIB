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

/**
 *
 * The error manager
 *
 * @author Dr. Ivan S. Zapreev
 */
public class ErrorManager {

    private ErrorListener m_el;

    /**
     * Basic constructor
     */
    private ErrorManager() {
        m_el = new ErrorListenerStub();
    }

    /**
     * Allows to set a new instance of the error listener
     *
     * @param new_el the new error listener
     * @return an old error listener
     */
    public ErrorListener set_listener(final ErrorListener new_el) {
        ErrorListener old_el = this.m_el;
        this.m_el = new_el;
        return old_el;
    }

    /**
     * Allows to retrieve an instance of the error manager
     *
     * @return an instance of the error manager
     */
    public static ErrorManager inst() {
        return ErrorManagerHolder.INSTANCE;
    }

    /**
     * Reports an error
     *
     * @param msg the error message
     */
    public static void error(final String msg) {
        error(msg, null);
    }

    /**
     * Reports an exception
     *
     * @param ex the exception
     */
    public static void error(final Exception ex) {
        error(null, ex);
    }

    /**
     * Reports an error/exception
     *
     * @param msg the additional message, may be null if none provided
     * @param ex the exception, may be null if none provided
     */
    public static void error(String msg, Exception ex) {
        ErrorManagerHolder.INSTANCE.m_el.error(msg, ex);
    }

    /**
     * Stores an instance of the error manager
     */
    private static class ErrorManagerHolder {

        //Stores an instance of the error manager
        private static final ErrorManager INSTANCE = new ErrorManager();
    }
}
