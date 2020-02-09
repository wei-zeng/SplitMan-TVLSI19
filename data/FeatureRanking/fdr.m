for benchmark = [1,5,10,12,18]
    for level = [4]
fidr = fopen(strcat('superblue', num2str(benchmark), '_', num2str(level), '.arff'),'r');
for i=1:14
    line = fgets(fidr); % skip header
end
f = textscan(fidr, '%f%f%f%f%f%f%f%f%f%f%f%s\n', 'delimiter', ',');
if benchmark == 1
    g = f;
else
for j=1:12
    g{1,j} = [g{1,j};f{1,j}];
end
end
fclose(fidr);
    end
end
h = zeros(length(g{1,1}),12);
for j=1:11
    h(:,j) = g{1,j};
end
h(:,12) = string(g{1,10})=='true';
a1 = h(h(:,10)==1,:);
a0 = h(h(:,10)==0,:);
FDR = (mean(a1)-mean(a0)).^2./(std(a1).^2+std(a0).^2);
