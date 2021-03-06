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
package nl.tudelft.dcsc.sr2jlib.grammar.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import nl.tudelft.dcsc.sr2jlib.grammar.Grammar;
import nl.tudelft.dcsc.sr2jlib.grammar.GrammarProvider;

/**
 * Represents a functional expression of any type
 *
 * @author <a href="mailto:ivan.zapreev@gmail.com"> Dr. Ivan S. Zapreev </a>
 */
public class FunctExpr extends Expression {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(FunctExpr.class.getName());

    private static final String VAR_NAME_PREF_STR = "x";
    private static final String MATH_SYMBOL_REG = "\\$";
    private static final String MATH_PREFIX_STR = "Math";
    private static final String MATH_DOT_PREFIX_STR = MATH_PREFIX_STR + ".";
    private static final String MATH_DOT_PREFIX_REG = MATH_PREFIX_STR + "\\.";
    private static final String WHITE_SPACE_REG = "\\s+";
    private static final String SIGN_ARG_DELIM_REG = ",";
    private static final char OPEN_FUNC_CHAR = '[';
    private static final char CLOSE_FUNC_CHAR = ']';
    private static final char OPEN_SIGN_CHAR = '(';
    private static final char CLOSE_SIGN_CHAR = ')';
    private static final int NODE_SIZE_1 = 1;
    private static final int FIRST_VAR_IDX = 1;
    private static final String FIRST_VAR_NAM_STR = VAR_NAME_PREF_STR + FIRST_VAR_IDX;

    private static final ScriptEngineManager SCRIPT_EM = new ScriptEngineManager();
    private static final String SCRIPT_ENGINE_NAME = "Nashorn";
    private static final String THREADING_PROPERTY = "THREADING";
    private static final String IMPORT_JAVA_MATH_CLASS_SRT = "var Math = Java.type(\"java.lang.Math\");";
    private static ScriptEngine SCRIPT_ENGINE;

    //Stores the bracket safe expressions argument regular expression
    private static final String BSAFE_EXPR_ARG_REG = "[\\w\\-\\+\\/\\>\\<\\=\\!\\:\\?\\.\\&\\|\\[\\]]*";
    //Stores the bracket safe expressions regular expression
    private static final String BSAFE_EXPR_REG = MATH_DOT_PREFIX_REG + "\\w*\\("
            + BSAFE_EXPR_ARG_REG + "(," + BSAFE_EXPR_ARG_REG + ")*\\)";
    //Stores the pattern for the bracket safe expressions
    private static final Pattern BSAFE_EXPR_PATTERN = Pattern.compile(BSAFE_EXPR_REG);

    /**
     * This interface is used for node representation as a string
     */
    @FunctionalInterface
    private interface ToString {

        /**
         * Allows to convert an expression to some of its string representations
         *
         * @param node the expression node to be converted
         * @return the string representation of the node
         */
        public String to_string(Expression child);
    }

    /**
     * Allows to import java.lang.Math for being used in expressions into the
     * engine.
     *
     * @param script_eng the script engine to be used, may be null
     * @return the script engine if the import was successful, otherwise null
     */
    private static ScriptEngine import_java_math(final ScriptEngine script_eng) {
        if (script_eng != null) {
            try {
                script_eng.eval(IMPORT_JAVA_MATH_CLASS_SRT);
                return script_eng;
            } catch (ScriptException ex) {
                LOGGER.log(Level.WARNING, "Failed importing java Math class in ScriptEngine!", ex);
            }
        }
        return null;
    }

    static {
        final ScriptEngine script_engine = SCRIPT_EM.getEngineByName(SCRIPT_ENGINE_NAME);
        if (script_engine == null) {
            LOGGER.log(Level.WARNING, "The {0} engine is not found!", SCRIPT_ENGINE_NAME);
            SCRIPT_ENGINE = null;
        } else {
            if (script_engine.getFactory().getParameter(THREADING_PROPERTY) == null) {
                LOGGER.log(Level.WARNING, "The {0} engine is not multi-threaded!", SCRIPT_ENGINE_NAME);
                SCRIPT_ENGINE = null;
            } else {
                SCRIPT_ENGINE = import_java_math(script_engine);
            }
        }
    }

