for i in 1 5 10 12 18;
do
    for j in 4 6 8;
    do java -cp <path to weka jar file>/weka.jar weka.attributeSelection.CorrelationAttributeEval \
            -i <path to training arff files>/superblue${i}_${j}.arff \
            -s "weka.attributeSelection.Ranker" \
            > <path to the root folder>/FeatureRanking/feature_ranking_Correlation_${i}_${j}.txt;
        java -cp <path to weka jar file>/weka.jar weka.attributeSelection.InfoGainAttributeEval \
            -i <path to training arff files>/superblue${i}_${j}.arff \
            -s "weka.attributeSelection.Ranker" \
            > <path to the root folder>/FeatureRanking/feature_ranking_InfoGain_${i}_${j}.txt;
    done;
done

sed -i '1,11d; 21,23d' *Info*
sed -i '1,10d; 20,22d' *Correlation*

matlab -nodisplay -nosplash -nodesktop -r "plotFeatureRanking; exit"

rm *.txt
