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
import nl.tudelft.dcsc.sr2jlib.grammar.GrammarProvider;

/**
 * Represents a functional expression of any type
 *
 * @author Dr. Ivan S. Zapreev
 */
public class FunctExpr extends Expression {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(FunctExpr.class.getName());

    private static final String VAR_NAME_PREF_STR = "x";
    private static final String MATH_PREFIX_REG = "\\$";
    private static final String MATH_PREFIX_STR = "Math.";
    private static final String WHITE_SPACE_REG = "\\s+";
    private static final String SIGN_ARG_DELIM_REG = ",";
    private static final char OPEN_FUNC_CHAR = '[';
    private static final char CLOSE_FUNC_CHAR = ']';
    private static final char OPEN_SIGN_CHAR = '(';
    private static final char CLOSE_SIGN_CHAR = ')';
    private static final int NODE_SIZE_1 = 1;

    //Stores the grammar provider
    private final GrammarProvider m_provider;
    //Stores the function 
    private final String m_func;
    //Stores the operation signature types
    private final String m_sign;
    //Stores the list of children, once materialized
    private List<Expression> m_child;
    //Stores the array of sgnature tokens
    private final String[] m_arg_types;
    //Stores the number of argument occurences in the function
    private final int[] m_arg_occ;
    //Stores the minimum node size
    private int m_min_size;
    //Stores the maximum node size
    private int m_max_size;
    //Stors the flag indicating if the node is terminal
    private boolean m_is_terminal;
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
        this.m_is_terminal = false;
        this.m_node_size = 0;

        if (m_sign.isEmpty()) {
            throw new IllegalArgumentException("The argument list of '" + desc
                    + "' can not be ampty!");
        }

