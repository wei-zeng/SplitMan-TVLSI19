parallel --joblog ./log.txt --max-proc 5 \
	./run.sh {1} '>' ./runtimeInfo/output_2lvl_{1}.txt ::: 1 5 10 12 18
