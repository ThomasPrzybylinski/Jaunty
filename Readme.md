# JAUNTY

This code was created for the calculation of variable symmetries for clausal propositional statements. It is based off of the work of NAUTY and SAUCY, which are concerned with large graph isomorphisms. JAUNTY, on the other hand is focused on rapidly finding many local symmetries.

This code was built to support my dissertation, which can be found at:
https://etd.library.emory.edu/concern/etds/zs25x906m?locale=en

## Extra Libraries
Some functionality may require that graphiz executables are in your PATH.

This was created in Eclipse, so I recommend building/running it from there.

## Places to start
src/WorkflowTests/FullTest.java will calculate symmetries/similarity from a Cartesian product of the required edge manipulators (a similarity measure), optional manipulation (e.g. ensuring triangle inequality by taking shortest paths), finding sets via eclectic set cover creators, and model creators. It will create a folder called FullTests in the project directory with results, with .html files for easy perusal.

/SATStore/src/task/symmetry/RealSymFinder.java is what is typically used to find local symmetries

## Notes
This code wasn't built for release in mind, and so is poorly documented and has some annoying issues (e.g. variables are 1-indexed, but clauses are 0-indexed). There's a good chance there's some random functionality that's not working.

