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
package nl.tudelft.dcsc.sr2jlib.grammar;

import nl.tudelft.dcsc.sr2jlib.grammar.expr.Expression;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.VarExpr;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.BConstExpr;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.NConstExpr;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.FunctExpr;
import nl.tudelft.dcsc.sr2jlib.grammar.expr.TermExpr;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the GP grammar storing class
 *
 * @author Dr. Ivan S. Zapreev
 */
public class Grammar implements GrammarProvider {

    //Stores the reference to the logger
    private static final Logger LOGGER = Logger.getLogger(Grammar.class.getName());

    private static final String ENTRY_DELIM_REG = "\n";
    private static final String COMMENT_STR = "//";
    private static final String COMMENT_REG = "//.*$";

    private static final String ENTRY_BOOL_STR = "B";
    private static final String ENTRY_NUM_STR = "R";

    //Stores the manager to dof index to grammar mappints
    private static final Map<Integer, Map<Integer, Grammar>> m_mgr_grammars = new HashMap();
    private static int max_mgr_id = 0;
    private static int max_dof_id = 0;
    private static Grammar[][] m_grammars = null;

    /**
     * Allows to get the current grammar instance
     *
     * @param mgr_id the process manager id
     * @param dof_idx the dimension index within the given manager
     * @return the current grammar instance or null if none
     */
    public static Grammar inst(final int mgr_id, final int dof_idx) {
        return m_grammars[mgr_id][dof_idx];
    }

    /**
     * Allows to register the given grammar to be used for the given manager and
     * its given dimension index.
     *
     * @param mgr_id the manager id
     * @param dof_id the manager's dimension index
     * @param grammar the grammar
     */
    public static void register_grammar(final int mgr_id,
            final int dof_id, final Grammar grammar) {
        //Get the current dof to grammar mapping for the manager
        Map<Integer, Grammar> dof_grammars = m_mgr_grammars.get(mgr_id);
        if (dof_grammars == null) {
            dof_grammars = new HashMap();
            m_mgr_grammars.put(mgr_id, dof_grammars);
        }
        dof_grammars.put(dof_id, grammar);

        max_mgr_id = Math.max(max_mgr_id, mgr_id);
        max_dof_id = Math.max(max_dof_id, dof_id);
    }

    /**
     * Must be called after all the grammars are registered
     */
    public static void prepare_grammars() {
        m_grammars = new Grammar[max_mgr_id + 1][max_dof_id + 1];
        for (Entry<Integer, Map<Integer, Grammar>> mgr_entry : m_mgr_grammars.entrySet()) {
            for (Entry<Integer, Grammar> dof_entry : mgr_entry.getValue().entrySet()) {
                m_grammars[mgr_entry.getKey()][dof_entry.getKey()] = dof_entry.getValue();
            }
        }
    }

    /**
     * Must be called after all the before new set of grammars gets registered
     */
    public static void clear_grammars() {
        m_mgr_grammars.clear();
        max_mgr_id = 0;
        max_dof_id = 0;
        m_grammars = null;
    }

    /**
     * Lets one to instantiate a new grammar object. This grammar is yet to be
     * registered for concrete manager id and dof id it is possible to use the
     * same grammar for multiple managers and dofs.
     *
     * @param cfg the configuration object
     * @return the prepared grammar object.
     *
     * @throws IllegalArgumentException in case improper argument values
     */
    public static Grammar create_grammar(final GrammarConfig cfg)
            throws IllegalArgumentException {
        Grammar grammar = new Grammar(cfg);

        //Parse the grammar
        grammar.parse_grammar(cfg.m_grammar, cfg.m_is_prop_pnodes);

        //Check the grammar
        grammar.check_grammar();

        return grammar;
    }

    //Stores the maximum allowed tree size
    private final int m_max_ts;
    //Stores the number of variables
    private final int m_num_vars;
    //Stores the change versus replace ratio
    private final double m_ch_vs_rep;
    private final double m_min_node_grow;
    private final double m_max_node_grow;
    //Stores the maximum grammar depth for fixed point iterations
    private final int m_max_gd;
    //terminal versus non terminal mutation ratio
    private final double m_tm_vs_ntm;
    //Stores the gramma entries
    private final Map<String, GrammarEntry> m_entries;
    //The minimum tree size bound
    private int m_min_tree_size;
    //The tree size bound
    private int m_tree_size_bound;

