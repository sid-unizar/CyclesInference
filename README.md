# Cycles method

This project computes the cycle-based method to obtain translation pairs between two languages in order to get indirect translations. Results are printed in a ".tsv" file as:
>source_label	target_label	part_of_speech	score
		
This method has participate in the [TIAD task](http://tiad2020.unizar.es/) (Translation inference across dictionaries) in 2020 in combination with the OTIC method, showing good results in comparison other the participants [1].

This technique was proposed by [2] in 2006. The idea was exploting the properties of the Apertium RDF Graph, by using cycles to identify potential targets that may be a translation of a given word