        this.m_child = new ArrayList();
        this.m_arg_types = m_sign.split(SIGN_ARG_DELIM_REG);
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
        return func.replaceAll(MATH_PREFIX_REG, MATH_PREFIX_STR);
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
        this.m_child = new ArrayList();
        //Iterate over children and clone them
        other.m_child.forEach((child) -> {
            this.m_child.add(child.duplicate());
        });
        this.m_arg_types = other.m_arg_types;
        this.m_min_size = other.m_min_size;
        this.m_max_size = other.m_max_size;
        this.m_arg_occ = other.m_arg_occ;
        this.m_is_terminal = other.m_is_terminal;
        this.m_unb_cnt = other.m_unb_cnt;
        this.m_fin_sum_max = other.m_fin_sum_max;
        this.m_fin_max = other.m_fin_max;
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
        m_child.forEach((child) -> {
            child.get_nodes(term, nterm);
        });
    }

    @Override
    public boolean replace_node(Expression from, Expression to) {
        for (int idx = 0; idx < m_child.size(); ++idx) {
            Expression child = m_child.get(idx);
            LOGGER.log(Level.FINE, "Considering child node {0}", child);
            if (child == from) {
                LOGGER.log(Level.FINE, "The node is found!", child);
                m_child.set(idx, to);
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
        this.m_child = donor.m_child;
        donor.m_child = null;
    }

    @Override
    public boolean emplace_funct(Expression from, Expression to) {
        for (int idx = 0; idx < m_child.size(); ++idx) {
            Expression child = m_child.get(idx);
            LOGGER.log(Level.FINE, "Considering child node {0}", child);
            if (child == from) {
                LOGGER.log(Level.FINE, "The node is found!", child);
                m_child.set(idx, to);
                //Copy the children
                ((FunctExpr) to).m_child = ((FunctExpr) from).m_child;
                ((FunctExpr) from).m_child = null;
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
            m_child.forEach((child) -> {
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
    public Expression duplicate() {
        return new FunctExpr(this);
    }

    //Stores the number of unbounded size arguments
    private int m_unb_cnt = 0;
    //Stores the sum of maximum argument sizes, capping the 
    //unbounded ones with the maimum value of the bounded
    private int m_fin_sum_max = 0;
    //Stores the maximum bounded maximum argument size
    private int m_fin_max = 0;
    //Stores the retrieved arrays of min/max argument sizes per argument
    private int[][] m_mm_sizes = null;

    @Override
    public void pre_process_arg_sizes() {
        if (m_mm_sizes == null) {
            final int num_args = m_arg_types.length;
            m_mm_sizes = new int[num_args][];
            for (int arg_idx = 0; arg_idx < num_args; ++arg_idx) {
                m_mm_sizes[arg_idx] = m_provider.get_min_max_size(m_arg_types[arg_idx]);
                //Count the number of unbounded arguments
                if (m_mm_sizes[arg_idx][1] == Integer.MAX_VALUE) {
                    m_unb_cnt += 1;
                } else {
                    m_fin_sum_max += m_mm_sizes[arg_idx][1];
                    m_fin_max = Math.max(m_fin_max, m_mm_sizes[arg_idx][1]);
                }
            }
            m_fin_sum_max += m_unb_cnt * m_fin_max;
        }
    }

    /**
     * Distribute the given size between the arguments in a fair way
     *
     * @param rem_size the size to distribute
     * @return the array of sizes per argument
     */
    private int[] distribute_argument_size(int rem_size) {
        final int num_args = m_arg_types.length;
        int[] a_sizes = new int[num_args];
        //Check if we can use maximum sizes for all arguments
        if (rem_size < m_fin_sum_max) {
            //If we can not then give each finite argment the maximum fair share
            while (rem_size > 0) {
                int p_rem_size = rem_size;
                for (int arg_idx = 0; arg_idx < num_args; ++arg_idx) {
                    if ((a_sizes[arg_idx] + m_mm_sizes[arg_idx][0]) < m_mm_sizes[arg_idx][1]) {
                        a_sizes[arg_idx] += 1;
                        --rem_size;
                        if (rem_size <= 0) {
                            break;
                        }
                    }
                }
                //We will not be able to use all of the points, all arguments are full
                if (p_rem_size == rem_size) {
                    break;
                }
            }
        } else {
            //Each argument can get maximum size, the unbounded
            //args get an equal share of the remaining size
            final int rest = rem_size - m_fin_sum_max;
            final int unb_extra = ((m_unb_cnt != 0) ? (int) Math.ceil(rest / m_unb_cnt) : 0);
            for (int arg_idx = 0; arg_idx < num_args; ++arg_idx) {
                if (m_mm_sizes[arg_idx][1] == Integer.MAX_VALUE) {
                    a_sizes[arg_idx] = m_fin_max + unb_extra;
                } else {
                    a_sizes[arg_idx] = m_mm_sizes[arg_idx][1];
                }
            }
        }
        return a_sizes;
    }

    @Override
    public void materialize(int rem_size) {
        //Define the number of arguments constant
        final int num_args = m_arg_types.length;

        //If somehow we do not have enough, please be generous, this is a soft constriant
        if (rem_size < m_min_size) {
            rem_size = m_min_size;
        }
        rem_size -= NODE_SIZE_1;

        //Distribute argument size
        final int[] a_sizes = distribute_argument_size(rem_size);

        //Materialize children according to sizes
        for (int arg_rem = num_args; arg_rem > 0; arg_rem--) {
            //Get the argument index
            final int arg_idx = num_args - arg_rem;
            //Get the argument type
            final String arg_type = m_arg_types[arg_idx];
            //Chose the size of expression for this argument
            final int arg_size = a_sizes[arg_idx];
            //Generate an expression
            final Expression exp = m_provider.choose_expr(arg_type, arg_size);
            LOGGER.log(Level.FINE, "Materializing expression {0} for size {1}",
                    new Object[]{exp, arg_size});
            //Materialize the expression with the chosen size
            exp.materialize(arg_size);
            //Add the expression into the list of children
            m_child.add(exp);
        }
        //Define if the node is terminal
        m_is_terminal = (is_placement() && (m_child.get(0).is_terminal()));
    }

    /**
     * Allows to get the children nodes of this expression node if the node is
     * materialized.
     *
     * @return the children nodes of this expression
     */
    public List<Expression> get_children() {
        return m_child;
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

    /**
     * Allows to get a signature of the expression
     *
     * @return the expression's signature
     * @throws UnsupportedOperationException in case the method is nod supported
     */
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
        String func = m_func;
        int idx = 1;
        for (Expression child : m_child) {
            final String child_str = child.serialize();
            final String var_str = VAR_NAME_PREF_STR + idx;
            func = func.replaceAll(bb(var_str), bb(child_str));
            func = func.replaceAll(bc(var_str), bc(child_str));
            func = func.replaceAll(cc(var_str), cc(child_str));
            func = func.replaceAll(cb(var_str), cb(child_str));
            if (child.is_terminal()) {
                func = func.replaceAll(var_str, child_str);
            } else {
                func = func.replaceAll(var_str, bb(child_str));
            }
            ++idx;
        }
        return func;
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
    public boolean is_terminal() {
        return m_is_terminal;
    }

    @Override
    public boolean is_placement() {
        //DO NOT MAKE PLACEMENTS from terminal types!
        //This will cause biased individuals when materializing.
        //As the node size [-x1](D) or [1/x1](D) will be 2 and [x](D) will be one!
        return m_func.equals(VAR_NAME_PREF_STR + 1)
                && !m_sign.equals(VarExpr.ENTRY_VAR_STR)
                && !m_sign.equals(BConstExpr.ENTRY_CBOOL_STR)
                && !m_sign.equals(NConstExpr.ENTRY_CNUM_STR);
    }
}
