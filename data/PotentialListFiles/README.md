This folder will contain the list of candidate for each target v-pin after `runClassification.sh` if `boundingBox` is NOT enabled, with the classification of threshold specified in `runClassification.sh`.

**NOTE**: It is recommend NOT to generate the list of candidate file for a small threshold or for split layer 4, as the potential LoC is large and the generated files can be as large as several GBs. This could also slow down the process of classification due to massive I/Os. The option whether to write LoC files can be set in `runClassification.sh`.

