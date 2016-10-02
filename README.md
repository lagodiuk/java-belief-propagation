# java-belief-propagation

Repository contains the derivation of [Belief Propagation algorithm](https://en.wikipedia.org/wiki/Belief_propagation) from the ground up, as well as generic Java implementation of the Belief Propagation algorithm.


![Equations of Belief Propagation algorithm](https://raw.githubusercontent.com/lagodiuk/java-belief-propagation/master/img/belief_propagation.png)

PDF with detailed derivation of the algorithm for Pairwise Markov Random Fields (at the moment this is still in a draft version, and any comments are welcome): 
https://github.com/lagodiuk/java-belief-propagation/blob/master/derivation_of_belief_propagation_algorithm/Derivation.pdf

Examples of the inference over the different Pairwise Markov Random Fields can be found in folder: *src/test/java/com/lahodiuk/bp/example/*

## Reconstruction of the images ##
![Scheme of the reconstruction using Pairwise Markov Random Field and Belief Propagation](https://raw.githubusercontent.com/lagodiuk/java-belief-propagation/master/img/image_reconstruction_scheme.png)

For more details check the class: https://github.com/lagodiuk/java-belief-propagation/blob/master/src/main/java/com/lahodiuk/bp/example/ImageReconstruction.java

![Example of reconstruction 2](https://raw.githubusercontent.com/lagodiuk/java-belief-propagation/master/img/image_reconstruction_2.gif)

![Example of reconstruction 1](https://raw.githubusercontent.com/lagodiuk/java-belief-propagation/master/img/image_reconstruction_1.gif)

### How to run an example of the image reconstruction ###

Compile the library:
```bash
$ mvn clean install
```
Run an example:
```bash
$ java -cp "target/bp-1.0-SNAPSHOT.jar:target/lib/*" com.lahodiuk.bp.example.ImageReconstruction
```
