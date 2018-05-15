# **Symbolic Regression 2 Java Library**

**Author:** [Dr. Ivan S. Zapreev](https://nl.linkedin.com/in/zapreevis)

**Project pages:** [Git-Hub-Project](https://github.com/ivan-zapreev/SR2JLIB)

## **Introduction**

**SR2JLIB** is a Netbeans project supplying a Java library for grammar-guided *Symbolic Regression* (**SR**) [Koza93], powered by *Genetic Programming* (**GP**) [Koz94, WHM+97], and more specifically *Grammar-Guided Genetic Programming* (**GGGP**) [GCA+08].

The approach realized in this library differs from standard GP in that the population is placed on a 2D grid. The latter defines a habitat for the individuals which are reproducing in parallel and thus there is no notion of staging. The reproduction process is ensured by multiple parallel threads randomly choosing individuals from the current population and allowing them to reproduce trying to settle the offsprings in the pre-defined neighborhood of their ancestors. Each time a new individual is created it is attempted to be placed in the neighboring cells based on tournament or probabilistic tournament selection. 

It is important to note that one can also limit the life-time of an individual. This is done by defining the minimum and maximum number of reproductions. The actual number of reproductions is then chosen based on the individual's fitness but within the pre-specified bounds. 

The realized GP procedure allows to fit (multi-dimensional) vector functions to data. SR can be done for vector function sub-components simultaneously or in parallel. The functions allowed by the library are arbitrary Java numeric expressions and allow using conditional expressions of the form:

```
 <boolean expression> ? <numeric expression> : <numeric expression>
```
 
Moreover, numeric expressions allow for using any of the java.lang.Math class function primitives. Our strong believe is that cross over operation when breeding function mostly makes little sense. Therefore, the only genetic operations supported are mutations which can be of the next two types:
 
 1. Mutating an existing expression into a completely new one of the same type;
 2. Mutating the given function into another function with the same arity and with the same arguments;

Another feature of the library is that it allows for *Just In Time* compilation (**JIT**) of the individuals in a form java classes and dynamic loading thereof into **JVM**. The latter is done seamlessly for the library's user and allows one to compute individual's fitness from Java code or using *Java Native Interface* (**JNI**) in order to compute fitness from, e.g., C or C++ code.

Last but not least, is that the grammar provided for building expressions is probabilistic, each grammatical expression can be given a weight that defines its likelihood to be taken when choosing between expressions of the same type.

## **Using software**

Using software requires Netbeans IDE 8.2 or later and JDK v1.8 or later.
Sample projects using the library are provided by:

1. <https://github.com/ivan-zapreev/SR2JLIB_EX>
2. <https://github.com/ivan-zapreev/SCOTS2SR>

The former is a Java-only project giving a basic example of how the library can be used, it contains multiple comments and is self-explanatory. The latter is a more involved **JNI** based project for symbolic fitting of SCOTSv2.0 <https://gitlab.lrz.de/matthias/SCOTSv0.2> BDD controllers [Run16]. We also suggest taking a thorough look at the Java Doc of the library stored in the `./api/` folder of the project.

It is a known issue with the Java 8 **JIT** compiler that its caches can become full thus preventing further individuals' compilation and potentially preventing the library to function properly. Therefore, in case a JIT compilation is to be used, see the section on library interfaces further in the text, we suggest supplying java with the following command line arguments:

```
-XX:InitialCodeCacheSize=1024m
-XX:ReservedCodeCacheSize=2048m 
-XX:+UseCodeCacheFlushing
```
These are to be provided as `java` command line parameters of the application using **SR2JLIB**. If needed, the cache sizes can be increased even further.

## **Main concepts**

This section explains how the library can be used by specifying:

1. The way the grammars are to be specified;
2. The library interfaces to be used;
3. The observers and listeners to be implemented;

### Grammar

The `grammar` is needed to define valid numeric and boolean expressions to be used as vector function components. It is defined as a set of new-line-separated grammar entries:

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

Note that, `L`, `V`, and `D` are build-in and R with B are to be specified by the user. Boolean expressions (`B`) only require specification if are implicitly or explicitly used to define `R`. Specifying `R` is compulsory, as any genetically breed function will be of type `R`.

Each `expression` is defined by: : *(i)* a function (some numeric/boolean expression); *(ii)* its argument types; *(iii)* an optional `weight` - a positive double defining the probability distribution over expressions of the same type (if omitted the default value is 1.0):

```
	<expression> := [<function>](<types>) |
	                [<function>](<types>)@<weight>
```

Here `function` is a valid java expression (boolean or numerical depending on the context) which uses parameters named `x` followed by indexes starting with `1` (i.e. x1, x2, x3). Function parameters define function arguments and their types are given by the `types` entry in the definition above. The latter is a comma-separated list:

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

### Main Interfaces

**SR2JLIB** allows to run multiple SR processes in parallel on separate grids. Each of such processes is encapsulated inside an instance of a `nl.tudelft.dcsc.sr2jlib.ProcessManager` class. A single process manager can breed multi-dimensional vector functions. Each of the vector function dimension functions is defined by some grammar. Such a grammar can be individual per dimension or shared with some other dimension within the same or between different instances of `ProcessManager`. A grammar is defined by an instance of the `nl.tudelft.dcsc.sr2jlib.grammar.Grammar` class. Each instance thereof is to be supplied with a configuration object of type `nl.tudelft.dcsc.sr2jlib.grammar.GrammarConfig`. A general pattern for instantiating and setting up grammars is given below by means of an example:

```java
//Clear any previously registered grammars
Grammar.clear_grammars();

//Register dof 0 grammar for process manager index 0
final GrammarConfig g_cfg00 = new GrammarConfig(...);
final Grammar grammar00 = Grammar.create_grammar(g_cfg00);
Grammar.register_grammar(0, 0, grammar00);

//Register dof 1 grammar for process manager index 0
final GrammarConfig g_cfg01 = new GrammarConfig(...);
final Grammar grammar01 = Grammar.create_grammar(g_cfg01);
Grammar.register_grammar(0, 1, grammar01);

//Register dof 0 grammar for process manager index 1
//It has the same grammar as dof 1 of process manager 0
Grammar.register_grammar(1, 0, grammar01);

//Post-process the registered grammars preparing for work
Grammar.prepare_grammars();
```

Here `grammar00` and `grammar01` are used to breed two-dimensional vector functions in an instance of a `ProcessManager` with a unique identifier 0. In addition, `grammar01` is used to breed single-dimensional vector functions in an instance of a `ProcessManager` with a unique identifier 1.

Once the grammars are instantiated and registered one is to instantiate process managers. These are to be configured with instances of the `nl.tudelft.dcsc.sr2jlib.ProcessManagerConfig` class. Further, each process manager can be individually started - to begin its symbolic regression; and stopped - to stop the genetic breeding process.

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

As one can see, all of the `Grammar` and `ProcessManager` class parameters are encapsulated in the corresponding configuration objects. The latter, including the suggested and default values will be discussed in the next section. Also note that, stopping the process manager is typically done once a sufficiently fit individual is found. Doing that, will be discussed in the subsequent section on listeners, observers and computers.

### Configuration objects

Let us consider the two configuration objects `GrammarConfig` and `ProcessManagerConfig`.

The `GrammarConfig` is used for grammar configuration and has the following constructor:

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
Here we suggest the following default values:

```java
ch_vs_rep = 0.5;
min_node_grow = 0.8;
max_node_grow = 1.2;
is_prop_pnodes = false;
tm_vs_ntm = 0.5;
```

Choosing the value for `max_ts` depends on a concrete `grammar` in a sense that a grammar using many `Math` class methods will result in hard-to-compute functions which will likely have a negative impact on the performance of fitness computations. In such cases the tree size may be chosen to be smaller than in case of, e.g., a grammar defining polynomials. In general, the tree size defines the number of nodes in the tree. Each expression node adds one to the tree size. 

Similarly, `max_gd` - *maximum grammar depth* (**MGD**) is also `grammar` specific. It should be set to an upper bound of the MGD which is defined as the maximum (over all functions) of minimum (over all function instances) expression sizes. For simplicity, one can simply set `max_gd` to some rather high value to, e.g. 1000, and try setting up the grammar. If the actual MGD turns out to be larger, which is very unlikely, a corresponding error will be reported. Setting the value of `max_gd` ​​​​​​​above the actual MGD does not result in any performance overheads. It is possible however to create a grammars with unbounded MGD, such grammars are not supported as they are not feasible, e.g.:

```
R := [x1+x2](R,R)
```

Clearly, `R` is defined recursively but without explicit or implicit use of any terminal expressions. To fix this grammar and make it bounded one needs to simply add a terminal expression, such as:

```
R := [x1](D); [x1+x2](R,R)
```
The maximum grammar depth in this case will be 3 and as a rule of thumb: *"A grammar will have a bounded MGD if each grammar expression can be instantiated as a finite expression tree"*.


The `ProcessManagerConfig` is used to configure a process manager and has the following constructor:

```java
    /**
     * The basic constructor
     *
     * @param mgr_id the id of the population manager
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
     * @param sel_type the individual's selection type
     * @param is_allow_dying if true then individuals are dying after they had a
     * certain number of children
     * @param min_chld_cnt the minimum number of children before dying
     * @param max_chld_cnt the maximum number of children before dying
     * @param observer the fitness observer instance to monitor the population
     * @param done_cb the call back to be called once this manager has finished
     */
    public ProcessManagerConfig(
            final int mgr_id,
            final double init_pop_mult,
            final int num_workers,
            final long max_num_reps,
            final int num_dofs,
            final int size_x, final int size_y,
            final int ch_sp_x, final int ch_sp_y,
            final SelectionType sel_type,
            final boolean is_allow_dying,
            final int min_chld_cnt,
            final int max_chld_cnt,
            final GridObserver observer, 
            final FinishedCallback done_cb){...}
```
The default suggested values for this configuration class are as follows:

```java
init_pop_mult = 0.1;
num_workers = 20;
max_num_reps = Long.MAX_VALUE;
size_x = 30;
size_y = 30;
ch_sp_x = 1;
ch_sp_y = 1;
is_allow_dying = false;
min_chld_cnt = 0;
max_chld_cnt = 0;
sel_type = SelectionType.VALUE;
```
The value of `mgr_id` depends on the number of managers, we recommend using continuous (in the non-negative integer domain) manager ids starting from 0. The value of `num_dofs` is problem specific.

Last but not least `done_cb` and `observer` provide the call-back objects. The former is just an object realizing a functional interface to be called once the process manager has finished the SR procedure. The latter is a grid observing object allowing to monitor all of the population changes. We shall discuss these and other interfaces in the next section. 

### Listeners, Observers, and Computers

There are several main interfaces that are to be realized by the application using  **SR2JLIB**, the first three of them are:

1. `nl.tudelft.dcsc.sr2jlib.FinishedCallback` - a functional interface to be used by the `ProcessManager` to notify the user that the SR is finished;
2. `nl.tudelft.dcsc.sr2jlib.grid.GridObserver` - an interface allowing to monitor the symbolic regression process, i.e.: its start, stop and adding/killing new/old individuals on the grid;
3. `nl.tudelft.dcsc.sr2jlib.ErrorListener` - an optional error listener to monitor exceptions and errors occurring while SR. For instance, a broken grammar can result in non-compilable individual classes and these exceptions will be reported through an instance of this interface.

Objects of classes implementing `FinishedCallback` and `GridObserver` are provided as arguments for the `ProcessManagerConfig` class constructor. An object implementing `ErrorListener` is to be set explicitly from the code through  calling the:

```java
    /**
     * Allows to set a new instance of the error listener
     *
     * @param new_el the new error listener
     * @return an old error listener
     */
    public ErrorListener set_listener(final ErrorListener new_el){...}
```

method of the `nl.tudelft.dcsc.sr2jlib.err.ErrorManager` class. 

It is important to note that since `GridObserver` provides an interface for monitoring the population manager's individuals, it is a custom to check for individuals' fitness in order to stop the corresponding process manager from within this interface implementation.

Other interfaces, at least one of which needs to be implemented, are used for individual's fitness computations. These are given by the abstract classes located in the `nl.tudelft.dcsc.sr2jlib.fitness` package and require implementing their overloaded:

```java
public abstract Fitness compute_fitness(final int mgr_id, ...);
```

methods. The concrete abstract class to inherit from, and thus the `compute_fitness` method to be implemented, depends on user preferences. We classify them based on what is provided as an argument for the `compute_fitness` method:

1. `FitnessComputerExpression` - the individual's vector function expression trees.
2. `FitnessComputerString` - the individual's vector function serialized as Java expression strings.
3. `FitnessComputerClass` - the individual's vector function compiled into a java class.
4. `FitnessComputerInstance` - the individual's vector function compiled and instantiated as a java object.

In order to set the fitness computer class as the one to be used, it is required using the:

```java
    /**
     * Allows to set the instance of the fitness computer.
     *
     * @param inst the instance to be set
     */
    public static void set_inst(final FitnessComputerExpression inst) {...}
```

method of the `FitnessManager` class.

### Expression trees

Each individual's vector function dimension is represented in a form of a Java numeric expression. The latter is initially stored in a form of a tree where non-terminal nodes correspond to functions (numeric or boolean expressions) and terminal nodes correspond to numerical or boolean constants, or free variables. The classes used to form expression trees are stored in the `nl.tudelft.dcsc.sr2jlib.grammar.expr` package. Each tree node is an instance of the `Expression` class. Non-terminal nodes are instances of the `FunctExpr` class and terminal ones of the `TermExpr` class. The latter has three child classes:

1. `BConstExpr` - a boolean constant expression
2. `NConstExpr` - a numerical constant expression
3. `VarExpr` - a double free variable expression

All of these have interface functions in order to traverse through the expression tree and get information of each of its nodes. For more detail consider reading the Java Doc.

## **Licensing** 

This is a free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful, but **WITHOUT ANY WARRANTY**; without even the implied warranty of **MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE**. See the *GNU General Public License version 3* (**GPL3**) for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

## **Literature**

1. [Koza93] John R. Koza; Martin A. Keane; James P. Rice (1993). "Performance improvement of machine learning via automatic discovery of facilitating functions as applied to a problem of symbolic system identification" (PDF). IEEE International Conference on Neural Networks. San Francisco: IEEE. pp. 191–198.
2. [Koz94] John R. Koza. Genetic programming as a means for programming computers by natural selection. Statistics and Computing, 4(2):87–112, 1994.
3. [WHM+97] M. J. Willis, H. G. Hiden, P. Marenbach, B. McKay, and G. A. Montague. Genetic programming: an introduction and survey of applications. In Second International Conference On Genetic Algorithms In Engineering Systems: Innovations And Applications, pages 314–319, Sep 1997.
4. [GCA+08] Manrique Gamo, Daniel and Ríos Carrión, Juan and Rodríguez-Patón Aradas, Alfonso (2008). Grammar-Guided Genetic Programming. In: "Encyclopedia of Artificial Intelligence". Information Science Reference, EEUU, pp. 767-773.
5. [Run16] Rungger, M. and Zamani, M. (2016). SCOTS: A tool for the synthesis of symbolic controllers. In Proceedings of the 19th International Conference on Hybrid Systems: Computation and Control, HSCC, 99–104.