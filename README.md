# Computation Service

This project is just an example of how to use some Akka libraries in practice. It is based on the [Distributed Worker Template](http://www.lightbend.com/activator/template/akka-distributed-workers-java).

- Akka Java 8 Actors and FSM
- Akka (Remote) Cluster
- Akka HTTP
- MongoDB
- Guava Graph / Network
- ANTLR

## How it works
First you have to create a [Structure](src/main/java/de/stphngrtz/computation/model/Structure.java), which consists of a graph of [Elements](src/main/java/de/stphngrtz/computation/model/Element.java). Each [Element](src/main/java/de/stphngrtz/computation/model/Element.java) takes a set of [Definitions](src/main/java/de/stphngrtz/computation/model/Definition.java), which are similar to variables.
Then you create a [Computation](src/main/java/de/stphngrtz/computation/model/Computation.java) with an expression in reference to a [Structure](src/main/java/de/stphngrtz/computation/model/Structure.java) and an [Element](src/main/java/de/stphngrtz/computation/model/Element.java).
Let's have a look at the following example.

### Structure
```
                A1
             v      v
          B1          B3
       v      v          v
    C1          C2         C3
```

### Definitions
```
A1: x=1
B1: y=10
B2: y=20
C1: z=100
C2: z=200
C3: z=300
```

### Computation
```
Element: B1
Expression: 25 + z - y
Result: 25 + (100+200) - 10 = 315
```

As soon as the workers have processed the result, the [Computation](src/main/java/de/stphngrtz/computation/model/Computation.java) will be updated.