#Introduction

**SR2JLIB** is a Netbeans project supplying a Java library for *Grammar-based Symbolic Regression* (**SR**), powered by *Genetic Programming* (**GP**). 

The approach realized in this library differs from standard GP in that the population is placed on a 2D grid. The latter defines habitat for the individuals which are reproducing in parallel and thus there is no notion of staging. The reproduction process is ensured by multiple parallel threads randomly choosing individuals from the current population and allowing them to reproduce trying to settle in the pre-defined neighborhood of their ancestor. Each time a new individual is created it is attempted to be placed in the neighboring cells based on tournament or probabilistic tournament selection. 

It is important to note that one can also limit the life-time of an individual. This is done by defining the minimum and maximum number of reproductions, which are eventually influenced by individual's fitness. 

The realized GP procedure allows to fit (multi-dimensional) vector functions to data. SR can be done for vector function sub-components simultaneously or in parallel. The functions allowed by the library are arbitrary Java numeric expressions and allow using conditional operator:

```
 <boolean expression> ? <numeric expression> : <numeric expression>
```
 
Moreover, numeric expressions allow for using any of the `java.lang.Math` class function primitives. Our strong believe is that cross over operation when breeding function mostly makes little sense. Therefore the only genetic operations supported are mutations which can be of two types:
 
 1. Mutating an existing expression into a complete new expression
 2. Mutating the given function into another function with the same arity and keeping the function arguments as is.

Another feature of the library is that it allows for *Just In Time* compilation (**JIT**) of the individuals in a form java classes and dynamic loading thereof into **JVM**. The latter allows one to easily compute fitness from Java code using the library or use *Java Native Interface* (**JNI**) in order to compute fitness from, e.g., C or C++ code.

Last but bit least is that the grammar provided for building expressions is probabilistic, each grammatical expression can be given a weight that defines its likelihood to be chosen when choosing expressions of the given type at random.

#Using software

Using software requires Netbeans IDE 8.2 or later and JDK v1.8 or later.
Example projects of using the library are provided by:

1. <https://github.com/ivan-zapreev/SR2JLIB_EX>
2. <https://github.com/ivan-zapreev/SCOTS2SR>

The former is a Java-only project giving a basic example of how the library can be used, it contains multiple comments and is self-explanatory. The latter is a more involved JNI based project for symbolic fitting of SCOTSv2.0 <https://gitlab.lrz.de/matthias/SCOTSv0.2> BDD controllers. We also suggest taking a thorough look at the Java Doc of the library stores in the `./api/` folder of the project.

It is a known issue with the the java8 JIT compiler that its caches can become full thus preventing further individuals compilation and thus proper functioning of the library. Therefore, in case a JIT compilation is to be used, see the section on library interfaces further in the text, we suggest supplying java with the following command line arguments:

```
-XX:InitialCodeCacheSize=1024m
-XX:ReservedCodeCacheSize=2048m 
-XX:+UseCodeCacheFlushing
```
These are to be provided to java in the application, which uses **SR2JLIB**. If needed, the cache sizes can be increased even further.

#Main concepts
This section introduces the main concepts of the library.

##Grammar
The `grammar` is defined as a set of new-line-separated grammar entries:

```
<grammar> := <entry> | <grammar> \n <entry>
```

Each grammar `entry` defines an expression `type` and consists of the type name and one or more semicolon-separated expressions of this type:

```
	<entry> := (<type> := <expression>) | <entry> ; <expression>
```
Each `type` is a string and there are several-predefined (and thus reserved) types available by default:

```
R - real expression
B - boolean expression
L - logical constant
V - variable
D - double constant
```

Note that, `L`, `V`, and `D` are build in and `R` with `B` are to be specified by the user. The latter (`B`) only requires specification if boolean expressions are used. Specifying `R` is compulsory as any genetically breed function will be of type `R`.

Each `expression` is defined by a function, its argument types and an optional `weight` - a positive double defining the probability distribution over expressions of the same type (if omitted the default value is 1.0):

```
	<expression> := [<function>](<types>) |
	                [<function>](<types>)@<weight>
```

Here `function` is a valid java expression (boolean or numerical depending on the context) which uses parameters named `x` followed by indexes starting with `1` (i.e. x1, x2, x3). Function parameters define function arguments and their types are given by `types` entry in the definition above. The latter is a comma-separated list:

```
	<types> := <type> | <types>, <type>
```

where the number of elements in the list must corresponds to the number of distinct `function` parameters. Moreover, the parameter index matches the corresponding `type` position in the list. 

Any `function` may use the `java.lang.Math` class by referencing it with the `$` sign. For instance `$sin(x1)` will be interpreted as `Math.sin(x1)`. Consider the following valid expression examples:

```
[x1 - $floor(x1)](D)
[$sin(x1)*x2](V,D)
[$sin(x2)*x1](D,V)
```
Note that here, `[$sin(x1)*x2](V,D)` and `[$sin(x2)*x1](D,V)` are equivalent and both result in a variable argument sinus multiplied with a real constant.