    //Stores the grammar provider
    private final GrammarProvider m_provider;
    //Stores the function 
    private final String m_func;
    //Stores the operation signature types
    private final String m_sign;
    //Stores the list of children, once materialized
    private List<Expression> m_children;
    //Stores the array of sgnature tokens
    private final String[] m_arg_types;
    //Stores the number of argument occurences in the function
    private final int[] m_arg_occ;
    //Stores the minimum node size
    private int m_min_size;
    //Stores the maximum node size
    private int m_max_size;
    //Stores the flag inditing if this node is a placement node
    private final boolean m_is_plc;
    //Stores the flag inditing if this node is a basic placement node
    private final boolean m_is_b_plc;
    //Stores the node size;
    private int m_node_size;

    /**
     * The basic constructor
     *
     * @param provider the grammar provider
     * @param expr_type the expression type
     * @param desc the function string description
     */
    public FunctExpr(final GrammarProvider provider, final String expr_type, final String desc) {
        super(expr_type);
        this.m_provider = provider;
        final int fs_idx = desc.indexOf(OPEN_FUNC_CHAR);
        final int fe_idx = desc.lastIndexOf(CLOSE_FUNC_CHAR);
        final int ss_idx = desc.lastIndexOf(OPEN_SIGN_CHAR);
        final int se_idx = desc.lastIndexOf(CLOSE_SIGN_CHAR);
        if ((ss_idx == -1) || (se_idx == -1) || (ss_idx >= se_idx)
                || (fs_idx == -1) || (fe_idx == -1) || (fs_idx >= fe_idx)
                || (fe_idx >= ss_idx)) {
            throw new IllegalArgumentException("Illegal function '" + desc
                    + "' must follow the pattern: [function](signature)");
        }
        this.m_func = add_math(strip(desc.substring(fs_idx + 1, fe_idx)));
        this.m_sign = strip(desc.substring(ss_idx + 1, se_idx));
        this.m_min_size = 0;
        this.m_max_size = 0;
        this.m_node_size = 0;
        this.m_children = new ArrayList();
        //It can happen that there is no arguments for the functional expression,
        //then just make a zero length array to keep things rolling. (ToDo: make a separate type?)
        this.m_arg_types = (m_sign.isEmpty() ? new String[0] : m_sign.split(SIGN_ARG_DELIM_REG));
        this.m_arg_occ = new int[m_arg_types.length];
        for (int idx = 0; idx < m_arg_types.length; ++idx) {
            final String var_str = VAR_NAME_PREF_STR + (idx + 1);
            final Pattern p = Pattern.compile(var_str);
            final Matcher m = p.matcher(m_func);
            this.m_arg_occ[idx] = 0;
            while (m.find()) {
                ++this.m_arg_occ[idx];
            }
            LOGGER.log(Level.FINE, "Function {0} contains {1}: {2} times",
                    new Object[]{m_func, var_str, this.m_arg_occ[idx]});
        }

        LOGGER.log(Level.FINE, "Parses the function: {0} -> [{1}]({2})",
                new Object[]{desc, m_func, m_sign});

        m_is_plc = m_func.equals(FIRST_VAR_NAM_STR);
        //The basic placement node is a placement node for a terminal node
        m_is_b_plc = m_is_plc && TermExpr.is_term_type(m_sign);
    }

    /**
     * Allows to make a materialized binary expression which further is not
     * fully initialized so must not be used in any mutations.
     *
     * @param expr_type the functional expression type
     * @param x1 the first argument
     * @param x1_type the first argument type
     * @param op the operation in between the arguments
     * @param x2 the second argument
     * @param x2_type the second argument type
     * @return
     */
    public static FunctExpr make_binary(final String expr_type,
            final Expression x1, final String x1_type, final String op,
            final Expression x2, final String x2_type) {
        FunctExpr expr = new FunctExpr(null, expr_type,
                "[x1" + op + "x2](" + x1_type + "," + x2_type + ")");
        expr.m_children.add(x1);
        expr.m_children.add(x2);
        return expr;
    }

    @Override
    public int compute_max_size() {
        LOGGER.log(Level.FINE, "Computing maximum node size for: {0}", this);
        for (int idx = 0; idx < m_arg_types.length; ++idx) {
            final int max_size = m_provider.compute_max_size(m_arg_types[idx]);
            LOGGER.log(Level.FINE, "The maximum node size for: {0} is {1}",
                    new Object[]{m_arg_types[idx], max_size});
            if (max_size == Integer.MAX_VALUE) {
                m_max_size = Integer.MAX_VALUE;
            } else {
                if (m_max_size != Integer.MAX_VALUE) {
                    m_max_size += max_size * m_arg_occ[idx];
                }
            }
        }
        //If the max value is no maxed out then add node weight
        if (m_max_size != Integer.MAX_VALUE) {
            m_max_size += NODE_SIZE_1;
        }
        LOGGER.log(Level.FINE, "The maximum node size for: {0} is: {1}",
                new Object[]{this, m_max_size});
        return m_max_size;
    }

