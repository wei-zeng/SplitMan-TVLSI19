for i in 1 5 10 12 18
do
    # the input to Main class is:
    # 1. benchmark index;
    # 2. layer;
    # 3. bounding box scaling ratio;
    # 4. path to the folder that holds everything;
    # 5. if bounding box is enabled or not (for both positive and negative instances);
    # 6. if generate testing instances
    # generate arff files for no bounding box cases
#    for j in 4 6 8
#    do
#        java -cp ./DataPreprocessing/src/ Main $i $j 1 ./data/ 0 0
#    done
    # generate arff files for bounding box cases
	# java -cp ./DataPreprocessing/src/ Main $i 8 1 ./data/ 1 0
	# java -cp ./DataPreprocessing/src/ Main $i 6 0.3 ./data/ 1 0
	# java -cp ./DataPreprocessing/src/ Main $i 4 0.1 ./data/ 1 0
	# generate arff files for cross-validation-based proximity attack
    java -cp ./DataPreprocessing/src/ MainCVPA $i 4 0.1 ./data/ 1 5 0
    java -cp ./DataPreprocessing/src/ MainCVPA $i 6 0.3 ./data/ 1 5 0
    java -cp ./DataPreprocessing/src/ MainCVPA $i 8 1 ./data/ 1 5 0
done
