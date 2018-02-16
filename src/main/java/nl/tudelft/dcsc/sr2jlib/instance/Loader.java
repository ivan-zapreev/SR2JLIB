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
package nl.tudelft.dcsc.sr2jlib.instance;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class loading for individuals
 *
 * NOTE: This class re-uses the internal byte buffer for efficiency, this is why
 * it is solely targeting the individual classes. I.e. we expect the loadClass
 * method NOT to be called recursively!
 *
 * @author Dr. Ivan S. Zapreev
 */
public class Loader extends ClassLoader {

    //Stores the bytes buffer size
    private static final int BYTE_BUF_SIZE = 1048576;
    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(Loader.class.getName());
    //Store the parent class loader of this loader
    private static final ClassLoader PARENT_CL = ClassLoader.getSystemClassLoader();

    //The number of read files into the buffer
    private int m_read = 0;
    //The buffer for the class bytecode
    private final byte[] m_buffer = new byte[BYTE_BUF_SIZE];
    //The byte stream for the data
    private final ByteArrayOutputStream m_bo_temp = new ByteArrayOutputStream();

    @Override
    public synchronized Class loadClass(String name)
            throws ClassNotFoundException {
        final Class cls;
        LOGGER.log(Level.FINE, "Loading class: {0}", name);
        if (name.startsWith(Creator.PACKAGE_NAME)) {
            cls = loadClassNC(name);
        } else {
            LOGGER.log(Level.FINE, "Loading class: {0} with Global class loader", name);
            cls = PARENT_CL.loadClass(name);
        }
        LOGGER.log(Level.FINE, "The class: {0} is loaded!", name);
        return cls;
    }

    /**
     * Loads the class through this class loader without caching it.
     *
     * @param name the class name to load
     * @return the class
     * @throws ClassNotFoundException if class is not found
     */
    public synchronized Class loadClassNC(final String name)
            throws ClassNotFoundException {
        LOGGER.log(Level.FINE, "Loading class: {0} with Dynamic class loader", name);
        final String file_name = toFilePath(name);
        try {
            final InputStream stream = new FileInputStream(file_name);
            final byte[] data = readData(stream);
            return loadClass(data, m_read, name);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load class from file!", ex);
            throw new ClassNotFoundException("Failed to load class from file!", ex);
        }
    }

    /**
     * Removes the old class by name
     *
     * @param name the old class name
     */
    public static void remove_old(final String name) {
        final String file_name = toFilePath(name);
        File file = new File(file_name);
        file.delete();
    }

    /**
     * Defines a new class instance for the given class data and name
     *
     * @param data the class byte code data
     * @param length the length o the loaded data
     * @param name the class name
     * @return the class
     */
    public Class loadClass(final byte[] data, final int length, final String name) {
        LOGGER.log(Level.FINE, "Defining class: {0}!", name);
        Class clazz = defineClass(name, data, 0, length);
        if (clazz != null) {
            LOGGER.log(Level.FINE, "Resolving class: {0}!", name);
            resolveClass(clazz);
        }
        LOGGER.log(Level.FINE, "The class: {0} is ready!", name);
        return clazz;
    }

    /**
     * Converts class name to class file path
     *
     * @param name the class name
     * @return the class
     */
    public static String toFilePath(final String name) {
        return Creator.CLASS_OUTPUT_FOLDER + "/" + name.replaceAll("\\.", "/") + ".class";
    }

    /**
     * Reads data from the given input stream
     *
     * @param inputStream the input stream
     * @return the bytes of the data
     * @throws java.io.IOException if the input stream could not be read
     */
    private byte[] readData(final InputStream inputStream)
            throws IOException {
        int last_read = inputStream.read(m_buffer, 0, BYTE_BUF_SIZE);
        //If we read leas than allowed then there will be no data
        if (last_read < BYTE_BUF_SIZE) {
            //Everything fits in a buffer so return it
            m_read = last_read;
            return m_buffer;
        } else {
            if (last_read > 0) {
                //Re-set the variables
                m_bo_temp.reset();
                m_read = 0;
                //There is more data to load, use the byte stream
                do {
                    m_bo_temp.write(m_buffer, 0, last_read);
                    m_read += last_read;
                    last_read = inputStream.read(m_buffer, 0, BYTE_BUF_SIZE);
                } while (last_read > 0);
                return m_bo_temp.toByteArray();
            } else {
                throw new IOException("Empty class byte stream!");
            }
        }
    }
}
