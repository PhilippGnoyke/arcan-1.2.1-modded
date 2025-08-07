# Arcan 1.2.1 Modded

## About The Project
This a modified version of [Arcan](https://gitlab.com/essere.lab.public/arcan). Note that I am **not** the creator of Arcan.

## Major Modifications
* Added a TD quantification framework adapted from [Roveda](https://boa.unimib.it/handle/10281/199005).
* Changed the definition of cyclic dependencies from "subcycles" to "supercycles". Arcan natively detects simple cyclic paths, while this modification is focused on strongly connected components. Shapes of supercycles are classified according to [Al-Mutawa et al.](https://ieeexplore.ieee.org/abstract/document/6824106)
* Added the calculation of several new metrics.
* Adapted the printed output files to the data required by AsTdEA.
* Modified data flow to enable multi-threaded usage and to increase efficiency by reducing repeated graph traversals.
* Major optimization: Reduced runtime complexity from a quadratic to a linear trend in relation to the input size (classes, packages, dependency edges)

## Installation
This modification is intended to be used along with [AsTdEA](https://github.com/PhilippGnoyke/AsTdEA) and thus included in its installation.

## Usage
This modification is controlled by AsTdEA and does not require direct human input. See the [readme of AsTdEA](https://github.com/PhilippGnoyke/AsTdEA/blob/master/README.md) for detailed instructions and a breakdown of all metrics.