    /**
     * The basic constructor
     *
     * @param cfg
     * @throws IllegalArgumentException in case improper argument values
     */
    private Grammar(final GrammarConfig cfg) {
        //Initialize parameters
        this.m_max_ts = cfg.m_max_ts;
        this.m_num_vars = cfg.m_num_vars;
        this.m_ch_vs_rep = cfg.m_ch_vs_rep;

        if (cfg.m_min_node_grow > cfg.m_max_node_grow) {
            throw new IllegalArgumentException("Minimum node grow factor must be "
                    + "smaller or equal to the maximum node grow factor!");
        }
        this.m_min_node_grow = cfg.m_min_node_grow;
        this.m_max_node_grow = cfg.m_max_node_grow;
        this.m_max_gd = cfg.m_max_gd;
        this.m_tm_vs_ntm = cfg.m_tm_vs_ntm;
        this.m_entries = new HashMap();
        this.m_min_tree_size = 0;
        this.m_tree_size_bound = 0;
    }

    /**
     * Allows to obtain the number of variables used within this grammar
     *
     * @return the number of variables used within this grammar
     */
    public int get_num_vars() {
        return m_num_vars;
    }

    /**
     * Allows to exclude lines which are comments and remove comments from lines
     *
     * @param line the line to work with
     * @return the result
     */
    private String pre_process_line(String line) {
        line = line.trim();
        if (line.startsWith(COMMENT_STR)) {
            return null;
        } else {
            return line.replaceFirst(COMMENT_REG, "").trim();
        }
    }

    /**
     * Prepares entries mapping
     *
     * @param grammar the entries maps
     */
    private void prepare_entries_map(final String grammar) {
        //Process the grammar line per line
        List lines = Arrays.asList(grammar.split(ENTRY_DELIM_REG));
        ListIterator<String> iter = lines.listIterator();
        while (iter.hasNext()) {
            //Remove comments and empty lines
            String line = pre_process_line(iter.next());
            if ((line != null) && !line.isEmpty()) {
                FunctGrammarEntry entry = new FunctGrammarEntry(this, line);
                if (m_entries.put(entry.get_expr_type(), entry) != null) {
                    throw new IllegalArgumentException(
                            "Multiple grammar entries for: " + entry.get_expr_type());
                }
            }
        }
    }

    /**
     * Initializes default entry map elements, for the build-in terminal nodes
     */
    private void initialize_default_entries() {
        m_entries.put(NConstExpr.ENTRY_CNUM_STR,
                new DefGrammarEntry(NConstExpr.ENTRY_CNUM_STR, new NConstExpr(ENTRY_NUM_STR)));
        m_entries.put(BConstExpr.ENTRY_CBOOL_STR,
                new DefGrammarEntry(BConstExpr.ENTRY_CBOOL_STR, new BConstExpr(ENTRY_BOOL_STR)));
        m_entries.put(VarExpr.ENTRY_VAR_STR,
                new DefGrammarEntry(VarExpr.ENTRY_VAR_STR, new VarExpr(ENTRY_NUM_STR, m_num_vars)));
    }

    /**
     * Compute the maximum node and entry sizes
     */
    private void compute_max_entry_size() {
        m_entries.values().forEach((expr) -> {
            expr.compute_max_size();
        });
    }

    /**
     * Pre-process the entry's argument sizes
     */
    private void pre_process_arg_sizes() {
        m_entries.values().forEach((expr) -> {
            expr.pre_process_arg_sizes();
        });
    }

    /**
     * Compute the minimum node and entry sizes
     */
    private void compute_min_entry_size() {
        int iter_cnt = 0;
        while (iter_cnt < m_max_gd) {
            String failed_entries = "";
            boolean is_fp_global = true;
            for (GrammarEntry entry : m_entries.values()) {
                final boolean is_fp_local = entry.compute_min_size();
                if (!is_fp_local && (iter_cnt + 1 == m_max_gd)) {
                    failed_entries += entry.get_expr_type() + ", ";
                }
                is_fp_global &= is_fp_local;
            }
            //Stop if the fixed point is reached for all grammar entries
            if (is_fp_global) {
                break;
            } else {
                iter_cnt++;
                if (!failed_entries.isEmpty()) {
                    throw new IllegalArgumentException("The recursion of entries: '"
                            + failed_entries + "' does not terminate, missing terminals!");
                }
            }
        }
    }

    /**
     * Initialize randomizer maps in the entries
     */
    private void initialize_randomizers() {
        m_entries.forEach((type_name, entry) -> {
            LOGGER.log(Level.FINE, "preparing randomizers for type {0}", type_name);
            entry.prepare_randomizers();
        });
    }

    /**
     * Propagate placement nodes to improve mutations
     */
    private void propagate_placement_nodes() {
        boolean has_plcs;
        do {
            has_plcs = false;
            for (Map.Entry<String, GrammarEntry> entry : m_entries.entrySet()) {
                LOGGER.log(Level.FINE, "Propagating placements for type {0}", entry.getKey());
                has_plcs = has_plcs | entry.getValue().propagate_placement_nodes(m_entries);
                LOGGER.log(Level.FINE, "Has placements result {0}", has_plcs);
            }
        } while (has_plcs);
    }

