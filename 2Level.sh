#!/bin/bash
benchmark=$1
boundingBox=1
splitLevel=6 # modify accordingly
scalingFactor=0.3 # 1.0 if splitLevel=8, 0.3 if splitLevel=6, 0.1 if splitLevel=4.

echo "Level 1 Testing"
java -cp "./GeneratePotentialList/src:./GeneratePotentialList/weka.jar" MainSoftImp2Lvl ${benchmark} ${splitLevel} ./data/ ${boundingBox} 1 0 0.5

echo "Level 2 Data Preproc"
for i in 1 5 10 12 18
do
	if [ ${i} -ne ${benchmark} ]
	then
		java -cp "./DataPreprocessing/src" MainData2Lvl ${i} ${splitLevel} ${scalingFactor} ./data/ ${boundingBox} 0 1 ${benchmark}
	fi
done

echo "Level 2 Testing"
java -cp "./GeneratePotentialList/src:./GeneratePotentialList/weka.jar" MainSoftImp2Lvl ${benchmark} ${splitLevel} ./data/ ${boundingBox} 0 1 0.5
