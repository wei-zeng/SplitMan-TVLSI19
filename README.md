# Analysis of Security of Split Manufacturing Using Machine Learning
This is the source code for [our TVLSI '19 paper](https://ieeexplore.ieee.org/document/8789523). Please kindly cite it if it is helpful for your academic research.

W. Zeng, B. Zhang and A. Davoodi, "Analysis of Security of Split Manufacturing Using Machine Learning," in IEEE Transactions on Very Large Scale Integration (VLSI) Systems, vol. 27, no. 12, pp. 2767-2780, Dec. 2019.

The code is authored by Wei Zeng and Boyu Zhang, and is now maintained by Wei Zeng. Our e-mail addresses can be found on the paper.

## How to run (under Linux environment):
1.	make sure `java` is installed
2.	make sure `parallel` is installed (this is not required but recommended for speed up classifications when running multiple benchmarks)
3.	`cd` into the root directory of this project
4.	run `generateTrainArffs.sh` to generate the arff files for training purpose
5.	run `runClassfication.sh` to build the machine learning models and perform classifications (this will take very long time depends on the split layer you choose)
6.	the LoC list for each vpin of all the benchmarks will be generated at `data/PotentialListFiles` or `data/BB_PotentialListFiles`
7.	the meta data of each benchmark will be generated at `data/MetaFiles` or `data/BB_MetaFiles`
8.	See `README.md` in these folders for formats of generated files.

## Vpin data
Vpin data is located at `data/VpinData` directory. This directory contains all the vpin data information for 5 benchmarks across 3 split layers. The naming convention of each file is `superblue(x)_(y).csv`, where `x` is the benchmark index and `y` is the split layer. 

The columns of each file are listed below:
1.	Vpin ID: this is the name used to uniquely identify this vpin
2.	X/Y coordinate: this is the X/Y coordinate of this vpin on the split layer in detailed coordinates. 
3.	WL down to L1: this is the total wirelength from the vpin to all the associated pins in the first layer. This is in global coordinates (each G cell is 1 and via between layers is also 1). 
4.	Pin Type: this is the pin type information of all the associated pins in layer 1 for this vpin. “I” stands for input pin and “O” stands for output pin.
5.	Number Layer One Pins: this is the number of pins associated with this vpin.
6.	Ave Cell Area Input/Output: due to typo, this should be “Sum Cell Area…” This is the summation of the area of the standard cells that connected to this vpin via its input/output pin.
7.	Ave Pin X/Y coordinate: this is the average X/Y coordinate of the associated pins in layer 1, in detailed coordinates. We calculate average since there might be multiple pins associated with one vpin. 
8.	CongestionCrouting/CongestionPlacement: these are two measurements of the congestions around the vpin and the pins, based on some bounding boxes from previous paper. 
9.	Matching Vpin ID: this is the name used to uniquely identify the matching vpin.
Please note that, eventually, we didn’t use (4.) in our work. For 9-feature data sets two features in (8.) are also excluded. For 7-feature data sets (3.) and (6. total cell area) are further excluded. Also note that this data assumes that one vpin is only connected with another one vpin, which is a simplification of the actual design. All of the benchmarks used are pre-placed and routed with NCTU router. For more information about how these data files are generated, please contact Jonathan Magana. 

## Build the Java class files
To simplify the process, we provided a `build.sh` script, which builds all java source code to class files in `DataPreprocessing` and `GeneratePotentialList` folders.

## Generate training samples
The machine learning package we are using in this project is called Weka. It requires a specific format for training and testing samples. The program in `DataPreprocessing` directory reads in the vpin information of each benchmark, generates training samples in the arff format, and place all the arff files in `data/(BB_)TrainArffs` directory. To run this program, enter the following command in terminal:
`java -cp DataPreprocessing/src/ Main` + arguments
This program takes 6 arguments, they are listed below:
1.	benchmark index. Only 1, 5, 10, 12, 18 are valid.
2.	split layer. Only 8, 6, 4 are valid.
3.	bounding box scaling factor. This is used to limit the search area for each vpin during both training sample generation and classification phase. This is a floating point number between 0 to 1.
4.	the absolute path of the “data” directory. Please don’t forget the “/” at the very end.
5.	enable the bounding box search area limitation or not. This is either 1 or 0.
6.	generate testing instances or not. This is either 1 or 0.

To simplify the process, we provided a `generateTrainArff.sh` script that could be used as a reference. The first part is for generating training sets for regular classification. The second part is for generating training sets for cross validation-based proximity attack (see later sections in this README).

The features of each training instance is covered in the paper. To control the feature set in the generated training samples, the only thing needs to be done is to selectively comment out some lines between line 224 to line 251 in the `Main.java` file. 

## Perform classification
Although we could generate all the testing instances and feed into Weka to perform classification, this approach quickly become impossible due to the large number of instances need to be tested. So, we have to build the testing instances on the fly while performing classification. The program in `GeneratePotentialList` directory performs the follow tasks:
1.	reads in all the training samples at the same split layer of all the other benchmarks.
2.	build the machine learning model
3.	reads in the vpin information of each benchmark
4.	build the testing samples on the fly and perform classification. 
The results of the classification are placed in `data/(BB_)PotentialListFiles` and `data/(BB_)MetaFiles` directory. The format of the potential list file is: for each vpin, it starts with that vpin’s ID, followed by all the potential matching vpins, and ends with an exclamation mark. This pattern repeats for all the vpins in the dataset. The format of files generated in `(BB_)MetaFiles` directories can be found in `README.md` inside these folders.

To run this program, enter the following command in terminal:
`java -cp 'GeneratePotentialList/weka.jar:GeneratePotentialList/src/' MainSoftImp` + arguments
This program takes 6 arguments, they are listed below:
1.	benchmark index. Only 1, 5, 10, 12, 18 are valid.
2.	split layer. Only 8, 6, 4 are valid.
3.	the absolute path of the “data” directory. Please don’t forget the “/” at the very end.
4.	enable the bounding box search area limitation or not. This is either 1 or 0.
5. Threshold of classification (default: 0.5). Test samples with model output greater than the threshold will be regarded as positive and vise versa.
6. Whether to output the potential list file. It is not recommended to generate the potential list for small threshold and/or for split level 4, as the generated files may be very large.

Besides a confusion matrix and potential lists associated with this specific threshold, a file with data points on the Size(LoC) vs Accuracy tradeoff curve can be found in `data/(BB_)MetaFiles/superblue_(x)_(y).hist.csv`. The format is specified in `data/(BB_)MetaFiles/README.md`.

During this process, the trained models are exported as `*.model`, which can be read by Weka.

The source code for classification is `MainSoftImp.java` in `GeneratePotentialList` folder. The default classifier used in the program is the `Bagging` of 10 `REPTree`s, but one can easily change it to other classifiers in Weka. Please also read the comments between lines 36 to 44 for more configurations about the feature order and the bounding box options.


## Proximity attack evaluation
In proximity attack (PA), for each target vpin we generate a very small potential list specific for proximity attack, the size of which is determined by cross validation. Then we pick from the list the closest vpin to the target vpin and verify if they are indeed a match or not. In our TVLSI'19 paper (Section III-H), we indicated that the success of the said proximity attack is equivalent to satisfying a series of conditions, which can be fully characterized by the number of other vpins in the 9 grids in Fig. 6. These numbers are generated along with the classification, in `data/(BB_)MetaFiles/superblue_(x)_(y).pa.csv`. The format is specified in `data/(BB_)MetaFiles/README.md`.

To get the proximity attack results, we will need the said numbers in `data/(BB_)MetaFiles/superblue_(x)_(y).pa.csv`, which is already generated when running classification. Besides, we will also need to determine the size of the (very small) potential list specific for proximity attack by cross validation. Cross validation is done by training and testing using the other four benchmarks, which can be performed simply by running the script `runCVPA.sh` (which requires training data sets that are different from the previous task, with file names `*_trainID.csv` and `*_for_PA.arff`; these can be generated using the second part of scripts in `generateTrainArffs.sh`.)

After running `runCVPA.sh`, look for files with naming format `data/(BB_)MetaFiles/superblue(x)_(y)_for_(z)_pa.csv`, which has the PA results of cross validation by testing benchmark `x` with split layer `y`, in order to help determine the size of PA-LoC of benchmark `z`. A MATLAB script in `evalPA.m` can be used as a reference to extract the results and plot the curve of "PA success rate vs PA-LoC fraction," where the $x$-coordinate of the highest point on the curve is used as the PA-LoC fraction for final PA evaluation of benchmark `z`. Please read the comments in this MATLAB script for more configurations.

## Two-level pruning
Two-level pruning is based on the intuition that misclassified negative vpin pairs (i.e. vpin pairs in LoC but are not real matches) may be of higher quality when used as training samples of a classifier. Please refer to Section III-E for details.

We provided a script `runTwoLevel.sh` to simplify the process. This script can be run as long as Level 1 training samples are already generated (i.e. by running the first part of `generateTrainArffs.sh`) and if so, there is no need to generate them again.

Please note that the threshold of classification does not vary (defaulted to 0.5) in this extension.

