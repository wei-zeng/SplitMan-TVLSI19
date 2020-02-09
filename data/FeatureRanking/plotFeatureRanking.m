infoGain_1_4 = getFeatureWeights('feature_ranking_InfoGain_1_4.txt');
infoGain_5_4 = getFeatureWeights('feature_ranking_InfoGain_5_4.txt');
infoGain_10_4 = getFeatureWeights('feature_ranking_InfoGain_10_4.txt');
infoGain_12_4 = getFeatureWeights('feature_ranking_InfoGain_12_4.txt');
infoGain_18_4 = getFeatureWeights('feature_ranking_InfoGain_18_4.txt');

infoGain_1_6 = getFeatureWeights('feature_ranking_InfoGain_1_6.txt');
infoGain_5_6 = getFeatureWeights('feature_ranking_InfoGain_5_6.txt');
infoGain_10_6 = getFeatureWeights('feature_ranking_InfoGain_10_6.txt');
infoGain_12_6 = getFeatureWeights('feature_ranking_InfoGain_12_6.txt');
infoGain_18_6 = getFeatureWeights('feature_ranking_InfoGain_18_6.txt');

infoGain_1_8 = getFeatureWeights('feature_ranking_InfoGain_1_8.txt');
infoGain_5_8 = getFeatureWeights('feature_ranking_InfoGain_5_8.txt');
infoGain_10_8 = getFeatureWeights('feature_ranking_InfoGain_10_8.txt');
infoGain_12_8 = getFeatureWeights('feature_ranking_InfoGain_12_8.txt');
infoGain_18_8 = getFeatureWeights('feature_ranking_InfoGain_18_8.txt');

Correlation_1_4 = getFeatureWeights('feature_ranking_Correlation_1_4.txt');
Correlation_5_4 = getFeatureWeights('feature_ranking_Correlation_5_4.txt');
Correlation_10_4 = getFeatureWeights('feature_ranking_Correlation_10_4.txt');
Correlation_12_4 = getFeatureWeights('feature_ranking_Correlation_12_4.txt');
Correlation_18_4 = getFeatureWeights('feature_ranking_Correlation_18_4.txt');

Correlation_1_6 = getFeatureWeights('feature_ranking_Correlation_1_6.txt');
Correlation_5_6 = getFeatureWeights('feature_ranking_Correlation_5_6.txt');
Correlation_10_6 = getFeatureWeights('feature_ranking_Correlation_10_6.txt');
Correlation_12_6 = getFeatureWeights('feature_ranking_Correlation_12_6.txt');
Correlation_18_6 = getFeatureWeights('feature_ranking_Correlation_18_6.txt');

Correlation_1_8 = getFeatureWeights('feature_ranking_Correlation_1_8.txt');
Correlation_5_8 = getFeatureWeights('feature_ranking_Correlation_5_8.txt');
Correlation_10_8 = getFeatureWeights('feature_ranking_Correlation_10_8.txt');
Correlation_12_8 = getFeatureWeights('feature_ranking_Correlation_12_8.txt');
Correlation_18_8 = getFeatureWeights('feature_ranking_Correlation_18_8.txt');

infoGain_layer_4 = [infoGain_1_4, infoGain_5_4, infoGain_10_4, infoGain_12_4, infoGain_18_4]';
infoGain_layer_6 = [infoGain_1_6, infoGain_5_6, infoGain_10_6, infoGain_12_6, infoGain_18_6]';
infoGain_layer_8 = [infoGain_1_8, infoGain_5_8, infoGain_10_8, infoGain_12_8, infoGain_18_8]';
Correlation_layer_4 = [Correlation_1_4, Correlation_5_4, Correlation_10_4, Correlation_12_4, Correlation_18_4]';
Correlation_layer_6 = [Correlation_1_6, Correlation_5_6, Correlation_10_6, Correlation_12_6, Correlation_18_6]';
Correlation_layer_8 = [Correlation_1_8, Correlation_5_8, Correlation_10_8, Correlation_12_8, Correlation_18_8]';

% generateBarPlot('InfoGain', 4, infoGain_layer_4);
% generateBarPlot('InfoGain', 6, infoGain_layer_6);
% generateBarPlot('InfoGain', 8, infoGain_layer_8);
% generateBarPlot('Correlation', 4, Correlation_layer_4);
% generateBarPlot('Correlation', 6, Correlation_layer_6);
% generateBarPlot('Correlation', 8, Correlation_layer_8);

generateBarPlot('Correlation', 68, [Correlation_layer_6; [0 0 0 0 0 0 0 0 0];Correlation_layer_8]);

% generateBarPlot('InfoGain', 68, [infoGain_layer_6; [0 0 0 0 0 0 0 0 0];infoGain_layer_8]);

function feature_weights = getFeatureWeights(fileName)
    file = fopen(fileName);
    data = textscan(file, '%f %f %s');
    feature_weights = data{1};
    feature_index = data{2};
    [~, order] = sort(feature_index);
    feature_weights = feature_weights(order);
end

function [] = generateBarPlot(which, layer, data)
    data_ave = mean(data);
    figure('Position', [100 100 1900 800]);
%     bar([data; data_ave]);
    bar(data);
    ylim([0 1]);
    set(gca, 'FontSize', 28);
%     xticklabels({'sb1', 'sb5', 'sb10', 'sb12', 'sb18', 'average'});
    xticklabels({'sb1', 'sb5', 'sb10', 'sb12', 'sb18', '', 'sb1', 'sb5', 'sb10', 'sb12', 'sb18'});
    yticks([0:0.1:1]);
%     ylabel(which);
%     legend({'diffVpinX','diffVpinY', 'diffPinX', 'diffPinY', 'hammingVpin', 'hammingPin', 'totalWireLength', 'totalCellArea', 'diffCellArea'}, ...
%         'Position', [0.19 0.65 0.1 0.2], 'FontSize', 23);
    grid on;
    print(gcf, sprintf('%s_layer_%d.jpeg', which, layer), '-djpeg', '-r0');
end