    /**
     * Parses the grammar
     *
     * @param grammar the grammar to parse
     * @param is_prop_pnodes propagate placement nodes flag
     */
    private void parse_grammar(final String grammar, final boolean is_prop_pnodes) {
        //Parse the grammar into entries
        prepare_entries_map(grammar);

        //Initialize the default entries
        initialize_default_entries();

        //Compute the maximum node sizes
        compute_max_entry_size();

        //Compute the minimum node sizes
        compute_min_entry_size();

        //Pre-process argument sizes
        pre_process_arg_sizes();

        //Propagate placement nodes
        if (is_prop_pnodes) {
            propagate_placement_nodes();
        }

        //Initialize in the randomiser maps
        initialize_randomizers();

        //Compute the tree size bound
        m_min_tree_size = m_entries.get(ENTRY_NUM_STR).get_min_size();
        m_tree_size_bound = Math.max(m_min_tree_size, m_max_ts) + 1;
    }

    @Override
    public int compute_max_size(final String arg_type) {
        GrammarEntry entry = m_entries.get(arg_type);
        if (entry != null) {
            return entry.compute_max_size();
        } else {
            throw new IllegalArgumentException("Missing grammar entry: " + arg_type);
        }
    }

    @Override
    public int get_min_size(final String arg_type) {
        GrammarEntry entry = m_entries.get(arg_type);
        if (entry != null) {
            return entry.get_min_size();
        } else {
            throw new IllegalArgumentException("Missing grammar entry: " + arg_type);
        }
    }

    @Override
    public int[] get_min_max_size(final String arg_type) {
        GrammarEntry entry = m_entries.get(arg_type);
        if (entry != null) {
            return entry.get_min_max_size();
        } else {
            throw new IllegalArgumentException("Missing grammar entry: " + arg_type);
        }
    }

    @Override
    public Expression choose_expr(final String exp_type, final int max_size) {
        LOGGER.log(Level.FINE, "Requesting a new expression type {0}, size {1}",
                new Object[]{exp_type, max_size});
        return m_entries.get(exp_type).choose_expr(max_size);
    }

