parallel --joblog ./log.txt --max-proc 5 \
	java -cp "GeneratePotentialList/weka.jar:GeneratePotentialList/src/" \
	MainCVPA {2} {1} ./data/ {3} 0.5 '>' ./runtimeInfo/output_{3}_{1}_{2}_CVPA.txt ::: 8 6 4 ::: 1 5 10 12 18 ::: 1
#    ./run.sh {1} '>' runtimeInfo/{1}.txt ::: 1 5 10 12 18