    @Override
    public int compute_min_size() {
        LOGGER.log(Level.FINE, "Computing minimum node size for: {0}", this);
        //Store the previous minimum size
        final int old_min_size = m_min_size;
        //Re-set the minimum size
        m_min_size = NODE_SIZE_1;
        for (int idx = 0; idx < m_arg_types.length; ++idx) {
            final int min_size = m_provider.get_min_size(m_arg_types[idx]);
            LOGGER.log(Level.FINE, "The minimum node size for: {0} is {1}",
                    new Object[]{m_arg_types[idx], min_size});
            //Add up argument node minimum sizes
            m_min_size += min_size * m_arg_occ[idx];
        }
        if (old_min_size == m_min_size) {
            LOGGER.log(Level.FINE, "Converged for {0} to minimum node size: {1}",
                    new Object[]{this, m_min_size});
        }
        return m_min_size;
    }

    /**
     * Replaces the mathematics class place holder with the class name
     *
     * @param func the function to do the replacement for
     * @return the resulting string
     */
    private String add_math(final String func) {
        return func.replaceAll(MATH_SYMBOL_REG, MATH_DOT_PREFIX_STR);
    }

    /**
     * Removes the white spaces
     *
     * @param str prepares the string by removing the white spaces
     * @return the string without white spaces
     */
    private String strip(final String str) {
        return str.replaceAll(WHITE_SPACE_REG, "");
    }

    /**
     * The copy constructor
     *
     * @param other the expression to copy from
     */
    protected FunctExpr(final FunctExpr other) {
        super(other);
        this.m_provider = other.m_provider;
        this.m_func = other.m_func;
        this.m_sign = other.m_sign;
        this.m_children = new ArrayList();
        //Iterate over children and clone them
        other.m_children.forEach((child) -> {
            this.m_children.add(child.duplicate());
        });
        this.m_arg_types = other.m_arg_types;
        this.m_min_size = other.m_min_size;
        this.m_max_size = other.m_max_size;
        this.m_arg_occ = other.m_arg_occ;
        this.m_is_plc = other.m_is_plc;
        this.m_is_b_plc = other.m_is_b_plc;
        this.m_mm_sizes = other.m_mm_sizes;
        this.m_node_size = other.m_node_size;
    }

    @Override
    public void get_nodes(final List<Expression> nterm, final List<Expression> term) {
        if (this.is_terminal()) {
            term.add(this);
        } else {
            nterm.add(this);
        }
        m_children.forEach((child) -> {
            child.get_nodes(nterm, term);
        });
    }

