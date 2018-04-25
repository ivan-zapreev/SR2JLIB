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

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * This class is partially inherited from
 * http://www.beyondlinux.com/2011/07/20/3-steps-to-dynamically-compile-instantiate-and-run-a-java-class/
 * It is used to compile an individual class for performing its fitness check
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class Creator {

    /**
     * The name of the array argument used by the individual dof functions
     */
    public static final String VAR_NAME = "args";

    /**
     * Allows to get the variable name for the variable with the given index.
     *
     * @param idx the variable index name
     * @return the variable name
     */
    public static final String get_var_name(final int idx) {
        return Creator.VAR_NAME + "\\[" + idx + "\\]";
    }

    /**
     * The name of the function for getting the individual's vector function
     * dimensionality
     */
    public static final String GET_NUM_DOFS = "get_num_dofs";

    /**
     * The prefix of the single dof function of the individual
     */
    public static final String EVALUATE = "evaluate_";

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(Creator.class.getName());

    /**
     * The name of the package for the Individual classes
     */
    public static final String PACKAGE_NAME = Creator.class.getPackage().getName() + ".ind";

    /**
     * The folder into which the compiled individual classes will be placed
     */
    public static final String CLASS_OUTPUT_FOLDER = "./target/classes";

    /**
     * The diagnostic listener class to store information about the compilation
     * process.
     */
    public static class MyDiagnosticListener implements DiagnosticListener<JavaFileObject> {

        /**
         * Stores the received message or null if none
         */
        public String message = null;

        @Override
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            message = "Line Number->" + diagnostic.getLineNumber()
                    + ", code->" + diagnostic.getCode() + ", Message->"
                    + diagnostic.getMessage(Locale.ENGLISH) + ", Source->"
                    + diagnostic.getSource();
            LOGGER.log(Level.SEVERE, message);
        }
    }

    /**
     * java File Object represents an in-memory java source file <br>
     * so there is no need to put the source file on hard disk *
     */
    public static class InMemoryJavaFileObject extends SimpleJavaFileObject {

        private final String m_class_name;
        private final String m_contents;

        /**
         * The basic constructor
         *
         * @param class_name the class name
         * @param contents the class content
         */
        public InMemoryJavaFileObject(String class_name, String contents) {
            super(URI.create("string:///" + class_name.replace('.', '/')
                    + Kind.SOURCE.extension), Kind.SOURCE);
            this.m_class_name = class_name;
            this.m_contents = contents;
        }

        /**
         * Allows to retrieve the class name.
         *
         * @return the class name
         */
        public String get_class_name() {
            return m_class_name;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors)
                throws IOException {
            return m_contents;
        }
    }

    private static InMemoryJavaFileObject getJavaFileObject(final String class_name,
            final String full_name, final String[] funct) {
        String contents = "package " + PACKAGE_NAME + ";"
                + "public class " + class_name + " { "
                + "  public static int " + GET_NUM_DOFS + "(){"
                + "    return " + funct.length + ";"
                + "}";
        for (int idx = 0; idx < funct.length; ++idx) {
            contents += "public static double " + EVALUATE + idx + "( double[] " + VAR_NAME + ") {"
                    + "    return " + funct[idx] + "; "
                    + "  } ";
        }
        contents += "} ";
        LOGGER.log(Level.FINE, "{0}: {1}", new Object[]{full_name, contents});
        return new InMemoryJavaFileObject(full_name, contents);
    }

    private static final JavaCompiler JAVAC = ToolProvider.getSystemJavaCompiler();

    /**
     * Compile the individual
     *
     * @throws IllegalArgumentException
     */
    private static void compile(Iterable<InMemoryJavaFileObject> files) throws IllegalArgumentException {

        // for compilation diagnostic message processing on compilation WARNING/ERROR
        MyDiagnosticListener diag = new MyDiagnosticListener();
        StandardJavaFileManager fileManager
                = JAVAC.getStandardFileManager(diag, Locale.ENGLISH, null);

        //specify classes output folder
        //Note: "-XDuseUnsharedTable" is a workarround for a java bug,
        //without it the compiler blows up with so many classes!
        Iterable options = Arrays.asList("-XDuseUnsharedTable",
                "-d", CLASS_OUTPUT_FOLDER);
        JavaCompiler.CompilationTask task = JAVAC.getTask(null, fileManager,
                diag, options, null, files);

        if (!task.call()) {
            final String class_name = files.iterator().next().get_class_name();
            throw new IllegalArgumentException("Failed compiling an Individual"
                    + class_name + ", msg: " + diag.message);
        }
    }

    /**
     * Allows to get a full class name given the individual's uid
     *
     * @param uid the individual's uid
     * @return the full class name
     */
    public static String get_class_name(final long uid) {
        final String class_name = "Individual" + uid;
        final String full_name = PACKAGE_NAME + "." + class_name;
        return full_name.replaceAll("\\.", "/");
    }

    /**
     * Allows to construct a person class name from the given function and
     * person id. The class is compiled and the class name is returned for
     * further use.
     *
     * @param uid the person's class uid
     * @param function the vector function description of the person
     * @return the prepared class name
     * @throws IllegalArgumentException if the individual is failed to compile
     */
    public static String prepare(final long uid, final String[] function) {
        final String class_name = "Individual" + uid;
        final String full_name = PACKAGE_NAME + "." + class_name;

        //Get the file object
        InMemoryJavaFileObject file = getJavaFileObject(class_name, full_name, function);
        //Call the compiler
        compile(Arrays.asList(file));

        return full_name.replaceAll("\\.", "/");
    }
}
