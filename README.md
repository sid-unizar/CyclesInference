# Cycles method

This project computes the cycle-based method to obtain translation pairs between two languages in order to get indirect translations. Results are printed in a ".tsv" file as:
>source_label	target_label	part_of_speech	score
		
This method has participate in the [TIAD task](http://tiad2020.unizar.es/) (Translation inference across dictionaries) in 2020 in combination with the OTIC method, showing good results in comparison with other participants [1].

This technique was proposed by [2] in 2006. The idea was exploting the properties of the Apertium RDF Graph, by using cycles to identify potential targets that may be a translation of a given word.


## References
[1] Lanau-Coronas, M., & Gracia, J. (2020, May). Graph Exploration and Cross-lingual Word Embeddings for Translation Inference Across Dictionaries. In Proceedings of the 2020 Globalex Workshop on Linked Lexicography (pp. 106-110).

[2] Villegas, M., Melero, M., Bel, N., & Gracia, J. (2016, May). Leveraging RDF graphs for crossing multiple bilingual dictionaries. In Proceedings of the Tenth International Conference on Language Resources and Evaluation (LREC'16) (pp. 868-876).