    /**
     * Do some checks on the grammar
     */
    private void check_grammar() {
        String msg = null;

        //Check ENTRY_NUM_STR is present
        if (!m_entries.containsKey(ENTRY_NUM_STR)) {
            msg = "The compulsory root node: " + ENTRY_NUM_STR + " is not present!";
        }

        if (msg != null) {
            LOGGER.log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Mutates the given mutant
     *
     * @param to_mutate_node the node of the mutant to change with a node of the
     * same signature
     * @param mutant the mutant to mutate
     * @return the mutated version of the mutant
     */
    private Expression change(final Expression to_mutate_node, Expression mutant) {
        if (to_mutate_node instanceof FunctExpr) {
            //Get the expression's signature
            final String signature = to_mutate_node.get_signature();
            //Get an expression with the same signature
            LOGGER.log(Level.FINE, "Exchanging node of type: {0}, signature {1}",
                    new Object[]{to_mutate_node.get_expr_type(), signature});
            final Expression exchange_node
                    = m_entries.get(to_mutate_node.get_expr_type()).choose_expr(signature);
            if (to_mutate_node.is_equal_funct(exchange_node)) {
                //It can happen that we chose the same node king,
                //then fall back into node replacement
                mutant = replace(to_mutate_node, mutant);
            } else {
                LOGGER.log(Level.FINE, "Changing: {0} into {1}",
                        new Object[]{to_mutate_node, exchange_node});
                //Emplace the new node in place of the old one, the root node is special
                if (mutant == to_mutate_node) {
                    LOGGER.log(Level.FINE, "Emplacing the root node");
                    ((FunctExpr) exchange_node).move_children((FunctExpr) mutant);
                    mutant = exchange_node;
                } else {
                    LOGGER.log(Level.FINE, "Searching for the child node to emplace");
                    //We replace some node in a tree
                    mutant.emplace_funct(to_mutate_node, exchange_node);
                }
            }
        } else {
            //If we are here then it is a Variable or a Numeric/boolean constant
            //So the change then boils down to a simple node re-materialization.
            to_mutate_node.materialize(1);
        }

        return mutant;
    }

    /**
     * Computes the new node size
     *
     * @param to_mutate_node the node of the mutant to replace with a new tree
     * @param mutant the mutant to mutate
     * @return the new to mutate node size
     */
    private int compute_new_node_size(final Expression to_mutate_node, Expression mutant) {
        //Get new sub-tree size and random generate a new node
        final int tree_size = mutant.get_size();
        //Do not re-comute size as to_mutate_node is from mutant
        final int old_node_size = to_mutate_node.get_size(false);
        final int min_size = Math.max(1,
                (int) Math.floor(old_node_size * m_min_node_grow));
        final int max_size = Math.max(min_size,
                (int) Math.ceil(old_node_size * m_max_node_grow));
        final int grow_range = Math.max(0, max_size - min_size) + 1;
        final int rem_size = m_max_ts - (tree_size - old_node_size);
        final int new_node_size = Math.min(rem_size,
                min_size + ThreadLocalRandom.current().nextInt(grow_range));
        LOGGER.log(Level.FINE, "The old node size: {0}, the new node size: {1},"
                + " the new size range is [{2}, {3})", new Object[]{old_node_size,
                    new_node_size, min_size, min_size + grow_range});
        return new_node_size;
    }

    /**
     * Mutates the given mutant
     *
     * @param to_mutate_node the node of the mutant to replace with a new tree
     * @param mutant the mutant to mutate
     * @return the mutated version of the mutant
     */
    private Expression replace(final Expression to_mutate_node, Expression mutant) {
        if (to_mutate_node instanceof TermExpr) {
            //If this is a terminal node type then just mutate  it and do 
            //not replace with a functional otherwise we can break the
            //actual type of the functional where this terminal is used
            mutant = change(to_mutate_node, mutant);
        } else {
            final int new_node_size = compute_new_node_size(to_mutate_node, mutant);
            LOGGER.log(Level.FINE, "Replacing node of type: {0}", to_mutate_node.get_expr_type());
            final Expression exchange_node
                    = m_entries.get(to_mutate_node.get_expr_type()).choose_expr(new_node_size);
            LOGGER.log(Level.FINE, "The old/new nodes are: {0}/{1}",
                    new Object[]{to_mutate_node, exchange_node});
            exchange_node.materialize(new_node_size);
            LOGGER.log(Level.FINE, "The the new node {0} has actual size: {1}",
                    new Object[]{exchange_node.serialize(), exchange_node.get_size()});
            //Set the new node in place of the old one, the root node is special
            if (mutant == to_mutate_node) {
                LOGGER.log(Level.FINE, "Replacing the root node");
                //We change the entire tree
                mutant = exchange_node;
            } else {
                LOGGER.log(Level.FINE, "Searching for the child node to replace");
                //We replace some node in a tree
                mutant.replace_node(to_mutate_node, exchange_node);
            }
        }
        return mutant;
    }

    /**
     * Picks up a random node from the expression
     *
     * @param mutant the mutant to choose a node from
     * @return the chosen node
     */
    private Expression pick_up_node(final Expression mutant) {
        final List<Expression> nterm = new ArrayList();
        final List<Expression> term = new ArrayList();
        mutant.get_nodes(nterm, term);
        //Give a certain chance for terminan v.s. non-temrinal nodes
        if (ThreadLocalRandom.current().nextFloat() < m_tm_vs_ntm) {
            final int idx = ThreadLocalRandom.current().nextInt(term.size());
            return (Expression) term.get(idx);
        } else {
            final int idx = ThreadLocalRandom.current().nextInt(nterm.size());
            return (Expression) nterm.get(idx);
        }
    }

    /**
     * Mutate the given expression into a new one the old expression stays
     * intact
     *
     * @param expr the original expression
     * @return the mutated version of the original expression
     */
    public Expression mutate(final Expression expr) {
        Expression mutant = expr.duplicate();

        LOGGER.log(Level.FINE, "Mutating size: {0} expression: {1}",
                new Object[]{mutant.get_size(), mutant.serialize()});

        //Pick up a node at random
        final Expression to_mutate_node = pick_up_node(mutant);

        LOGGER.log(Level.FINE, "Got node to mutate: {0}", to_mutate_node.serialize());

        //Choose change or replace
        boolean is_change = (ThreadLocalRandom.current().nextDouble() < m_ch_vs_rep);

        LOGGER.log(Level.FINE, "Is change : {0}", is_change);
        if (is_change) {
            mutant = change(to_mutate_node, mutant);
        } else {
            mutant = replace(to_mutate_node, mutant);
        }

        LOGGER.log(Level.FINE, "Obtained size: {0} expression: {1}",
                new Object[]{mutant.get_size(), mutant.serialize()});
        return mutant;
    }

    /**
     * Allows to generate a random expression
     *
     * @return a random expression
     */
    public Expression generate_numeric() {
        //Generate the maximum tree size to be used
        int max_size = ThreadLocalRandom.current().nextInt(m_min_tree_size, m_tree_size_bound);
        //Get the first numeric node
        Expression result = choose_expr(ENTRY_NUM_STR, max_size);
        //Populate the rest recursively
        result.materialize(max_size);
        LOGGER.log(Level.FINE, "Requested candidate of size {0}, obtained size {1}",
                new Object[]{max_size, result.get_size()});
        //Return the complete result
        return result;
    }
}