    @Override
    public boolean replace_node(Expression from, Expression to) {
        for (int idx = 0; idx < m_children.size(); ++idx) {
            Expression child = m_children.get(idx);
            LOGGER.log(Level.FINE, "Considering child node {0}", child);
            if (child == from) {
                LOGGER.log(Level.FINE, "The node is found!", child);
                m_children.set(idx, to);
                return true;
            } else {
                if (child instanceof FunctExpr) {
                    if (child.replace_node(from, to)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Allows to move children from the argument node to this one
     *
     * @param donor the donor node
     */
    public void move_children(FunctExpr donor) {
        this.m_children = donor.m_children;
        donor.m_children = null;
    }

    @Override
    public boolean emplace_funct(Expression from, Expression to) {
        for (int idx = 0; idx < m_children.size(); ++idx) {
            Expression child = m_children.get(idx);
            LOGGER.log(Level.FINE, "Considering child node {0}", child);
            if (child == from) {
                LOGGER.log(Level.FINE, "The node is found!", child);
                m_children.set(idx, to);
                //Copy the children
                ((FunctExpr) to).m_children = ((FunctExpr) from).m_children;
                ((FunctExpr) from).m_children = null;
                return true;
            } else {
                if (child instanceof FunctExpr) {
                    if (child.emplace_funct(from, to)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int get_size(final boolean is_re_compute) {
        if ((m_node_size == 0) || is_re_compute) {
            //Do not count the size of the placement nodes as they do not change the function
            m_node_size = NODE_SIZE_1;
            m_children.forEach((child) -> {
                m_node_size += child.get_size();
            });
        }
        return m_node_size;
    }

    @Override
    public int get_size() {
        return get_size(true);
    }

    @Override
    public int get_min_size() {
        return m_min_size;
    }

    @Override
    public int get_max_size() {
        return m_max_size;
    }

    @Override
    public boolean is_max_size_inf() {
        return (m_max_size == Integer.MAX_VALUE);
    }

    @Override
    public Expression duplicate() {
        return new FunctExpr(this);
    }

    //Stores the retrieved arrays of min/max argument sizes per argument
    private int[][] m_mm_sizes = null;

    @Override
    public void pre_process_arg_sizes() {
        if (m_mm_sizes == null) {
            final int num_args = m_arg_types.length;
            m_mm_sizes = new int[num_args][];
            for (int arg_idx = 0; arg_idx < num_args; ++arg_idx) {
                m_mm_sizes[arg_idx] = m_provider.get_min_max_size(m_arg_types[arg_idx]);
            }
        }
    }

    /**
     * Distribute the given size between the arguments in a fair way
     *
     * @param rem_size the size to distribute
     * @return the array of sizes per argument
     */
    private int[] distribute_argument_size(int rem_size) {
        //If somehow we do not have enough, please be generous, this is a soft constriant
        if (rem_size < m_min_size) {
            rem_size = m_min_size;
        }
        rem_size -= NODE_SIZE_1;

        final int num_args = m_arg_types.length;
        int[] a_sizes = new int[num_args];

        //First give all the arguments their minimum sizes, taking
        //into account the number of agument occurances
        for (int arg_idx = 0; arg_idx < num_args; ++arg_idx) {
            a_sizes[arg_idx] = m_mm_sizes[arg_idx][0];
            rem_size -= m_mm_sizes[arg_idx][0] * m_arg_occ[arg_idx];
        }

        //Use the remains to fill up the sizes until the maximum
        int p_rem_size = 0;
        while ((rem_size > 0) && (p_rem_size != rem_size)) {
            p_rem_size = rem_size;
            for (int arg_idx = 0; (arg_idx < num_args) && (rem_size > 0); ++arg_idx) {
                if (a_sizes[arg_idx] < m_mm_sizes[arg_idx][1]) {
                    a_sizes[arg_idx] += 1;
                    rem_size -= m_arg_occ[arg_idx];
                }
            }
        }
        return a_sizes;
    }

    @Override
    public void materialize(int rem_size) {
        //Distribute argument size
        final int[] a_sizes = distribute_argument_size(rem_size);

        //Define the number of arguments constant
        final int num_args = m_arg_types.length;

        //Materialize children according to sizes
        for (int arg_rem = num_args; arg_rem > 0; arg_rem--) {
            //Get the argument index
            final int arg_idx = num_args - arg_rem;
            //Get the argument type
            final String arg_type = m_arg_types[arg_idx];
            //Chose the size of expression for this argument
            int arg_size = a_sizes[arg_idx];
            //Choose an expression for the size
            final Expression exp = m_provider.choose_expr(arg_type, arg_size);
            LOGGER.log(Level.FINE, "Materializing expression {0} for size {1}",
                    new Object[]{exp, arg_size});
            //Materialize the expression with the chosen size
            exp.materialize(arg_size);
            //Add the expression into the list of children
            m_children.add(exp);
        }
    }

    /**
     * Allows to get the children nodes of this expression node if the node is
     * materialized.
     *
     * @return the children nodes of this expression
     */
    public List<Expression> get_children() {
        return m_children;
    }

    /**
     * Allows to get the function textual description of the expression node as
     * specified by the grammar.
     *
     * @return the function textual description as specified by the grammar.
     */
    public String get_function() {
        return m_func;
    }

    @Override
    public String get_signature() {
        return m_sign;
    }

    private String bb(final String str) {
        return "\\(" + str + "\\)";
    }

    private String bc(final String str) {
        return "\\(" + str + "\\,";
    }

    private String cc(final String str) {
        return "\\," + str + "\\,";
    }

    private String cb(final String str) {
        return "\\," + str + "\\)";
    }

    @Override
    public String serialize() {
        return to_string(m_func,
                (child) -> {
                    return child.serialize();
                });
    }

    @Override
    public String toString() {
        return OPEN_FUNC_CHAR + m_func + CLOSE_FUNC_CHAR
                + OPEN_SIGN_CHAR + m_sign + CLOSE_SIGN_CHAR;
    }

    @Override
    public boolean is_equal_funct(Expression expr) {
        return (expr instanceof FunctExpr)
                && m_func.equals(((FunctExpr) expr).m_func)
                && m_sign.equals(((FunctExpr) expr).m_sign); //This last check is just for safety
    }

    @Override
    final protected boolean is_terminal() {
        return m_sign.isEmpty()
                || (this.is_placement() && m_children.get(0).is_terminal());
    }

    @Override
    protected boolean is_bsafe_expr() {
        return BSAFE_EXPR_PATTERN.matcher(m_func).matches();
    }

    @Override
    public boolean is_placement() {
        return m_is_plc;
    }

    @Override
    public boolean is_b_placement() {
        return m_is_b_plc;
    }

    /**
     * Allows to convert this expression node to a string with the given
     * converter for child nodes
     *
     * @param conver the converter to be used for child nodes
     * @return the string representation of the given node
     */
    private String to_string(String func, final ToString conver) {
        int idx = FIRST_VAR_IDX;
        for (Expression child : m_children) {
            final String child_str = conver.to_string(child);
            final String var_str = VAR_NAME_PREF_STR + idx;
            func = func.replaceAll(bb(var_str), bb(child_str));
            func = func.replaceAll(bc(var_str), bc(child_str));
            func = func.replaceAll(cc(var_str), cc(child_str));
            func = func.replaceAll(cb(var_str), cb(child_str));
            if (child.is_placement() || child.is_bsafe_expr()) {
                func = func.replaceAll(var_str, child_str);
            } else {
                func = func.replaceAll(var_str, bb(child_str));
            }
            ++idx;
        }
        return func;
    }

    @Override
    public String to_text() {
        return to_string(m_func.replaceAll(MATH_DOT_PREFIX_REG, ""),
                (child) -> {
                    return child.to_text();
                });
    }

    /**
     * Attempts a brute-force optimization of the expression.
     *
     * @param script_enj the script engine to be used
     * @return true if the optimization was successful
     */
    private Expression brute_force_optimize(final ScriptEngine script_enj) {
        Expression result = null;
        //Use the engine to compute the constant value
        try {
            //Get the java expression
            final String java_expr = this.serialize() + ";";
            //Evaluate it via script engine
            final Object value = script_enj.eval(java_expr);
            //Take the result, detect its type and make an expression of
            if (value instanceof Double) {
                result = DConstExpr.make_const(
                        Grammar.NUM_ENTRY_TYPE_STR, (Double) value);
            } else {
                if (value instanceof Float) {
                    result = FConstExpr.make_const(
                            Grammar.NUM_ENTRY_TYPE_STR, (Float) value);
                } else {
                    if (value instanceof Boolean) {
                        result = BConstExpr.make_const(
                                Grammar.BOOL_ENTRY_TYPE_STR, (Boolean) value);
                    } else {
                        LOGGER.log(Level.WARNING,
                                "Unable to detect the {0} object type!", value);
                    }
                }
            }
        } catch (ScriptException ex) {
            LOGGER.log(Level.FINER, "Failed evaluating expression!", ex);
        }

        return result;
    }

    @Override
    public Expression optimize() {
        //Stores the result
        Expression result = this;

        //First of all check if the engine is available
        final ScriptEngine script_enj;
        if (SCRIPT_ENGINE == null) {
            script_enj = import_java_math(SCRIPT_EM.getEngineByName(SCRIPT_ENGINE_NAME));
        } else {
            script_enj = SCRIPT_ENGINE;
        }

        //If not then no optimizations are possible
        if (script_enj != null) {
            //Try the brute force optimization
            final Expression bf_res = brute_force_optimize(script_enj);

            //If the brute force did not work, try optimizing the children
            if (bf_res == null) {
                for (int idx = 0; idx < m_children.size(); ++idx) {
                    final Expression orig_child = m_children.get(idx);
                    final Expression opt_child = orig_child.optimize();
                    if (opt_child != orig_child) {
                        m_children.set(idx, opt_child);
                    }
                }
            } else {
                result = bf_res;
            }
        }

        LOGGER.log(Level.FINE, "Optimized: {0}\n---into---\n{1}",
                new Object[]{this.to_text(), result.to_text()});

        //If this node does not change return it
        return result;
    }
}
