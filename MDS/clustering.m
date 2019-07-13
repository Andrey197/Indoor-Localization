% Initialization
pkg load statistics
clear; close all; clc;

% Clustering Set ---------------------------------------------------------------------

% Loading the data.
fprintf('Loading the data set...\n');

D = dlmread('output/roomsD.txt', ',', 0, 0);
  
[Y e] = cmdscale(D);

% Plotting the data.
fprintf('Plotting the data set #1...\n');
subplot(1, 2, 1);
plot(Y(:, 1), Y(:, 2), 'k+','LineWidth', 1, 'MarkerSize', 7);
title('Training Set');

% Training K-Means.
fprintf('Training K-Means for data set...\n');
K = 3; % Number of centroids.
max_iterations = 30; % How many iterations we will do to find optimal centroids positions.
[centroids, closest_centroids_ids] = k_means_train(Y, K, max_iterations);

% Plotting clustered data.
fprintf('Plotting clustered data for data set...\n');
subplot(1, 2, 2);
for k=1:K
    % Plotting the cluster.
    cluster_x = Y(closest_centroids_ids == k, :);
    plot(cluster_x(:, 1), cluster_x(:, 2), '+');
    hold on;

    % Plotting centroid.
    centroid = centroids(k, :);
    plot(centroid(:, 1), centroid(:, 2), 'ko', 'MarkerFaceColor', 'r', 'MarkerSize', 8);
    hold on;
end
title('Clustered Set');

f = fopen("output/clusters.txt", "w");
for i = 1:length(closest_centroids_ids)
  fprintf(f, "%d\n", closest_centroids_ids(i));  
endfor
fclose(f);

hold off;
