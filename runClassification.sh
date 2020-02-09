# the input to MainSoftImp class is:
# 1. benchmark index;
# 2. layer;
# 3. path to the folder that holds everything;
# 4. if bounding box is enabled or not (for both positive and negative instances);
# 5. threshold of classification
# 6. if write LoC files to data/(BB_)PotentialListFiles/
parallel --joblog ./log.txt --max-proc 5 \
	java -cp "GeneratePotentialList/weka.jar:GeneratePotentialList/src/" \
	MainSoftImp {2} {1} ./data/ {3} 0.5 1 '>' ./runtimeInfo/output_{3}_{1}_{2}.txt ::: 8 6 4 ::: 1 5 10 12 18 ::: 1