The last thing to note is that the textual grammar description allows for comments which should start with `//` and last until the end of the current line. 

For completeness, below we give an example grammar which assumes uniform probability distribution over all expressions except for `[$abs(x1)](R)@0.1` and `x1](L) @0.01` which are given a smaller chance to be used (by setting the weights to 0.1 and 0.01 respectively):

```
R := [x1](D); [x1](V); [$abs(x1)](R)@0.1; [$sin(x1)](R); [$cos(x1)](R); [$sinh(x1)](R); [$cosh(x1)](R); [$tan(x1)](tR); [$tanh(x1)](R); [$acos(x1)](aR); [$asin(x1)](aR); [$sqrt(x1)](npR); [$cbrt(x1)](R); [$ceil(x1)](R); [$floor(x1)](R); [$log(x1)](lR); [$log10(x1)](lR); [$max(x1,x2)](R,R) ; [$min(x1,x2)](R, R) ; [$pow(x1,x2)](R,pI); [$signum(x1)](R); [-x1](R); [x1/x2](R,nR); [x1*x2](R,R); [x1+x2](R,R); [x1-x2](R,R) ; [x1 ? x2 : x3](B,R,R)

B := [x1](L) @0.01; [x1!=x2](R,R); [x1==x2](R,R); [x1<x2](R,R); [x1<=x2](R,R); [x1>x2](R,R); [x1>=x2](R,R); [!x1](B) ; [x1&&x2](B,B); [x1||x2](B,B)

//User defined by demand:
aR :=  [x1 - $floor(x1)](R) //domain for acos/asin
lR := [(x1<1.0e-4)?1.0e-4:x1](pR) //domain for log/log10
nR := [(x1==0.0)?1.0e-10:x1](R)  //non-zero real
pR := [$abs(x1)](nR)  //positive real
npR := [$abs(x1)](R)  //non-negative real
pI := [$floor(1.0/x1)](nR) //positive integer
tR := [x1 - $floor(x1/$PI)*$PI](R) //domain for tangent

//Form: [function](arguments)@weight
//"$" - is replaced with "Math."
//Arguments are comma separated
//The variable arguments for the java code
//are "args[idx]" where idx begins with 0
//Standard argument types:
//R - real expression
//B - boolean expression
//L - logical constant
//V - variable
//D - double constant
```

##Main Interfaces

**SR2JLIB** allows to run multiple SR processes in parallel on separate grids. Each of such processes is encapsulated inside an instance of a `nl.tudelft.dcsc.sr2jlib.ProcessManager` class. A single process manager can breed multi-dimensional vector functions. Each of the vector function dimension functions is defined by some grammar. Such a grammar can be individual per dimension or shared with some other dimension within the same or between different instance of `ProcessManager`. A grammar is defined by an instance of the `nl.tudelft.dcsc.sr2jlib.grammar.Grammar` class. Each instance thereof is to be supplied with a configuration object of type `nl.tudelft.dcsc.sr2jlib.grammar.GrammarConfig`. A general pattern for instantiating and setting up grammars is given below:

```java
//Clear any previously registered grammars
Grammar.clear_grammars();

//Register dof 0 grammar for process managed index 0
final GrammarConfig g_cfg00 = new GrammarConfig(...);
final Grammar grammar00 = Grammar.create_grammar(g_cfg00);
Grammar.register_grammar(0, 0, grammar00);

//Register dof 1 grammar for process managed index 0
final GrammarConfig g_cfg01 = new GrammarConfig(...);
final Grammar grammar01 = Grammar.create_grammar(g_cfg01);
Grammar.register_grammar(0, 1, grammar01);

//Register dof 0 grammar for process managed index 1
Grammar.register_grammar(1, 0, grammar00);

//Post-process the registered grammars preparing for work
Grammar.prepare_grammars();
```

Here `grammar00` and `grammar01` are used to breed two-dimensional vector functions in an instance of a `ProcessManager` with a unique identifier 0. In addition, `grammar00` is used to breed single-dimensional vector functions in an instance of a `ProcessManager` with a unique identifier 1.

Once the grammars are created and registered one is to instantiate process managers, which are to be configured with instances of the `nl.tudelft.dcsc.sr2jlib.ProcessManagerConfig` class. Then each process manager can be individually started - to begin with symbolic regression; and stopped - to stop the genetic breeding process.

```java
//Create the configuration object
final ProcessManagerConfig config = new ProcessManagerConfig(...)
//Instantiate the process manager with the given configuration
final ProcessManager manager = new ProcessManager(config);
//Start the Symbolic regression
manager.start();
//Stop the manager softly, waiting for all worker threads to finish
manager.stop(true);
```

As one can see all of the `Grammar` and `ProcessManager` class parameters are encapsulated in the corresponding configuration objects. The latter, including the suggested and default values will be discussed in the next section.

##Configuration objects
Let us consider the two configuration objects `GrammarConfig` and `ProcessManagerConfig`.

