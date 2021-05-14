# RAMP: Replica-Aware Management Protocols
This repository provides an implementation of the modelling and analysis framework given by replica-aware management protocols, which is presented in
> Soldani J, Cameriero M, Paparelli G, Brogi A. _Modelling and Analysing Replica- and Fault-aware Management of Horizontally Scalable Applications_, Submitted for Publication

## About RAMP
RAMP is a Maven project, whose sources are organized in two main parts:
* [core](https://github.com/di-unipi-socc/ramp/tree/master/src/main/java/unipi/di/socc/ramp/core), including an object model for representing application specifications in Java and an analyzer for checking the validity of existing management plans.
* [cli](https://github.com/di-unipi-socc/ramp/tree/master/src/main/java/unipi/di/socc/ramp/cli), providing the main module to run analyses and a set of parsing classes to provide the analyzer with the necessary inputs. 
* [test](https://github.com/di-unipi-socc/ramp/tree/master/src/test/java/unipi/di/socc/ramp), providing all necessary classes to unit test the code.

## Using RAMP
After cloning the repository on your computer, compile the project by issuing
``` 
mvn clean install 
```
in the project's folder (requires Maven installed). A runnable jar is placed in the `target` folder, which you can copy in the main project's folder by issuing
``` 
cp target/ramp-1.jar ramp.jar
```
RAMP can then be launched to analyse the validity of plan for an application in a given global state by issuing
``` 
java -jar ramp.jar appSpec [globalState] plan planType validity  
```
where
* `appSpec` is a JSON file containing the specification of the target application,
* `globalState` is an (optional) JSON file containing the specification of the global state where the plan is to be executed (if not specified, the starting "empty" global state is considered),
* `plan` is a JSON file specifying the plan (workflow or sequence) to be analysed,
* `planType` is either `--plan` or `--sequence` to distinguish whether the input `plan` is a workflow plan or a sequential plan, respectively, and
* `validity` is either `--valid` or `--weakly-valid` to distinguish whether the validity or weak validity of the input `plan` is to be verified.

Examples of `appSpec` and `globalState` are given by [thinking.json](https://github.com/di-unipi-socc/ramp/blob/master/data/thinking-app/thinking.json) and [running-globalstate.json](https://github.com/di-unipi-socc/ramp/blob/master/data/thinking-app/running-globalstate.json), whilst examples of plans and guidelines on how to analyse them can be found in the [thinking-app](https://github.com/di-unipi-socc/ramp/tree/master/data/thinking-app) folder.
