% benchmark to evaluate PA success rate. 
% Please modify the numbers in the file name accordingly 
% for different benchmarks and split layers. 
% The current setting is to evaluate for benchmark 1, split layer 6.
pa1 = csvread('superblue1_6_pa.csv');

nvp = 42998; % the number of vpins. Please update per Table I of the 
% TVLSI'19 paper OR uncomment one of the corresponding lines; modify "6" in
% the file name to the correct split layer if using a different one.

% c = csvread('superblue1_6_for_5_pa.csv'); nvp = c(1,5); % for benchmark 1
% c = csvread('superblue5_6_for_1_pa.csv'); nvp = c(1,5); % for benchmark 5
% c = csvread('superblue10_6_for_1_pa.csv'); nvp = c(1,5); % for benchmark 10
% c = csvread('superblue12_6_for_1_pa.csv'); nvp = c(1,5); % for benchmark 12
% c = csvread('superblue18_6_for_1_pa.csv'); nvp = c(1,5); % for benchmark 18

% benchmarks for cross validation.
% Please modify the numbers in the file name accordingly 
% for different benchmarks and split layers. 
% The current setting is to evaluate for benchmark 1, split layer 6.
cvpa5 = csvread('superblue5_6_for_1_pa.csv');
cvpa10 = csvread('superblue10_6_for_1_pa.csv');
cvpa12 = csvread('superblue12_6_for_1_pa.csv');
cvpa18 = csvread('superblue18_6_for_1_pa.csv');
% the file names are 'superblue(x)_(y)_for_(z)_pa.csv' where z is the 
% benchmark for final evaluation, x has to go through the four benchmarks 
% other than z, and y is the split layer.

% granularity of th, recommended settings:
% split layer 8: 1e-6
% split layer 6: 1e-7
% split layer 4: 1e-8
granu = 1e-7;
th_points = 2000;

cvpa5(:,1:3) = cvpa5(:,1:3) / cvpa5(1,5);
cvpa10(:,1:3) = cvpa10(:,1:3) / cvpa10(1,5);
cvpa12(:,1:3) = cvpa12(:,1:3) / cvpa12(1,5);
cvpa18(:,1:3) = cvpa18(:,1:3) / cvpa18(1,5);
s = zeros(1,th_points);
for k=1:th_points
    %k
th = k*granu;
for i=1:size(cvpa5,1), if cvpa5(i,1) < th && cvpa5(i,2) > 0 && (cvpa5(i,4) == 0 || sum(cvpa5(i,1:3)) >= th),
s(k) = s(k) + 1/cvpa5(i,2)/cvpa5(i,5)/cvpa5(i,5);
end
end
for i=1:size(cvpa10,1), if cvpa10(i,1) < th && cvpa10(i,2) > 0 && (cvpa10(i,4) == 0 || sum(cvpa10(i,1:3)) >= th),
s(k) = s(k) + 1/cvpa10(i,2)/cvpa10(i,5)/cvpa10(i,5);
end
end
for i=1:size(cvpa12,1), if cvpa12(i,1) < th && cvpa12(i,2) > 0 && (cvpa12(i,4) == 0 || sum(cvpa12(i,1:3)) >= th),
s(k) = s(k) + 1/cvpa12(i,2)/cvpa12(i,5)/cvpa12(i,5);
end
end
for i=1:size(cvpa18,1), if cvpa18(i,1) < th && cvpa18(i,2) > 0 && (cvpa18(i,4) == 0 || sum(cvpa18(i,1:3)) >= th),
s(k) = s(k) + 1/cvpa18(i,2)/cvpa18(i,5)/cvpa18(i,5);
end
end
end

plot([1:th_points] * granu, s);

[~, th] = max(s);
th = double(th) * granu
pa1(:,1:3) = pa1(:,1:3) / nvp;

spa = 0;
for i = 1:size(pa1,1)
if pa1(i,1) < th && pa1(i,2) > 0 && (pa1(i,4)==0 || sum(pa1(i,1:3)) >= th)
spa = spa + 1/pa1(i,2)/nvp/nvp;
end
end

% success rate of cross validation based PA
spa