The `GrammarConfig` is used to configure a grammar and has the following constructor:

```java
    /**
     * The basic constructor
     *
     * @param grammar the grammar's textual description
     * @param max_ts the maximum allowed generated expression tree size
     * @param ch_vs_rep the change versus replace ratio from the range [0,1]
     * @param num_vars the number of variables to be used in this grammar
     * @param min_node_grow the minimum node grow coefficient, a positive double
     * @param max_node_grow the maximum node grow coefficient, a positive double
     * @param is_prop_pnodes true if placement nodes are to be propagated
     * @param max_gd the maximum grammar depth for fixed point iteration
     * @param tm_vs_ntm terminal versus non terminal mutation ratio from the
     * range [0,1]
     */
    public GrammarConfig(final String grammar,
            final int max_ts, final double ch_vs_rep,
            final int num_vars, final double min_node_grow,
            final double max_node_grow, final boolean is_prop_pnodes,
            final int max_gd, final double tm_vs_ntm) {...}
```
Here we suggest the following default values

```java
ch_vs_rep = 0.5;
min_node_grow = 0.8;
max_node_grow = 1.2;
is_prop_pnodes = false;
tm_vs_ntm = 0.5;
```
Choosing the value for `max_ts` depends on `grammar` in a sense that a grammar using many `Math` class methods will result in hard-to-compute functions which will impact the performance of fitness computation. In such cases the tree size may be chosen to be smaller than in case of, e.g., a grammar defining polynomials. In general the tree size defines the number of nodes in the tree. Each expression node adds one to the tree size. 

Similarly, `max_gd` (maximum grammar depth) also depends on `grammar`. It should be set to an upper bound of the grammar depth which is defined as the maximum minimum size for each instantiated function in the grammar. In general one can simply set this value to, e.g. 1000 and try setting up the grammar. If the grammar depth turns out to be larger, which is unlikely, then an error will be reported. It is possible however to create a grammars with unbounded maximum grammar depth, such grammars are not supported as they are not feasible, e.g.:

```
R := [x1+x2](R,R)
```

To fix this grammar one can simply a terminal expression such as:

```
R := [x1](D); [x1+x2](R,R)
```
The maximum grammar depth in this case will be 3.


The `ProcessManagerConfig` is used to configure a process manager and has the following constructor:

```java
    /**
     * The basic constructor
     *
     * @param done_cb the call back to be called once this manager has finished
     * @param observer the fitness observer instance to monitor the population
     * @param init_pop_mult the initial population coefficient relative to the
     * number of grid cells, from (0.0,1.0]
     * @param num_workers the number of worker threads for this manager, each
     * thread works on reproducing individuals
     * @param max_num_reps the maximum number of reproductions, defined the
     * run-time of the symbolic regression on the grid
     * @param num_dofs the number of dimensions for the individual's vector
     * function
     * @param size_x the number of the population grid cells in x
     * @param size_y the number of the population grid cells in y
     * @param ch_sp_x the number of positions from the parent in x the children
     * will be spread
     * @param ch_sp_y the number of positions from the parent in y the children
     * will be spread
     * @param mgr_id the id of the population manager
     * @param sel_type the individual's selection type
     * @param is_allow_dying if true then individuals are dying after they had a
     * certain number of children
     * @param min_chld_cnt the minimum number of children before dying
     * @param max_chld_cnt the maximum number of children before dying
     */
    public ProcessManagerConfig(
            final FinishedCallback done_cb,
            final GridObserver observer, 
            final double init_pop_mult,
            final int num_workers,
            final long max_num_reps,
            final int num_dofs,
            final int size_x, final int size_y,
            final int ch_sp_x, final int ch_sp_y,
            final int mgr_id,
            final SelectionType sel_type,
            final boolean is_allow_dying,
            final int min_chld_cnt,
            final int max_chld_cnt){...}
```
The default suggested values for this configuration class are as follows:

```java
init_pop_mult = 0.1;
num_workers = 20;
max_num_reps = Long.MAX_VAL;
size_x = 30;
size_y = 30;
ch_sp_x = 1;
ch_sp_y = 1;
is_allow_dying = false;
min_chld_cnt = 0;
max_chld_cnt = 0;
sel_type = SelectionType.VALUE;
```
The value of `mgr_id` depends on the number of managers, we recommend using manager ids starting from 0. The value of `num_dofs` is problem specific.

Last but not least `done_cb` and `observer` provide the call-back objects. The former is just an object realizing a functional interface to be called once the process manager has finished the SR procedure. The latter is a grid observing object allowing to monitor all of the population changes. We shall discuss these and other interfaces in the next section. 

##Listeners and Observers


##Expression trees

#Licensing 

This is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful, but **WITHOUT ANY WARRANTY**; without even the implied warranty of **MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE**. See the *GNU General Public License version 3* (**GPL3**) for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

#Literature
1. [SCOTSv2.0]
2. [GP]
3. [SR]
4. [G-SP]