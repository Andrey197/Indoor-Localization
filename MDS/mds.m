function e = mds()
  D = dlmread('input/grid_distances.txt', ',', 0, 0);
  Dprim = dlmread('input/fdistances.txt', ',', 0, 0);
  
  [Y e] = cmdscale(D);
  [Yprim eprim] = cmdscale(Dprim);
  
  subplot(1, 2, 1);
  plot(Y(:,1), Y(:,2), 'or');
  title('Training Set #1 - Grid Distances');
  
  subplot(1, 2, 2);
  plot(Yprim(:,1), Yprim(:,2), 'ob');
  title('Training Set #2 - Fingeprint Distances');
   
  
endfunction
