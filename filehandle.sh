#!/bin/bash
cd ~
cd /Users/swarnenduchakraborty/study/dissertation/
cat score.txt story_text_tf_idf.txt >> merged_score.txt
awk '{print $1,$2,$5}' merged_score.txt >> merged_tfidf.txt
sort -t " " -nk1 -r -nk3 merged_tfidf.txt >> merged_tfidf_sort.txt
