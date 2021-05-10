# RAMP: Replica-Aware Management Protocols
This repository provides an implementation of the modelling and analysis framework given by replica-aware management protocols, which is presented in
> Soldani J, Cameriero M, Paparelli G, Brogi A. _Modelling and Analysing Replica- and Fault-aware Management of Horizontally Scalable Applications_, Submitted for Publication

## About RAMP
RAMP is a Maven project, whose sources are organized in two main parts:
* [core](https://github.com/di-unipi-socc/ramp/tree/master/src/main/java/unipi/di/socc/ramp/core), including an object model for representing application specifications in Java and an analyzer for checking the validity of existing management plans-
* [cli](https://github.com/di-unipi-socc/ramp/tree/master/src/main/java/unipi/di/socc/ramp/cli), providing the main module to run analyses and a set of parsing classes to provide the analyzer with the necessary inputs. 
* [test](https://github.com/di-unipi-socc/ramp/tree/master/src/test/java/unipi/di/socc/ramp), providing all necessary classes to unit test the code.

To compile the project, please issue
``` 
mvn clean install 
```

after cloning the repository on your device.

## Using RAMP
TBD
