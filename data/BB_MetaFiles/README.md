This folder will contain the following files after running `runClassification.sh` if `boundingBox` is enabled:
- `*.hist.csv`: each line corresponds to a point in the plot of Accuracy vs Size(LoC). Each line has three numbers:
1. size(LoC)
2. Accuracy
3. LoC fraction
- `*.pa.csv`: used for evaluating the performance of proximity attack. Each line corresponds to a target v-pin where the total number of other v-pins in slots S4, S6 and S7 is zero (see Fig. 6 of our [TVLSI'19 paper](https://ieeexplore.ieee.org/document/8789523)). Each line has four numbers:
1. The number of samples in slot S8
2. The number of samples in slot S0
3. The total number of samples in slots S2, S3, and S5
4. An integer: 1 if the number of samples in slot S1 > 0, else 0
- `*.metaInfo`:  Confusion matrix with a threshold of classification, as specified in `runClassificasion.sh`, running time, stats, etc.

